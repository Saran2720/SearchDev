package com.searchDev.SearchDev.DTO;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class UserDetailsDTO {
    private UUID id;
    private String email;
    private String username;
    private String bio;
    private List<String> skills;
    private Map<String,Object> links;
    private String role;
    private String experience;
    private String company;
}
