package com.popspot.popupplatform.dto;

import com.popspot.popupplatform.dto.main.MainPopupCardDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MainPageResponse {
    private List<MainPopupCardDto> hero;
    private List<MainPopupCardDto> latest;
    private List<MainPopupCardDto> endingSoon;
}
