package com.searchDev.SearchDev.DTO;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MessageReqDTO {
    private UUID receiverId;
    private String content;
}
