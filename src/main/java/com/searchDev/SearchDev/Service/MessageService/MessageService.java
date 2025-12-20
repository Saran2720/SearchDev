package com.searchDev.SearchDev.Service.MessageService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.searchDev.SearchDev.DTO.MessageResDTO;
import com.searchDev.SearchDev.ExceptionHandler.ResourceNotFoundException;
import com.searchDev.SearchDev.Model.Message;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.MessageRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.RedisService.RedisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepo messageRepo;

    private RedisService redisService;
    private UserRepo userRepo;

    @Autowired
    MessageService(RedisService redisService, UserRepo userRepo) {
        this.redisService = redisService;
        this.userRepo = userRepo;
    }

    // Helper method to get email by receiver ID, safely handling case where
    // receiver does not exist
    private String getReceiverEmailById(UUID receiverId) throws ResourceNotFoundException {
        Users receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with ID: " + receiverId));
        return receiver.getEmail();
    }

    // send message to the reciver
    public Message sendMessage(String senderEmail, UUID receiverId, String content) throws ResourceNotFoundException {
        Users sender = userRepo.findByEmail(senderEmail);
        if (sender == null) {
            throw new ResourceNotFoundException("Sender not found with email: " + senderEmail);
        }
        // get the eamil for the receiver to delte the chached data if present
        String email = getReceiverEmailById(receiverId);
        String key = "message:inbox" + email;
        List<MessageResDTO> cachedMsg = redisService.get(key, new TypeReference<List<MessageResDTO>>() {
        });
        if (cachedMsg != null) {
            redisService.delete(key);
        }

        Users receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with ID: " + receiverId));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return messageRepo.save(message);
    }

    // get the user inbox message and cache the data
    public List<MessageResDTO> getInbox(String email) throws ResourceNotFoundException {
        String key = "message:inbox" + email;
        List<MessageResDTO> cached = redisService.get(key, new TypeReference<List<MessageResDTO>>() {
        });
        if (cached != null) {
            return cached;
        }
        System.out.println("hit for message getting");

        Users receiver = userRepo.findByEmail(email);
        if (receiver == null) {
            throw new ResourceNotFoundException("Receiver not found with email: " + email);
        }
        UUID receiverID = receiver.getId();
        List<Message> messages = messageRepo.findByReceiverIdOrderByCreatedAtDesc(receiverID);
        List<MessageResDTO> dto = messages.stream()
                .map(MessageResDTO::fromEntity)
                .toList();
        redisService.save(key, dto, Duration.ofHours(1));
        return dto;
    }
}
