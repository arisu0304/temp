package bibid.livestation.service;

import bibid.livestation.domain.LiveStationChannel;
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
    private Map<Long, LiveStationChannel> assignedChannels = new HashMap<>();

    @PostConstruct
    public void initializePool() {
        channelPool = new LinkedList<>();  // 큐로 변경
        List<LiveStationChannel> preCreatedChannels = liveStationService.getChannelList();

        for (LiveStationChannel liveStationChannel : preCreatedChannels) {
            String channelStatus = liveStationChannel.getChannelStatus();
            String cdnStatus = liveStationChannel.getCdnStatusName();

            if(channelStatus.equals("READY") && cdnStatus.equals("RUNNING") ){
                liveStationChannel.setAvailable(true);
                channelPool.offer(liveStationChannel);
            }else {
                liveStationChannel.setAvailable(false);
                channelPool.offer(liveStationChannel);
                System.out.println("채널 " + liveStationChannel.getChannelId() + "은 사용할 수 없습니다. 채널 상태: " + channelStatus + ", CDN 상태: " + cdnStatus);
            }
        }
    }

    public LiveStationChannel getAvailableChannel() {

        LiveStationChannel channel = channelPool.poll();  // 큐에서 제거하며 가져옴

        // 만약 큐가 비었을 경우 새 채널 생성 로직 (필요한 경우)
//    if (channel == null) {
//        // 새 채널 생성 로직
//        String newChannelId = liveStationService.createChannel("상품 이름");  // 새로운 채널 생성
//        channel = new LiveStationChannel(newChannelId, ...);  // 새로 생성한 채널 객체
//        channelPool.offer(channel);  // 새 채널을 다시 큐에 추가
//    }
        return channel;
    }

    public void releaseChannel(LiveStationChannel channel) {

        channel.setAvailable(true);
        channelPool.offer(channel);
    }

}
