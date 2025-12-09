package com.popspot.popupplatform.domain.reservation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PopupBlock {

    private Long pbId;
    private Long popId;
    private LocalDateTime pbDateTime;
}
