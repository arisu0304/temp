package bibid.livestation.dto;

import bibid.livestation.entity.LiveStationChannel;
import bibid.livestation.entity.LiveStationServiceUrl;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiveStationServiceUrlDTO {

    private Long LiveStationServiceUrlIndex;

    private String serviceUrl;

    private Long liveStationChannelIndex;

    public LiveStationServiceUrl toEntity() {
        return LiveStationServiceUrl.builder()
                .LiveStationServiceUrlIndex(this.LiveStationServiceUrlIndex)
                .serviceUrl(this.serviceUrl)
                .LiveStationServiceUrlIndex(this.liveStationChannelIndex)
                .build();
    }

}
