package com.searchDev.SearchDev.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class MessageReqDTO {
    private UUID receiverId;
    private String content;
}
