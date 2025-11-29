package com.popspot.popupplatform.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistItemDto {

    private Long popupId;           // pop_id
    private String popupName;       // POPUPSTORE.pop_name
    private String popupThumbnail;  // POPUPSTORE.pop_thumbnail
    private String popupLocation;   // POPUPSTORE.pop_location

    private LocalDateTime startDate;   // POPUPSTORE.pop_start_date
    private LocalDateTime endDate;     // POPUPSTORE.pop_end_date

    private String popupStatus;        // POPUPSTORE.pop_status (진행중/종료 등)
}