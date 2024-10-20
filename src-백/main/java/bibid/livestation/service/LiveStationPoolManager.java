package bibid.livestation.service;

import bibid.livestation.domain.LiveStationChannel;
import bibid.livestation.dto.LiveStationChannelDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LiveStationPoolManager {

    private final LiveStationService liveStationService;
    private Queue<LiveStationChannel> channelPool;

    @PostConstruct
    public void initializePool() {
        channelPool = new LinkedList<>();  // 큐로 변경

        // API를 통해 콘솔에서 미리 만들어진 채널 정보를 가져옴
        List<LiveStationChannel> preCreatedChannels = liveStationService.getChannelList();

        // API에서 받은 채널 정보를 메모리 풀에 초기화 (큐에 추가)
        for (LiveStationChannel liveStationChannel : preCreatedChannels) {
            channelPool.offer(liveStationChannel);  // 큐에 채널 추가
        }
    }

    // 사용 가능한 채널을 큐에서 가져옴
    public Optional<LiveStationChannel> getAvailableChannel() {
        // 큐에서 채널을 가져옴 (FIFO)
        LiveStationChannel channel = channelPool.poll();  // 큐에서 제거하며 가져옴

//        // 만약 큐가 비었을 경우 새 채널 생성 (필요한 경우)
//        if (channel == null) {
//            // 새 채널 생성 로직
//            String newChannelId = liveStationService.createChannel("상품 이름");  // 새로운 채널 생성
//            channelPool.offer(channel);  // 새 채널을 다시 큐에 추가
//        }

        return Optional.ofNullable(channel);
    }

    // 채널 반납 (사용 가능 상태로 다시 큐에 추가)
    public void releaseChannel(LiveStationChannel channel) {
        channel.setAvailable(true);  // 다시 사용 가능 상태로 변경
        channelPool.offer(channel);  // 큐에 다시 추가
    }


    public List<LiveStationChannel> getAvailableChannels() {
        // 사용 가능한 채널들만 필터링
        return channelPool.stream()
                .filter(LiveStationChannel::isAvailable)  // 사용 가능한 채널만 선택
                .collect(Collectors.toList());  // 리스트로 반환
    }

    public LiveStationChannel getChannelById(String channelId) {
        // 특정 채널 ID로 채널을 찾아 반환
        return channelPool.stream()
                .filter(channel -> channel.getChannelId().equals(channelId))  // ID가 일치하는 채널 찾기
                .findFirst()  // 첫 번째 일치하는 채널을 찾음
                .orElseThrow(() -> new RuntimeException("채널 ID: " + channelId + "를 찾을 수 없습니다."));  // 없으면 예외 처리
    }
}


