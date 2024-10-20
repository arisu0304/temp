package bibid.specialAuction.service.impl;

import bibid.dto.AuctionDto;
import bibid.dto.StreamingDto;
import bibid.entity.Auction;
import bibid.entity.Streaming;
import bibid.livestation.domain.LiveStationChannel;
import bibid.livestation.dto.LiveStationInfoDTO;
import bibid.livestation.dto.LiveStationUrlDTO;
import bibid.livestation.service.LiveStationPoolManager;
import bibid.livestation.service.LiveStationService;
import bibid.repository.auction.AuctionRepository;
import bibid.specialAuction.repository.SpecialAuctionRepository;
import bibid.specialAuction.repository.StreamingRepository;
import bibid.specialAuction.service.ChatRoomService;
import bibid.specialAuction.service.SpecialAuctionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialAuctionServiceImpl implements SpecialAuctionService {


    private final SpecialAuctionRepository specialAuctionRepository;
    private final StreamingRepository streamingRepository;
    private final LiveStationPoolManager liveStationPoolManager;
    private final TaskScheduler taskScheduler;


    public enum AuctionType {
        REALTIME("실시간 경매"),
        BLIND("블라인드 경매");

        private final String koreanName;

        AuctionType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    // 경매 시작 30분 전에 스트리밍 채널 할당 스케줄 설정
    public void scheduleAuctionStart(AuctionDto auctionDto) {
        LocalDateTime auctionStartTime = auctionDto.getStartingLocalDateTime().minusMinutes(30);
        taskScheduler.schedule(() -> prepareStreamingForAuction(auctionDto),
                java.util.Date.from(auctionStartTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    // 경매 시작 30분 전에 스트리밍 채널을 준비
    public void prepareStreamingForAuction(AuctionDto auctionDto) {
//        try {
//            // 채널 할당 로직 추가
//            String channelId = liveStationService.allocateChannel(auctionDto.getAuctionId());
//            auctionDto.setChannelId(channelId);
//            auctionRepository.save(auctionDto.toEntity());
//        } catch (Exception e) {
//            log.error("채널 할당 실패: {}", e.getMessage());
//        }
    }

    // 경매 종료 시 채널 반환 로직 (필요 시 구현)
    public void releaseStreamingChannel(AuctionDto auctionDto) {
//        try {
//            liveStationService.releaseChannel(auctionDto.getChannelId());
//        } catch (Exception e) {
//            log.error("채널 반환 실패: {}", e.getMessage());
//        }
    }

    // 경매 타입과 종료 시간이 현재 이후인 (즉, 아직 경매가 끝나지 않은) 경매 목록을 페이징 처리하여 조회
    @Override
    public Page<AuctionDto> findAuctionsByType(String auctionType, Pageable pageable) {
        String koreanAuctionType = "";

        // 문자열 비교는 .equals()로 처리
        if ("realtime".equals(auctionType)) {
            koreanAuctionType = "실시간 경매";
        } else if ("blind".equals(auctionType)) {
            koreanAuctionType = "블라인드 경매";
        }

        // 로깅 - 경매 타입 확인
        log.info("Requested auctionType: {}", auctionType);
        log.info("Converted koreanAuctionType: {}", koreanAuctionType);

        LocalDateTime currentTime = LocalDateTime.now();

        // 로깅 - 현재 시간 확인
        log.info("Current time for filtering auctions: {}", currentTime);

        Page<AuctionDto> auctionDtoPage = specialAuctionRepository.findAuctionsByType(koreanAuctionType, currentTime, pageable)
                .map(Auction::toDto);

        // 로깅 - 쿼리 결과 확인
        log.info("Found auctions: {}", auctionDtoPage.getContent());
        log.info("Total elements: {}, Total pages: {}", auctionDtoPage.getTotalElements(), auctionDtoPage.getTotalPages());

        return auctionDtoPage;
    }

    @Override
    public StreamingDto findStreamingByAuctionIndex(Long auctionIndex) {

        Optional<Streaming> streaming = streamingRepository.findByAuction_AuctionIndex(auctionIndex);

        // 스트리밍이 존재할 경우, DTO로 변환해서 반환
        return streaming.map(Streaming::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Streaming not found for auctionIndex: " + auctionIndex));
    }

//    // 옥션이 시작될 때
//    @PostMapping("/start")
//    public ResponseEntity<?> startAuction(@RequestParam Long auctionId) {
//        // 옥션 시작
//        auctionService.startAuction(auctionId);
//
//        // 라이브 스테이션 풀에서 채널을 할당받음
//        Optional<LiveStationChannel> optionalChannel = liveStationPoolManager.getAvailableChannel();
//        if (optionalChannel.isPresent()) {
//            LiveStationChannel channel = optionalChannel.get();
//            auctionService.startBroadcast(auctionId, channel);  // 방송 시작
//
//            return ResponseEntity.ok("옥션 시작. 채널 ID: " + channel.getChannelId());
//        } else {
//            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("사용 가능한 채널이 없습니다.");
//        }
//    }
//
//    // 옥션이 끝날 때
//    @PostMapping("/end")
//    public ResponseEntity<String> endAuction(@RequestParam Long auctionId, @RequestParam String channelId) {
//        // 옥션 종료
//        auctionService.endAuction(auctionId);
//
//        // 라이브 채널을 반납
//        LiveStationChannel channel = liveStationPoolManager.getChannelById(channelId);
//        liveStationPoolManager.releaseChannel(channel);  // 채널 반납
//
//        return ResponseEntity.ok("옥션이 종료되고 채널이 반납되었습니다.");
//    }


}
