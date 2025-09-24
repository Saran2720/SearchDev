package com.searchDev.SearchDev.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileReqDTO {
    private String username;
    private String bio;
    private List<String> skills;
    private Map<String,Object> links;
    private String role;
    private String experience;
    private String company;
}
