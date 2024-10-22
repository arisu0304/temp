package bibid.livestation.service;

import bibid.livestation.dto.LiveStationChannelDTO;
import bibid.livestation.dto.LiveStationServiceUrlDTO;
import bibid.livestation.dto.LiveStationUrlDTO;
import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.entity.LiveStationServiceUrl;
import bibid.livestation.repository.LiveStationChannelRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LiveStationPoolManager {


    private final LiveStationService liveStationService;
    private final LiveStationChannelRepository channelRepository;
    private Queue<LiveStationChannel> channelPool = new LinkedList<>();  // 채널을 메모리에 관리
    private final RedisTemplate<String, LiveStationChannel> redisTemplate; // Redis 사용

    @PostConstruct
    public void initializePool() {

        List<LiveStationChannel> existingChannels = channelRepository.findAll();

        if (existingChannels.isEmpty()) {
            System.out.println("DB에 채널이 없습니다. API로부터 채널을 가져옵니다.");
            List<LiveStationChannelDTO> preCreatedChannelDTOList = liveStationService.getChannelList();

            for (LiveStationChannelDTO preCreatedChannelDTO : preCreatedChannelDTOList) {
                LiveStationChannel preCreatedChannel = configureChannel(preCreatedChannelDTO);
                channelRepository.save(preCreatedChannel);
            }
        }
    }

    private LiveStationChannel configureChannel(LiveStationChannelDTO preCreatedChannelDTO) {
        String channelId = preCreatedChannelDTO.getChannelId();
        String cdnStatusName = preCreatedChannelDTO.getCdnStatusName();
        String channelStatus = preCreatedChannelDTO.getChannelStatus();

        LiveStationChannel preCreatedChannel = preCreatedChannelDTO.toEntity();

        if (cdnStatusName.equals("RUNNING")) {
            List<LiveStationServiceUrl> serviceUrlList = liveStationService.getServiceURL(channelId, "GENERAL")
                    .stream()
                    .map(liveStationUrlDTO -> LiveStationServiceUrl.builder()
                            .liveStationChannel(preCreatedChannel)
                            .serviceUrl(liveStationUrlDTO.getUrl())
                            .build()
                    )
                    .toList();
            preCreatedChannel.setServiceUrlList(serviceUrlList);
        }

        preCreatedChannel.setAvailable(cdnStatusName.equals("RUNNING") && channelStatus.equals("READY"));

        return preCreatedChannel;
    }


    public LiveStationChannel allocateChannel() {

        LiveStationChannel allocatedChannel = channelRepository.findFirstByIsAvailableTrue()
                .orElseThrow(() -> new RuntimeException("사용 가능한 채널이 없습니다."));
        
        /* 
        * 이 부분에는 만약 가용 채널이 없다면 livestationService의 createChannel 메소드를 호출하고
        * cdn 생성 완료까지 기다린 후 DB에 반영하고
        * 이를 다시 꺼내오는 방식의 로직을 추가
        * */

        allocatedChannel.setChannelStatus("PUBLISH");
        allocatedChannel.setAvailable(false);
        
        channelRepository.save(allocatedChannel);

        return allocatedChannel;
    }

    public void releaseChannel(LiveStationChannel channel) {

        channel.setChannelStatus("READY");
        channel.setAvailable(true);

        channelRepository.save(channel);
    }

}
