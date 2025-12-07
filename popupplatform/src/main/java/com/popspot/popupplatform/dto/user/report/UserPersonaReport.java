package com.popspot.popupplatform.dto.user.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserPersonaReport {
    private Long userId;
    private String gender;
    private Integer birthYear;
    private Integer age;
    private String ageGroupLabel;
    private String periodLabel;

    private int totalViewCount;
    private int totalWishlistCount;
    private int totalReservationCount;

    private List<UserPersonaAxis> axes;
    private String summary;

    private List<UserPersonaPopupCard> similarTastePopups;
    private List<UserPersonaPopupCard> demographicPopups;

    private List<UserPersonaTagStat> topHashtags;
    private List<UserPersonaRegionStat> topRegions;
}
