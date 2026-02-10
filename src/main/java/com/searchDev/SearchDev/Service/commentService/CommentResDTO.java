package com.searchDev.SearchDev.Service.commentService;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResDTO {
    private UUID commentId;
    private UUID userId;
    private String userName;
    private UUID parentId;
    private UUID rootId;
    private String content;
    private String time;

    
}
