package bibid.livestation.scheduler;

import bibid.livestation.dto.LiveStationChannelDTO;
import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.entity.LiveStationServiceUrl;
import bibid.livestation.repository.LiveStationChannelRepository;
import bibid.livestation.service.LiveStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiveStationScheduler {

    private final LiveStationService liveStationService;
    private final LiveStationChannelRepository channelRepository;

    @Scheduled(fixedRate = 60 * 60 * 1000)  // 1시간 간격으로 실행
    public void updateChannelPool() {
        List<LiveStationChannelDTO> preCreatedChannelDTOList = liveStationService.getChannelList();

        for (LiveStationChannelDTO preCreatedChannelDTO : preCreatedChannelDTOList) {
            String channelId = preCreatedChannelDTO.getChannelId();
            String cdnStatusName = preCreatedChannelDTO.getCdnStatusName();
            String channelStatus = preCreatedChannelDTO.getChannelStatus();

            LiveStationChannel channelEntity = channelRepository.findByChannelId(channelId)
                    .orElseGet(preCreatedChannelDTO::toEntity);

            channelEntity.setCdnStatusName(cdnStatusName);
            channelEntity.setChannelStatus(channelStatus);

            if (cdnStatusName.equals("RUNNING")) {
                List<LiveStationServiceUrl> serviceUrlList = liveStationService.getServiceURL(channelId, "GENERAL")
                        .stream()
                        .map(liveStationUrlDTO -> LiveStationServiceUrl.builder()
                                .liveStationChannel(channelEntity)
                                .serviceUrl(liveStationUrlDTO.getUrl())
                                .build()
                        )
                        .toList();
                channelEntity.setServiceUrlList(serviceUrlList);
            }

            channelEntity.setAvailable(cdnStatusName.equals("RUNNING") && channelStatus.equals("READY"));

            channelRepository.save(channelEntity);
        }
    }

}
