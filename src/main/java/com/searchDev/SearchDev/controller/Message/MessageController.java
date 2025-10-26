package com.searchDev.SearchDev.controller.Message;

import com.searchDev.SearchDev.DTO.ApiResDTO;
import com.searchDev.SearchDev.DTO.MessageReqDTO;
import com.searchDev.SearchDev.DTO.MessageResDTO;
import com.searchDev.SearchDev.Model.Message;
import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Service.MessageService.MessageService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<ApiResDTO<MessageResDTO>> send(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody MessageReqDTO messageReqDTO){

        try{
            String senderEmail = userPrincipal.getUsername();
            Message message=messageService.sendMessage(senderEmail,messageReqDTO.getReceiverId(),messageReqDTO.getContent());
            MessageResDTO messageResDTO=MessageResDTO.fromEntity(message);
            ApiResDTO<MessageResDTO> apiResDTO = ApiResDTO.<MessageResDTO>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Message send successfully")
                    .data(messageResDTO)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(apiResDTO);
        }catch(Exception e){
            ApiResDTO<MessageResDTO> errorResponse = ApiResDTO.<MessageResDTO>builder()
                    .success(false)
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/inbox")
    public ResponseEntity<ApiResDTO<List<MessageResDTO>>> getInbox(@AuthenticationPrincipal UserPrincipal userPrincipal){
        try{
            List<MessageResDTO> messageResDTOList= messageService.getInbox(userPrincipal.getUsername());
            ApiResDTO<List<MessageResDTO>> listApiResDTO = ApiResDTO.<List<MessageResDTO>>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("inbox message fetched successfully")
                    .data(messageResDTOList)
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.ok(listApiResDTO);
        }catch(Exception e){
            ApiResDTO<List<MessageResDTO>> errorResponse = ApiResDTO.<List<MessageResDTO>>builder()
                    .success(false)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
