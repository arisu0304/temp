package bibid.service.specialAuction;

import bibid.dto.AuctionDto;
import bibid.dto.StreamingDto;
import bibid.livestation.domain.LiveStationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpecialAuctionService {
    // 경매 타입에 따른 경매 목록을 페이징 처리하여 반환
    Page<AuctionDto> findAuctionsByType(String auctionType, Pageable pageable);

    // 방송 시작 시 가용 채널 할당
    LiveStationChannel allocateAvailableChannel(Long auctionIndex);

    // 방송 종료 시 채널 반납
    void releaseChannel(LiveStationChannel channel);
}
