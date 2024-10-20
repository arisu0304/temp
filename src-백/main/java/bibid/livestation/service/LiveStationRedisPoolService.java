//package bibid.livestation.service;
//
//@Service
//public class LiveStationRedisPoolService {
//    private final RedisTemplate<String, LiveStationChannel> redisTemplate;
//    private final String POOL_KEY = "LiveStationPool";
//
//    @Autowired
//    public LiveStationRedisPoolService(RedisTemplate<String, LiveStationChannel> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    public Optional<LiveStationChannel> allocateAvailableChannel() {
//        List<LiveStationChannel> pool = redisTemplate.opsForList().range(POOL_KEY, 0, -1);
//        Optional<LiveStationChannel> availableChannel = pool.stream()
//                .filter(LiveStationChannel::isAvailable)
//                .findFirst();
//
//        availableChannel.ifPresent(channel -> {
//            channel.setAvailable(false);
//            redisTemplate.opsForList().remove(POOL_KEY, 1, channel);
//            redisTemplate.opsForList().rightPush(POOL_KEY, channel);
//        });
//
//        return availableChannel;
//    }
//
//    public void releaseChannel(LiveStationChannel channel) {
//        channel.setAvailable(true);
//        redisTemplate.opsForList().remove(POOL_KEY, 1, channel);
//        redisTemplate.opsForList().rightPush(POOL_KEY, channel);
//    }
//}
