package bibid.schedular;

import bibid.entity.Auction;
import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.service.LiveStationPoolManager;
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
    private final LiveStationPoolManager liveStationPoolManager;
    private ScheduledFuture<?> futureTask;

    public void scheduleChannelAllocation(Long auctionIndex, LocalDateTime startingLocalDateTime) {
        LocalDateTime allocationTime = startingLocalDateTime.minusMinutes(30);
        Date scheduleDate = Date.from(allocationTime.atZone(ZoneId.systemDefault()).toInstant());

        futureTask = taskScheduler.schedule(() -> {
            try {
                LiveStationChannel allocatedChannel = liveStationPoolManager.allocateChannel();

                Auction auction = auctionRepository.findById(auctionIndex)
                        .orElseThrow(() -> new RuntimeException("옥션을 찾을 수 없습니다."));

                auction.setAuctionStatus("준비중");
                auction.setLiveStationChannel(allocatedChannel);
                auctionRepository.save(auction);
                
                /*
                *  할당할 때 채널 생성하는 로직을 allocateChannel에 줄지 이 스케쥴러에서 줄지는 고민
                * */

            } catch (Exception e) {
                System.out.println("채널 할당 중 오류 발생: " + e.getMessage());
            }
        }, scheduleDate);
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)  // 15분마다 실행 (단위: 밀리초)
    public void checkAndReallocateChannels() {

        List<Auction> activeAuctions = getActiveRealtimeAuctions();

        for (Auction auction : activeAuctions) {

            if(auction.)
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime auctionStartTime = auction.getStartingLocalDateTime();

            if ( auctionStartTime.minusMinutes(30).isBefore(currentTime) ) {

                LiveStationChannel allocatedChannel = liveStationPoolManager.allocateChannel();

                auction.setAuctionStatus("준비중");
                auction.setLiveStationChannel(allocatedChannel);
                auctionRepository.save(auction);

            }
        }
    }

    private List<Auction> getActiveRealtimeAuctions() {
        return specialAuctionRepository.findByAuctionStatusInAndAuctionType(
                Arrays.asList("대기중", "준비중"), "실시간 경매"
        );
    }
}