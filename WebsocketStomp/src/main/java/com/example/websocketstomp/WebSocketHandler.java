package com.example.websocketstomp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    //웹소켓 연결 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        var sessionId = session.getId();  // session : 'StandardWebSocketSession구현체가 주입된다. 소스코드를 보면 UUID값이 생성되는 것으로 확인된다. 사용자는 유니크한 세션 ID를 배정받게 된다. 이와 같은 방법이, 세션 정보를 메모리에 저장하는 방식이다. 서버가 재부팅되면, 세션 정보는 전부 사라지게 될 것이다.
        sessions.put(sessionId, session);

        Message message = Message.builder().sender(sessionId).receiver("all").build();
        message.newConnect();

        sessions.values().forEach(s -> {
            try {
                if(!s.getId().equals(sessionId)) {
                    s.sendMessage(new TextMessage(Utils.getString(message)));
                }
            }
            catch (Exception e) {
                //TODO: throw
            }
        });
    }

    //위쪽 내용들이 클라이언특 서버에 웹소켓 연결을 하는 과정

    //양방향 데이터 통신
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        Message message = Utils.getObject(textMessage.getPayload());
        message.setSender(session.getId());

        WebSocketSession receiver = sessions.get(message.getReceiver());
        // 1) 메시지를 전달할 타켓 상대방을 찾는다.

        if (receiver != null && receiver.isOpen()) {
        // 타켓이 존재하고 연결된 상태라면, 메시지를 전송하다.
            receiver.sendMessage(new TextMessage(Utils.getString(message)));
        }
    }

    //소켓 연결 종료
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        var sessionId = session.getId();

        sessions.remove(sessionId);  // 1) 세션 저장소에서 연결이 끊긴 사용자를 삭제한다.

        final Message message = new Message();
        message.closeConnect();
        message.setSender(sessionId);

        sessions.values().forEach(s -> {
            try {                   // 2) 다른 사용자들에게, 누군가 접속이 끊겼다고 알려준다.
                s.sendMessage(new TextMessage(Utils.getString(message)));
            } catch (Exception e) {
                //TODO: throw
            }
        });
    }

    // 소켓 통신 에러
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //TODO:
    }
}

//웹소켓 핸들러 클래스는 4개의 메서드를 오버라이드 해야 한다.
//- afterConnectionEstablished : 웹소켓 연결 시
//- handleTextMessage : 테이터 통신 시
//- afterConnectionClosed : 웹소켓 연결 종료 시
//- handleTransportError : 웹소켓 통신 에러 시
