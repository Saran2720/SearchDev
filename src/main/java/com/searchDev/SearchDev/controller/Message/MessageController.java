package com.searchDev.SearchDev.controller.Message;
import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.MessageReqDTO;
import com.searchDev.SearchDev.DTO.MessageResDTO;
import com.searchDev.SearchDev.Model.Message;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.MessageService.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<ApiResDTO<MessageResDTO>> send(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody MessageReqDTO messageReqDTO
    ) {
        String senderEmail = userPrincipal.getUsername();
        Message message = messageService.sendMessage(senderEmail, messageReqDTO.getReceiverId(), messageReqDTO.getContent());
        MessageResDTO messageResDTO = MessageResDTO.fromEntity(message);

        ApiResDTO<MessageResDTO> response = ApiResDTO.<MessageResDTO>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Message sent successfully")
                .data(messageResDTO)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/inbox")
    public ResponseEntity<ApiResDTO<List<MessageResDTO>>> getInbox(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) { 
        List<MessageResDTO> inbox = messageService.getInbox(userPrincipal.getUsername());

        ApiResDTO<List<MessageResDTO>> response = ApiResDTO.<List<MessageResDTO>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Inbox messages fetched successfully")
                .data(inbox)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
