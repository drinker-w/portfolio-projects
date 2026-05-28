package com.aiqa.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface ChatService {

    String ask(Long userId, String question);

    void askStream(Long userId, Long documentId, String question,
                   List<Map<String, String>> history, SseEmitter emitter);
}
