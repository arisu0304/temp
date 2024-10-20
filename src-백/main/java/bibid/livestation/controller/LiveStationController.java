package bibid.livestation.controller;

import bibid.livestation.domain.LiveStationChannel;
import bibid.livestation.service.LiveStationPoolManager;
import bibid.livestation.service.LiveStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/liveStation")
@RequiredArgsConstructor
@Slf4j
public class LiveStationController {

    private final LiveStationPoolManager liveStationPoolManager;
    private final LiveStationService liveStationService;

    // 라이브 채널 리스트 조회
    @GetMapping("/channels")
    public ResponseEntity<?> getChannels() {
        List<LiveStationChannel> channels = liveStationPoolManager.getAvailableChannels();
        return ResponseEntity.ok(channels);
    }

    // 새로운 채널 생성 요청 (옥션과 무관하게 라이브 스트리밍용 채널을 생성할 때)
    @PostMapping("/create")
    public ResponseEntity<?> createChannel(@RequestParam String channelName) {
        String newChannelId = liveStationService.createChannel(channelName);
        return ResponseEntity.ok("새로운 채널이 생성되었습니다. 채널 ID: " + newChannelId);
    }

    // 라이브 채널 할당
    @PostMapping("/allocate")
    public ResponseEntity<?> allocateChannel() {
        Optional<LiveStationChannel> optionalChannel = liveStationPoolManager.getAvailableChannel();
        if (optionalChannel.isPresent()) {
            LiveStationChannel channel = optionalChannel.get();
            return ResponseEntity.ok("할당된 채널 ID: " + channel.getChannelId());
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("사용 가능한 채널이 없습니다.");
        }
    }

    // 채널 반납
    @PostMapping("/release")
    public ResponseEntity<String> releaseChannel(@RequestParam String channelId) {
        LiveStationChannel channel = liveStationPoolManager.getChannelById(channelId);
        liveStationPoolManager.releaseChannel(channel);
        return ResponseEntity.ok("채널이 반납되었습니다.");
    }

}
