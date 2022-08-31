package com.example.websocketstomp2;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    /*
        /sub/channel/12345      - 구독(channelId:12345)
        /pub/hello              - 메시지 발행
    */

    @MessageMapping("/hello")
    public void message(Message message) {

        simpMessageSendingOperations.convertAndSend("/sub/channel/" + message.getChannelId(), message);

    }
}

//channel id가 다르면 메시지를 받지 않는다. 즉, eddy라는 채널 아이디에 구독중인 사용자만 메시지를 받게 된다.