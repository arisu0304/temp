//package bibid.livestation.service;
//
//import org.springframework.stereotype.Service;
//
//@Service
//public class LiveStationPoolService {
//    private LiveStationPool liveStationPool;
//
//    @Autowired
//    public LiveStationPoolService(LiveStationPool liveStationPool) {
//        this.liveStationPool = liveStationPool;
//    }
//
//    // 풀에서 사용 가능한 채널을 할당하는 메서드
//    public Optional<LiveStationChannel> allocateAvailableChannel() {
//        Optional<LiveStationChannel> availableChannel = liveStationPool.getAvailableChannel();
//        availableChannel.ifPresent(liveStationPool::allocateChannel);  // 사용 가능하면 할당
//        return availableChannel;
//    }
//
//    // 풀에 채널을 반납하는 메서드
//    public void releaseChannel(LiveStationChannel channel) {
//        liveStationPool.releaseChannel(channel);  // 사용이 끝난 후 반납
//    }
//
//    // 사용 가능한 채널이 없으면 새로운 채널을 생성하는 메서드
//    public LiveStationChannel createNewChannel() {
//        // 새로운 채널 및 CDN 생성 로직 추가
//        return createNewLiveStationAndCDN();
//    }
//}
