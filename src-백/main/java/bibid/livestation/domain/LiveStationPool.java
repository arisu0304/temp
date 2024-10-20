package bibid.livestation.domain;

import java.util.List;
import java.util.Optional;

public class LiveStationPool {
    private List<LiveStationChannel> channelPool;

    public LiveStationPool(List<LiveStationChannel> initialChannels) {
        this.channelPool = initialChannels;
    }

    // 사용 가능한 채널을 찾음
    public Optional<LiveStationChannel> getAvailableChannel() {
        return channelPool.stream()
                .filter(LiveStationChannel::isAvailable)
                .findFirst();
    }

    // 채널을 사용 상태로 변경
    public void allocateChannel(LiveStationChannel channel) {
        channel.setAvailable(false);
    }

    // 채널을 사용 가능 상태로 변경 (방송 종료 후)
    public void releaseChannel(LiveStationChannel channel) {
        channel.setAvailable(true);
    }
}
