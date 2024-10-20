import React from 'react';

function AuctionEndPopup({ auction, handleClosePopup }) {

  const WinnerIndex = auction.auctionDetailDto.WinnerIndex;
  const WinningBid= auction.auctionDetailDto.WinningBid;


  return (
    <div className="SAoverlay">
      <div className='SAtotalPopup'>
        <div className="SAendPopup">
          <h2>경매 마감 안내</h2>
          <p>{auction.productName} <br/>상품의 실시간 경매가 마감되었습니다.</p>
          <p id='SAsuccessfulBidderName'>최종 낙찰자 : *최종 낙찰자*님</p>
          <p id='SAsuccessfulBidderPrice'>낙찰 금액 : **낙찰 금액**원</p>
          <p id='SAendAuctionMent'>참여해 주신 모든 분들께 감사드리며, <br/> 다음 경매에도 많은 관심 부탁드립니다!</p>
          <button className='SAendOkbttn' onClick={handleClosePopup}>확인</button>
        </div>
      </div>
    </div>
  );
}

export default AuctionEndPopup;
