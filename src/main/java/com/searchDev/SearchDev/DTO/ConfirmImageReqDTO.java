package com.searchDev.SearchDev.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmImageReqDTO {

    @NotBlank
    private String fileKey;
}
