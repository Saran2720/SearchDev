package com.searchDev.SearchDev.controller.Message;

import com.searchDev.SearchDev.DTO.MessageReqDTO;
import com.searchDev.SearchDev.DTO.MessageResDTO;
import com.searchDev.SearchDev.Model.Message;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.MessageService.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<MessageResDTO> send(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody MessageReqDTO messageReqDTO){
        String senderEmail = userPrincipal.getUsername();
        Message message=messageService.sendMessage(senderEmail,messageReqDTO.getReceiverId(),messageReqDTO.getContent());
        return ResponseEntity.ok(MessageResDTO.fromEntity(message));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<MessageResDTO>> getInbox(@AuthenticationPrincipal UserPrincipal userPrincipal){
        List<MessageResDTO> messageResDTOList= messageService.getInbox(userPrincipal.getUsername());
        return ResponseEntity.ok(messageResDTOList);
    }
}
