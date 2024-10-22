package bibid.service.specialAuction.impl;

import bibid.dto.AuctionDto;
import bibid.entity.Auction;
import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.service.LiveStationPoolManager;
import bibid.repository.specialAuction.SpecialAuctionRepository;
import bibid.repository.specialAuction.StreamingRepository;
import bibid.service.specialAuction.ChatRoomService;
import bibid.service.specialAuction.SpecialAuctionService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialAuctionServiceImpl implements SpecialAuctionService {


    private final SpecialAuctionRepository specialAuctionRepository;
    private final ChatRoomService chatRoomService;
    private final StreamingRepository streamingRepository;
    private final LiveStationPoolManager liveStationPoolManager;

    @Getter
    @AllArgsConstructor
    public enum AuctionType {
        REALTIME("실시간 경매"),
        BLIND("블라인드 경매");
        private final String koreanName;
    }

//    // 경매 시작 시간이 30분 이내인 경매들 찾기 (찾아서 채팅방 생성)
//    @Transactional
//    @Scheduled(fixedRate = 6000) // 1분마다 실행
    public void checkAuctionStart() {
        log.info("checkAuctionStart() 실행됨");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesLater = now.plusMinutes(3000);

        try {
            // 경매 시작 시간이  이내인 경매들을 찾음
            List<Auction> upcomingAuctions = specialAuctionRepository.findAuctionsStartingWithinThirtyMinutes(now, thirtyMinutesLater);

            for (Auction auction : upcomingAuctions) {
                // 1. 채팅방이 생성되지 않은 경우 채팅방 생성
                if (!auction.isChatRoomCreated()) {
                    chatRoomService.createChatRoom(auction.getAuctionIndex()); // 채팅방 생성
                    auction.setChatRoomCreated(true); // 채팅방 생성 상태 업데이트
                }

//                // 2. 스트리밍이 생성되지 않은 경우 스트리밍 채널 생성
//                if (!auction.isStreamingCreated()) {
//                    String UUIDShort = UUID.randomUUID().toString().substring(0, 5); // UUID의 첫 5글자만 사용
//                    String originalChannelName = auction.getAuctionIndex() + "_" + UUIDShort;
//                    String channelName = originalChannelName.substring(0, Math.min(20, originalChannelName.length())); // 20자 이내로 자르기
//                    log.info("channelName : {}", channelName);
//                    String channelId = liveStationService.createChannel(channelName);
//                    LiveStationInfoDTO liveStationInfoDTO = liveStationService.getChannelInfo(channelId);
//                    List<LiveStationUrlDTO> liveStationUrlDTOList = liveStationService.getServiceURL(channelId, "GENERAL");
//
//                    Streaming streaming = Streaming.builder()
//                            .channelId(channelId)
//                            .channelName(liveStationInfoDTO != null ? liveStationInfoDTO.getChannelName() : null) // null이면 그대로 null
//                            .channelStatus(liveStationInfoDTO != null ? liveStationInfoDTO.getChannelStatus() : null)
//                            .cdnInstanceNo(liveStationInfoDTO != null ? liveStationInfoDTO.getCdnInstanceNo() : null)
//                            .cdnStatus(liveStationInfoDTO != null ? liveStationInfoDTO.getCdnStatus() : null)
//                            .publishUrl(liveStationInfoDTO != null ? liveStationInfoDTO.getPublishUrl() : null)
//                            .streamKey(liveStationInfoDTO != null ? liveStationInfoDTO.getStreamKey() : null)
//                            .startTime(auction.getStartingLocalDateTime())
//                            .endTime(auction.getEndingLocalDateTime().plusMinutes(30)) // 스트리밍 종료 시간 설정
//                            .auction(auction)
//                            .streamUrlList(liveStationUrlDTOList != null
//                                    ? liveStationUrlDTOList.stream().map(LiveStationUrlDTO::getUrl).toList()
//                                    : new ArrayList<>())
//                            .build();
//
//                    auction.setStreaming(streaming);
//                    auction.setStreamingCreated(true);
//                    auctionRepository.save(auction);
//                }
            }
        } catch (Exception e) {
            log.error("경매 시작 스케줄링 중 오류 발생: ", e);
        }
    }


    @Override
    public Page<AuctionDto> findAuctionsByType(String auctionType, Pageable pageable) {

        AuctionType auctionTypeEnum;

        try {
            auctionTypeEnum = AuctionType.valueOf(auctionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("올바르지 않은 경매 타입입니다.");
        }

        String koreanAuctionType = auctionTypeEnum.getKoreanName();
        LocalDateTime currentTime = LocalDateTime.now();

        Page<AuctionDto> auctionDtoPage = specialAuctionRepository.findAuctionsByType(koreanAuctionType, currentTime, pageable)
                .map(Auction::toDto);

        return auctionDtoPage;
    }


    @Override
    public LiveStationChannel allocateAvailableChannel(Long auctionIndex) {

        LiveStationChannel channel = liveStationPoolManager.getAvailableChannel();
        if (channel != null) {
            channel.setAvailable(false);
            return channel;
        } else {
//            // 가용한 채널이 없으면 새로 생성 하는 로직 수정 필
//            String newChannelId = createChannel(auctionId);  // 새 채널 생성
//            LiveStationChannel newChannel = LiveStationChannel.builder()
//                    .isAvailable(false)
//                    .isStreaming(false)
//                    .build();  // 새 채널 사용 상태로 설정
//            liveStationPoolManager.allocateChannel(newChannel);  // 새로 생성한 채널 풀에 추가
            return null;
        }
    }

    // 방송 종료 시 채널 반납
    @Override
    public void releaseChannel(LiveStationChannel channel) {
        // API 호출로 방송 종료 처리 필요 시 추가
        // stopBroadcast(channel);  // 방송 종료 API 호출 가능

        liveStationPoolManager.releaseChannel(channel);  // 채널을 풀에 반납
        System.out.println("채널 " + channel.getChannelId() + " 반납 완료");
    }

}
