package bibid.schedular;

import bibid.entity.Auction;
import bibid.livestation.domain.LiveStationChannel;
import bibid.repository.auction.AuctionRepository;
import bibid.repository.specialAuction.SpecialAuctionRepository;
import bibid.service.specialAuction.SpecialAuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class SpecialAuctionSchedular {

    private final SpecialAuctionService specialAuctionService;
    private final AuctionRepository auctionRepository;
    private final TaskScheduler taskScheduler;
    private final SpecialAuctionRepository specialAuctionRepository;
    private ScheduledFuture<?> futureTask;

    public void scheduleChannelAllocation(Long auctionIndex, LocalDateTime startingLocalDateTime) {
        LocalDateTime allocationTime = startingLocalDateTime.minusMinutes(30);
        Date scheduleDate = Date.from(allocationTime.atZone(ZoneId.systemDefault()).toInstant());

        futureTask = taskScheduler.schedule(() -> {
            try {
                LiveStationChannel channel = specialAuctionService.allocateAvailableChannel(auctionIndex);
                Auction auction = auctionRepository.findById(auctionIndex)
                        .orElseThrow(() -> new RuntimeException("옥션을 찾을 수 없습니다."));

                auction.setAuctionStatus("준비중");
                auctionRepository.save(auction);
                if (channel != null) {
                    System.out.println("옥션 " + auctionIndex + "에 대한 채널 할당 성공: " + channel.getChannelId());
                    System.out.println("옥션 " + auctionIndex + "에 대한 채널 할당 실패");
                    System.out.println("새로운 채널 생성 - 10분 정도의 시간이 소요됩니다.");
                } else {
                    System.out.println("옥션 " + auctionIndex + "에 대한 채널 할당 실패");
                    System.out.println("새로운 채널 생성 - 10분 정도의 시간이 소요됩니다.");
                }

            } catch (Exception e) {
                System.out.println("채널 할당 중 오류 발생: " + e.getMessage());
            }
        }, scheduleDate);
    }

//    public void cancelScheduledTask() {
//        if (futureTask != null && !futureTask.isCancelled()) {
//            futureTask.cancel(true);  // 작업 취소
//            System.out.println("스케줄링 작업 취소됨.");
//        }
//    }


    @Scheduled(fixedRate = 15 * 60 * 1000)  // 15분마다 실행 (단위: 밀리초)
    public void checkAndReallocateChannels() {

        List<Auction> activeAuctions = getActiveRealtimeAuctions();

        for (Auction auction : activeAuctions) {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime auctionStartTime = auction.getStartingLocalDateTime();

            if ( auctionStartTime.minusMinutes(30).isBefore(currentTime) ) {

                LiveStationChannel channel = specialAuctionService.allocateAvailableChannel(auction.getAuctionIndex());
                if (channel == null) {
                    System.out.println("채널 할당 실패: " + auction.getAuctionIndex());
                } else {
                    auction.setChannelId(channel.getChannelId());
                    auction.setAuctionStatus("방송 준비중");
                    auctionRepository.save(auction);
                    System.out.println("채널 할당 성공: " + channel.getChannelId());
                }
            }
        }
    }

    private List<Auction> getActiveRealtimeAuctions() {
        return specialAuctionRepository.findByAuctionStatusInAndAuctionType(
                Arrays.asList("대기중", "준비중"), "실시간 경매"
        );
    }
}