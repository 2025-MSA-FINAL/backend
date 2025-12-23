package com.popspot.popupplatform.dto.admin;

import lombok.Data;

@Data
public class CategoryValidationDTO {
    private String category;
    private int totalTags;
    private int matchedTags;
    private double accuracy; // matched / total
}
