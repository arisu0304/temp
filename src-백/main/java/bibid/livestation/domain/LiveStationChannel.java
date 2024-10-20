package bibid.livestation.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class LiveStationChannel {
    private final String channelId;
    private final int cdnInstanceNo;
    private boolean isAvailable = true ;  // 사용 가능 여부
}
