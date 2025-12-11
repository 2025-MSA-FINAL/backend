package com.popspot.popupplatform.dto.global;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDto {
    private Long userId;
    private String role;
    private String status;
}
