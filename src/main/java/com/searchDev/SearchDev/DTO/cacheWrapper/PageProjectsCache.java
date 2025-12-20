package com.searchDev.SearchDev.DTO.cacheWrapper;

import java.util.List;

import com.searchDev.SearchDev.DTO.ProjectResDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageProjectsCache {
    private List<ProjectResDTO> projects;
    private long totalElements;
}
