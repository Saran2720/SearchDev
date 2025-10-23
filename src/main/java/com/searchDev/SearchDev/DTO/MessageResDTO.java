package com.searchDev.SearchDev.DTO;

import com.searchDev.SearchDev.Model.Message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResDTO(
        UUID id,
        UUID senderId,
        String senderUsername,
        UUID receiverId,
        String receiverUsername,
        String content,
        LocalDateTime sentAt
) {
    public static MessageResDTO fromEntity(Message message) {
        return new MessageResDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
