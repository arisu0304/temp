package bibid.livestation.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LiveStationChannel {
    private String channelId;
    private String channelStatus;
    private int cdnInstanceNo;
    private String cdnStatusName;

    private String publishUrl;
    private String streamKey;
    private List<String> serviceUrlList;

    private boolean isAvailable;
}