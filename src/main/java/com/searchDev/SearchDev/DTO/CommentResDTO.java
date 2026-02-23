package com.searchDev.SearchDev.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResDTO {
    private UUID commentId;

    // author (for profile redirect)
    private UUID userId;
    private String username;

    // reply info (null for root comments)
    private UUID parentCommentId;
    private UUID repliedToUserId;
    private String repliedToUsername;

    private String content;
    private String createdAt; // ISO-ish string (LocalDateTime#toString)

    // useful for threading on client
    private UUID rootCommentId;
}


