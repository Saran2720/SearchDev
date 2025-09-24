package com.searchDev.SearchDev.DTO;


import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public String toString() {
        return "UserDetailsDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                ", skills=" + skills +
                ", links=" + links +
                ", role='" + role + '\'' +
                ", experience='" + experience + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}
