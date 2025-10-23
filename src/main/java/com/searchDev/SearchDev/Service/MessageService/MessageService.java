package com.searchDev.SearchDev.Service.MessageService;

import com.searchDev.SearchDev.DTO.MessageResDTO;
import com.searchDev.SearchDev.Model.Message;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.MessageRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MessageRepo messageRepo;

    public Message sendMessage(String senderEmail, UUID receiverId, String content){
        Users sender = userRepo.findByEmail(senderEmail);
        if(sender==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found with email: " + senderEmail);
        }

        Users receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

      return  messageRepo.save(message);
    }

    public List<MessageResDTO> getInbox(String email) {
        Users receiver = userRepo.findByEmail(email);
        UUID receiverID = receiver.getId();
        List<Message> messages = messageRepo.findByReceiverIdOrderByCreatedAtDesc(receiverID);

        return messages.stream()
                .map(MessageResDTO::fromEntity)
                .toList();
    }


}
