package com.searchDev.SearchDev.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;
    private String password;

    public UserDTO(String name, String email, String password) {
        this.username = name;
        this.email = email;
        this.password = password;
    }
}
