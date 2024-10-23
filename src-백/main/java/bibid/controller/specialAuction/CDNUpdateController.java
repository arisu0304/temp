package bibid.controller.specialAuction;

import bibid.livestation.dto.LiveStationServiceUrlDTO;
import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.entity.LiveStationServiceUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CDNUpdateController {

    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/cdn.notify/{auctionIndex}")
    @SendTo("/topic/cdn-updates/{auctionIndex}")
    public List<LiveStationServiceUrlDTO> sendCdnUpdate(String auctionIndex, LiveStationChannel channel) {
        return channel.getServiceUrlList().stream()
                .map(LiveStationServiceUrl::toDto)
                .toList();
    }

}
