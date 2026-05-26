package com.aiqa.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ChatService {

    /**
     * 普通问答（非流式）
     */
    String ask(Long userId, String question);

    /**
     * 流式问答（SSE）
     */
    void askStream(Long userId, Long documentId, String question, SseEmitter emitter);
}
