import { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useSelector } from 'react-redux';
const useWebSocket = (auctionIndex, isChatClosed, setIsChatClosed) => {
  const loginMemberNickname = useSelector((state) => state.memberSlice.nickname);
  const [messages, setMessages] = useState({});
  const [inputMessage, setInputMessage] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [currentPrices, setCurrentPrices] = useState({});
  const [bidAmounts, setBidAmounts] = useState({});
  // WebSocket 연결 함수
  useEffect(() => {
    if (!auctionIndex || isChatClosed) return;
    const connectWebSocket = () => {
      let token = localStorage.getItem('ACCESS_TOKEN');
      const socket = new SockJS('http://localhost:8080/ws');
      const client = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        onConnect: () => {
          setConnected(true);
          console.log("웹소켓 연결되었습니다.");
          setMessages((prev) => ({ ...prev, [auctionIndex]: [] }));
          // 채팅 구독
          client.subscribe(`/topic/public/${auctionIndex}`, (message) => {
            const newMessage = JSON.parse(message.body);
            console.log("Message received:", newMessage);
            setMessages((prevMessages) => ({
              ...prevMessages,
              [auctionIndex]: [...(prevMessages[auctionIndex] || []), newMessage],
            }));
          });
          // 경매 구독
          client.subscribe(`/topic/auction/${auctionIndex}`, (message) => {
              const auctionInfo = JSON.parse(message.body);
              console.log("Bid received:", auctionInfo);
              if (auctionInfo.bidAmount) {
                setCurrentPrices((prev) => ({
                  ...prev,
                  [auctionIndex]: auctionInfo.bidAmount,
                }));
              }
          });
          // 방에 입장할 때 메시지 초기화
          // 입장 메시지 전송
          sendJoinMessage(client, auctionIndex);
        },
        onStompError: (error) => {
          console.error("WebSocket error:", error);
        },
      });
      client.activate();
      setStompClient(client);
      setIsChatClosed(true);
    };
    connectWebSocket();
    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, [auctionIndex, isChatClosed]);
  // 메시지 전송 함수
  const sendMessage = () => {
    if (connected && stompClient) {
      stompClient.publish({
        destination: `/app/chat.sendMessage/${auctionIndex}`,
        body: JSON.stringify({
          senderNickname: loginMemberNickname,
          chatMessage: inputMessage,
        }),
      });
      setInputMessage(''); // 입력 초기화
    }
  };
  // 입장 메시지 전송
  const sendJoinMessage = (client, auctionIndex) => {
    const joinMessage = `${loginMemberNickname}님이 입장하셨습니다.`;
    client.publish({
      destination: `/app/chat.sendMessage/${auctionIndex}`,
      body: JSON.stringify({
        senderNickname: loginMemberNickname,
        chatMessage: joinMessage,
      }),
    });
    // const welcomeMessage = '채팅방에 입장하셨습니다. 환영합니다.';
    // setMessages((prevMessages) => [
    //   ...prevMessages,
    //   { chatMessage: welcomeMessage, senderNickname: '시스템' },
    // ]);
  };
  // 퇴장 메시지 전송
  const sendLeaveMessage = () => {
    if (connected && stompClient) {
      const leaveMessage = `${loginMemberNickname}님이 퇴장하셨습니다.`;
      stompClient.publish({
        destination: `/app/chat.sendMessage/${auctionIndex}`,
        body: JSON.stringify({
          senderNickname: '시스템',
          chatMessage: leaveMessage,
        }),
      });
    }
  };
  const handleBidSubmit = (auctionIndex, bidAmount) => {
    if (bidAmount > currentPrices[auctionIndex]) {
      setBidAmounts((prev) => ({
        ...prev,
        [auctionIndex]: bidAmount,
      }));
      stompClient.publish({
        destination: `/app/auction.bid/${auctionIndex}`,
        body: JSON.stringify({ auctionIndex, bidAmount }),
      });
    } else {
      alert('입찰가는 현재가보다 높아야 합니다.');
      console.log(bidAmount + " : 입찰가");
      console.log(currentPrices[auctionIndex] + " : 현재가");
    }
  };
  return {
    messages,
    inputMessage,
    setInputMessage,
    sendMessage,
    sendLeaveMessage,
    handleBidSubmit,
    currentPrices,
    setCurrentPrices,
    bidAmounts,
    setBidAmounts
  };
};
export default useWebSocket;