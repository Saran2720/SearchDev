package com.searchDev.SearchDev.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuthResDTO {
    private String accessToken;
    private String refreshToken;
}
