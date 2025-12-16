package com.searchDev.SearchDev.DTO.cacheWrapper;
import com.searchDev.SearchDev.DTO.UserDetailsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageUsersCache {
    private List<UserDetailsDTO> users;
    private long totalElements;
}
