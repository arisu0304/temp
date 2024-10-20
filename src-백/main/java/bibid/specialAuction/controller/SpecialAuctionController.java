package bibid.specialAuction.controller;

import bibid.dto.AuctionDto;
import bibid.dto.ResponseDto;
import bibid.dto.StreamingDto;
import bibid.livestation.dto.LiveStationInfoDTO;
import bibid.livestation.dto.LiveStationUrlDTO;
import bibid.livestation.service.LiveStationService;
import bibid.specialAuction.service.SpecialAuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/specialAuction")
@RequiredArgsConstructor
@Slf4j
public class SpecialAuctionController {

    private final SpecialAuctionService specialAuctionService;
    private final LiveStationService liveStationService;

    @GetMapping("/auctions")
    public ResponseEntity<?> getAuctionsByType(
            @RequestParam("auctionType") String auctionType,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // 응답 데이터를 담을 Map 선언
        ResponseDto<AuctionDto> responseDto = new ResponseDto<>();

        try {

            Page<AuctionDto> auctionDtoList = specialAuctionService.findAuctionsByType(auctionType, pageable);

            if (auctionDtoList.isEmpty()) {
                log.info("No auctions found for auctionType: {}", auctionType);
            } else {
                log.info("Found auctions: {}", auctionDtoList.getContent());
            }

            responseDto.setPageItems(auctionDtoList);
            responseDto.setStatusCode(HttpStatus.OK.value());
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok(responseDto);
        } catch(Exception e) {
            log.error("getAuctions error: {}", e.getMessage());
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setStatusMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseDto);
        }
    }

    @GetMapping("/streaming")
    public ResponseEntity<?> findStreamingByAuctionIndex(
            @RequestParam("auctionIndex") Long auctionIndex) {

        ResponseDto<StreamingDto> responseDto = new ResponseDto<>();

        try {

            StreamingDto streamingDto = specialAuctionService.findStreamingByAuctionIndex(auctionIndex);

            responseDto.setItem(streamingDto);
            responseDto.setStatusCode(HttpStatus.OK.value());
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("getAuctions error: {}", e.getMessage());
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setStatusMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseDto);
        }

    }

    @GetMapping("/streamingInfo")
    public ResponseEntity<?> findStreamingByAuctionIndex(
            @RequestParam("channelId") String channelId) {

        ResponseDto<LiveStationInfoDTO> responseDto = new ResponseDto<>();

        try {

            LiveStationInfoDTO liveStationInfoDTO = liveStationService.getChannelInfo(channelId);

            responseDto.setItem(liveStationInfoDTO);
            responseDto.setStatusCode(HttpStatus.OK.value());
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("getAuctions error: {}", e.getMessage());
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setStatusMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseDto);
        }

    }

    @GetMapping("/serviceUrl")
    public ResponseEntity<?> findStreamingUrlByAuctionIndex(
            @RequestParam("channelId") String channelId) {

        ResponseDto<LiveStationUrlDTO> responseDto = new ResponseDto<>();

        try {

            List<LiveStationUrlDTO> liveStationUrlDTOList = liveStationService.getServiceURL(channelId, "GENERAL");

            responseDto.setItems(liveStationUrlDTOList);
            responseDto.setStatusCode(HttpStatus.OK.value());
            responseDto.setStatusMessage("ok");

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("getAuctions error: {}", e.getMessage());
            responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseDto.setStatusMessage(e.getMessage());
            return ResponseEntity.internalServerError().body(responseDto);

        }
    }

}
