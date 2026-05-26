package com.aiqa.controller;

import com.aiqa.common.Result;
import com.aiqa.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

        @PostMapping("/ask")
    public Result<Map<String, String>> ask(@RequestAttribute Long userId,
                                           @RequestBody ChatRequest request) {
        String answer = chatService.ask(userId, request.getQuestion());

        Map<String, String> data = new HashMap<>();
        data.put("question", request.getQuestion());
        data.put("answer", answer);

        return Result.success(data);
    }

        @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestAttribute Long userId,
                             @RequestParam Long documentId,
                             @RequestParam String question) {
        SseEmitter emitter = new SseEmitter(120000L);

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> {
            log.error("SSE连接异常", e);
            emitter.complete();
        });

        try {
            chatService.askStream(userId, documentId, question, emitter);
        } catch (Exception e) {
            log.error("流式问答异常", e);
            try {
                emitter.send("处理请求时出现异常");
                emitter.complete();
            } catch (Exception ex) {
                emitter.complete();
            }
        }

        return emitter;
    }

    @Data
    static class ChatRequest {
        private String question;
    }
}
