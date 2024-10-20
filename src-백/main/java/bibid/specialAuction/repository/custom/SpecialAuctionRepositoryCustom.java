package bibid.specialAuction.repository.custom;

import bibid.dto.SpecialAuctionListMapping;

import java.util.List;

public interface SpecialAuctionRepositoryCustom {

    List<SpecialAuctionListMapping> findAllByAuctionType(String auctionType);
}
