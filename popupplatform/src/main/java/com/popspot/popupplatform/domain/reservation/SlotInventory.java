package com.popspot.popupplatform.domain.reservation;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotInventory {
    private Long ptsId;
    private LocalDate invDate;
    private Integer remainCapacity;
}
