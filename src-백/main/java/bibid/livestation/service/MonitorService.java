//package bibid.livestation.service;
//
//public class MonitorService {
//    @Scheduled(fixedRate = 600000)  // 10분마다 실행
//    public void monitorPoolStatus() {
//        List<LiveStationChannel> pool = liveStationPool.getAllChannels();
//
//        long availableCount = pool.stream().filter(LiveStationChannel::isAvailable).count();
//        log.info("현재 사용 가능한 CDN 채널 수: {}", availableCount);
//
//        if (availableCount < 5) {  // 사용 가능한 리소스가 5개 미만이면 추가 생성
//            log.info("사용 가능한 리소스가 부족하므로 추가 생성 중...");
//            createNewLiveStationAndCDN();
//        }
//    }
//
//}
