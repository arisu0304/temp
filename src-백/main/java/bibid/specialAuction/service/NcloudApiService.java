package bibid.specialAuction.service;

import bibid.dto.NcloudApiRequestDTO;

public interface NcloudApiService {
    String callNcloudApi(NcloudApiRequestDTO requestDTO);
}
