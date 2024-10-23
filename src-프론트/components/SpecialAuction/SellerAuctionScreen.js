import React, { useEffect, useRef, useState } from 'react';
import { getFormattedRemainingTime } from '../../util/utils';
import SAsellerSteamingBox from './SAsellerStreamingBox';
import '../../css/SpecialAuction/SAitem.css';
import axios from 'axios';
import VideoSection from './VideoSection';
import { OBSWebSocket } from 'obs-websocket-js';
function SellerAuctionScreen({
  webSocketProps, auction, remainingTime, closeSellerPage
}) {

  const obs = useRef(new OBSWebSocket());
  const [isConnected, setIsConnected] = useState(false); // WebSocket 연결 상태 관리
  const [isStreaming, setIsStreaming] = useState(false); // 스트리밍 상태 관리
  const [channelInfo, setChannelInfo] = useState({
    serverURL: '',
    streamKey: '',
    channelStatus: 'CREATING',
    cdnStatusName: 'CREATING',
    streamingUrl: []
  });

  // OBS WebSocket 연결 설정 함수
  const connectOBS = () => {
    obs.current = new OBSWebSocket();
    obs.current.connect('ws://localhost:4455') // OBS WebSocket 서버로 연결
      .then(() => {
        console.log('OBS WebSocket 연결 성공');
        setIsConnected(true); // 연결 성공 시 상태 업데이트
        checkStreamingStatus(); // 연결 후 스트리밍 상태 확인
      })
      .catch(err => {
        console.error('OBS WebSocket 연결 실패:', err);
        setIsConnected(false); // 연결 실패 시 상태 업데이트
      });
  };

  // OBS의 현재 스트리밍 상태 확인 (OBS WebSocket v5)
  const checkStreamingStatus = () => {
    if (obs.current) {
      // 스트리밍 아웃풋 상태 확인
      obs.current.call('GetOutputStatus', { outputName: 'simple_stream' }) // 아웃풋 이름을 'simple_stream'으로 지정
        .then(response => {
          if (response.outputActive) { // outputActive가 true면 스트리밍 중
            console.log('OBS는 현재 스트리밍 중입니다.');
            setIsStreaming(true); // 스트리밍 중인 상태로 업데이트
          } else {
            console.log('OBS는 스트리밍 중이 아닙니다.');
            setIsStreaming(false); // 스트리밍이 아닌 상태로 업데이트
          }
        })
        .catch(err => {
          console.error('OBS 스트리밍 상태 확인 실패:', err);
        });
    }
  };

   // OBS에서 스트리밍 상태 이벤트 감지
   const listenToStreamEvents = () => {
    if (obs.current) {
      obs.current.on('StreamStateChanged', (data) => {
        if (data.outputActive) {
          console.log('OBS 스트리밍이 시작되었습니다.');
          setIsStreaming(true); // 스트리밍 시작 상태로 변경
        } else {
          console.log('OBS 스트리밍이 중지되었습니다.');
          setIsStreaming(false); // 스트리밍 중지 상태로 변경
        }
      });
    }
  };

  // OBS WebSocket 연결 및 재연결 처리
  useEffect(() => {
    connectOBS(); // 컴포넌트 마운트 시 OBS 연결
    listenToStreamEvents();

    // 컴포넌트 언마운트 시 WebSocket 연결 해제 및 이벤트 리스너 제거
    return () => {
      if (obs.current) {
        obs.current.off('StreamStateChanged'); // 이벤트 리스너 제거
        obs.current.disconnect();
        console.log('OBS WebSocket 연결 해제 및 리스너 제거');
      }
    };
  }, []);

  useEffect(() => {
    const fetchChannelInfo = async () => {
      try {
        // API 호출로 streaming 정보 가져오기
        const token = localStorage.getItem('ACCESS_TOKEN');
        const response = await axios.get(`http://localhost:8080/specialAuction/channelInfo/${auction.auctionIndex}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        const channelInfoDto = response.data.item;

        // 서버와 스트림 키 업데이트
        setChannelInfo({
          streamKey: channelInfoDto.streamKey,
          serverURL: channelInfoDto.publishUrl,
          channelStatus: channelInfoDto.channelStatus,
          cdnStatusName: channelInfoDto.cdnStatusName,
          streamingUrl: channelInfoDto.serviceUrlList
        });
      } catch (error) {
        console.error('스트리밍 정보 가져오기 실패:', error);
      }
    };
    fetchChannelInfo();
  }, [auction.auctionIndex]);

  const { messages, inputMessage, setInputMessage, sendMessage, currentPrices } = webSocketProps;
  const messagesEndRef = useRef(null);
  const auctionStartTime = new Date(auction.startingLocalDateTime);
  const formattedAuctionStartTime = auctionStartTime.toLocaleString('ko-KR'); // 한국어 포맷으로 변경
  const formattedBidIncrement = auction.bidIncrement?.toLocaleString() || '0'; // null/undefined 대비 기본값 처리
  const formattedStartingPrice = auction.startingPrice?.toLocaleString() || '0';
  const formattedCurrentPrice = (currentPrices[auction.auctionIndex] || auction.startingPrice)?.toLocaleString() || '0';
  const formattedRemainingTime = getFormattedRemainingTime(remainingTime);

  // 메시지 스크롤을 맨 아래로 이동
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  // 메시지를 전송하는 핸들러
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') sendMessage();
  };


  return (
    <div className="SAoverlay">
      <div className='SAtotalPopup'>
        <div className="SAbuyerPopup">
          <div className="SAsellerLiveAuctionHeader">
            <h3>Live Off</h3>
            <h1>판매자</h1>
          </div>
          <div className='SAauctionContainer'>
            <div className='SAsellerTotalBox'>
              <div className='SAsellerViewBox'>
                <div className="SAsellerAuctionContentBox">
                  <div className="SAsellerProductSection">
                    <div className='SAsoundBttn'>
                      <img id='SAmikeOffIcon' src='/images/mike_off_icon.svg' alt="Mic Off" />
                      <img id='SAmikeOnIcon' src='/images/mike_on_icon.svg' alt="Mic On" />
                    </div>
                    {/* isStreaming 상태에 따라 UI를 다르게 렌더링 */}
                      {isStreaming ? (
                        // OBS 방송이 시작된 경우, HLS 스트리밍을 VideoSection을 통해 재생
                        <VideoSection streamingUrl={channelInfo.streamingUrl} />
                      ) : (
                        // OBS 방송이 시작되지 않은 경우 대기 중 화면을 표시
                        <div
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            height: '400px',
                            color: '#FFFFFF',
                            backgroundColor: '#000000',
                            fontSize: '1.5rem',
                          }}
                        >
                          방송이 아직 시작되지 않았습니다.
                        </div>
                      )}
                  </div>
                  <div className="SAsellerAuctionDetails">
                    <h2>{auction.productName}</h2>
                    <div className='SAsellerAuctionDetailsBox'>
                      <div className='SAsellerAuctionContentsTitle'>
                        <p>경매 시작까지<br />남은 시간</p>
                        <p>입찰단위</p>
                        <p>경매 시작가</p>
                        <p>현재가</p>
                        <p>대기중인 사용자</p>
                      </div>
                      <div className='SAsellerAuctionContentsValue'>
                        <div>
                          <p id='SAsellerAuctionStartRemainTimeDiff'>{formattedRemainingTime}</p>
                          <p id='SAsellerAuctionStartRemainTime'>({formattedAuctionStartTime})</p>
                        </div>
                        <p>{formattedBidIncrement}원</p>
                        <p>{formattedStartingPrice}원</p>
                        <p>{formattedCurrentPrice}원</p>
                        <p>20,584</p>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="SAsellerStreamingInfo">
                  <p id='SAsellerStreamingNote'>* 경매 시간이 되면 자동으로 스트리밍을 시작합니다.</p>
                  <p id='SAsellerStreamingNoteContent'>실시간 스트리밍을 시작하려면 스트리밍 소프트웨어에서 동영상 전송을 시작하세요.</p>
                </div>
                <SAsellerSteamingBox channelInfo={channelInfo}/>
              </div>
              {/* Chat Section */}
              <div className="SAsellerChatContainer">
                <div className="SAchatSection">
                  <div>
                    <ul>
                      {webSocketProps.messages[auction?.auctionIndex]?.map((msg, index) => (
                        <li key={index}>{msg.senderNickname}: {msg.chatMessage}</li>
                      )) || <li>메시지가 없습니다.</li>}
                      <div ref={messagesEndRef} />
                    </ul>
                  </div>
                </div>
                <div>
                  <input
                    className='SAchatInput'
                    type="text"
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyDown={handleKeyPress}
                    placeholder="메시지를 입력하세요..."
                  />
                </div>
              </div>
            </div>
          </div>
          <button className="SAtotalBoxCloseButton" onClick={closeSellerPage}>
            <img src='/images/white_close_button_icon.svg' alt="close button" />
          </button>
        </div>
      </div>
    </div>
  );
}
export default SellerAuctionScreen;