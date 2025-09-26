package com.searchDev.SearchDev.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ProjectReqDTO {
    private String projectName;
    private String description;
    private List<String> techStack;
    private Map<String,Object> links;
}
