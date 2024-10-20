package bibid.specialAuction.scheduler;

import bibid.entity.Auction;
import bibid.entity.Streaming;
import bibid.livestation.dto.LiveStationInfoDTO;
import bibid.livestation.dto.LiveStationUrlDTO;
import bibid.livestation.service.LiveStationService;
import bibid.repository.auction.AuctionRepository;
import bibid.specialAuction.repository.SpecialAuctionRepository;
import bibid.specialAuction.repository.StreamingRepository;
import bibid.specialAuction.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class SpecialAuctionSchedular {

    private final SpecialAuctionRepository specialAuctionRepository;
    private final AuctionRepository auctionRepository;
    private final ChatRoomService chatRoomService;
    private final LiveStationService liveStationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행 (60000 밀리세컨드)
    public void checkAuctionStart() {
        log.info("checkAuctionStart() 실행됨");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesLater = now.plusMinutes(30);  // 30분 후로 설정

        try {
            // 경매 시작 시간이 30분 이내인 경매들 찾기
            List<Auction> upcomingAuctions = specialAuctionRepository.findAuctionsStartingWithinThirtyMinutes(now, thirtyMinutesLater);

            for (Auction auction : upcomingAuctions) {
                // 1. 채팅방이 생성되지 않은 경우 채팅방 생성
                if (!auction.isChatRoomCreated()) {
                    chatRoomService.createChatRoom(auction.getAuctionIndex()); // 채팅방 생성
                    auction.setChatRoomCreated(true); // 채팅방 생성 상태 업데이트
                }

                // 2. 스트리밍이 생성되지 않은 경우 스트리밍 채널 생성
                if (!auction.isStreamingCreated()) {
                    String UUIDShort = UUID.randomUUID().toString().substring(0, 5); // UUID의 첫 5글자만 사용
                    String originalChannelName = auction.getAuctionIndex() + "_" + UUIDShort;
                    String channelName = originalChannelName.substring(0, Math.min(20, originalChannelName.length())); // 20자 이내로 자르기
                    log.info("channelName : {}", channelName);

                    // 채널 생성 예외 처리
                    String channelId;
                    try {
                        channelId = liveStationService.createChannel(channelName);
                    } catch (Exception e) {
                        log.error("채널 생성 중 오류 발생: {}", e.getMessage());
                        continue;  // 예외 발생 시 해당 경매는 건너뜀
                    }

                    // 채널 정보 가져오기 예외 처리
                    LiveStationInfoDTO liveStationInfoDTO;
                    try {
                        liveStationInfoDTO = liveStationService.getChannelInfo(channelId);
                    } catch (Exception e) {
                        log.error("채널 정보 가져오는 중 오류 발생: {}", e.getMessage());
                        liveStationInfoDTO = new LiveStationInfoDTO(); // 기본값으로 처리
                    }

                    // 서비스 URL 가져오기 예외 처리
                    List<LiveStationUrlDTO> liveStationUrlDTOList;
                    try {
                        liveStationUrlDTOList = liveStationService.getServiceURL(channelId, "GENERAL");
                    } catch (Exception e) {
                        log.error("서비스 URL 가져오는 중 오류 발생: {}", e.getMessage());
                        liveStationUrlDTOList = new ArrayList<>();  // 기본값으로 빈 리스트 사용
                    }

                    log.info("channelId : {}", channelId);

                    // Streaming 객체 생성
                    Streaming streaming = Streaming.builder()
                            .channelId(channelId)
                            .channelName(Optional.ofNullable(liveStationInfoDTO.getChannelName()).orElse("Unknown Channel"))
                            .channelStatus(Optional.ofNullable(liveStationInfoDTO.getChannelStatus()).orElse("Unknown Status"))
                            .cdnInstanceNo(Optional.of(liveStationInfoDTO.getCdnInstanceNo()).orElse(0))
                            .cdnStatusName(Optional.ofNullable(liveStationInfoDTO.getCdnStatusName()).orElse("Unknown CDN Status"))
                            .streamKey(Optional.ofNullable(liveStationInfoDTO.getStreamKey()).orElse("Unknown Stream Key"))
                            .publishUrl(Optional.ofNullable(liveStationInfoDTO.getPublishUrl()).orElse("Unknown Publish URL"))
                            .streamUrlList(Optional.ofNullable(liveStationUrlDTOList)
                                    .map(list -> list.stream().map(LiveStationUrlDTO::getUrl).toList())
                                    .orElse(new ArrayList<>()))  // API 실패 시 빈 리스트로 처리
                            .startTime(auction.getStartingLocalDateTime())
                            .endTime(auction.getEndingLocalDateTime().plusMinutes(30))  // 스트리밍 종료 시간 설정
                            .auction(auction)
                            .build();

                    // 경매에 스트리밍 정보 설정
                    auction.setStreaming(streaming);
                    auction.setStreamingCreated(true);

                    // 경매 정보 저장
                    auctionRepository.save(auction);
                }
            }
        } catch (Exception e) {
            log.error("경매 시작 스케줄링 중 오류 발생: ", e);
        }
    }

}
