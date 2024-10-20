package bibid.specialAuction.service;

import bibid.dto.AuctionDto;
import bibid.dto.StreamingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpecialAuctionService {
    // 경매 타입에 따른 경매 목록을 페이징 처리하여 반환
    Page<AuctionDto> findAuctionsByType(String auctionType, Pageable pageable);

    StreamingDto findStreamingByAuctionIndex(Long auctionIndex);
}
