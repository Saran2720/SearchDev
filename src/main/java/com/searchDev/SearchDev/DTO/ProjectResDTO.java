package com.searchDev.SearchDev.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResDTO {
    private UUID projectId;
    private UUID ownerID;
    private String name;
    private String description;
    private List<String> techStack;
    private Map<String,Object> links;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @Override
    public String toString() {
        return "ProjectResDTO{" +
                "projectId=" + projectId +
                ", ownerID=" + ownerID +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", techStack=" + techStack +
                ", links=" + links +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}

