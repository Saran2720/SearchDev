package com.searchDev.SearchDev.DTO;

import com.searchDev.SearchDev.Model.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record MessageResDTO(
        UUID id,
        UUID senderId,
        String senderUsername,
        UUID receiverId,
        String receiverUsername,
        String content,
        String formattedDate,
        String formattedTime
) {
    public static MessageResDTO fromEntity(Message message) {
        LocalDateTime createdAt = message.getCreatedAt();
        String formattedDate = formatDate(createdAt);
        String formattedTime = formatTime(createdAt);
        
        return new MessageResDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getReceiver().getId(),
                message.getReceiver().getUsername(),
                message.getContent(),
                formattedDate,
                formattedTime
        );
    }
    
    private static String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter dayOfMonthFormatter = DateTimeFormatter.ofPattern("d");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        
        String dayOfWeek = dateTime.format(dayFormatter);
        String dayOfMonth = dateTime.format(dayOfMonthFormatter);
        String year = dateTime.format(yearFormatter);
        
        return String.format("(%s, %s, %s)", dayOfWeek, dayOfMonth, year);
    }
    
    private static String formatTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        
        String amPm;
        int displayHour;
        
        if (hour == 0) {
            displayHour = 12;
            amPm = "am";
        } else if (hour < 12) {
            displayHour = hour;
            amPm = "am";
        } else if (hour == 12) {
            displayHour = 12;
            amPm = "pm";
        } else {
            displayHour = hour - 12;
            amPm = "pm";
        }
        
        return String.format("%d.%02d%s", displayHour, minute, amPm);
    }
}
