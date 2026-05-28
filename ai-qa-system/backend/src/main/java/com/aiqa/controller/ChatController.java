package com.aiqa.controller;

import com.aiqa.common.Result;
import com.aiqa.service.ChatService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
                             @RequestParam String question,
                             @RequestParam(required = false) String history) {
        SseEmitter emitter = new SseEmitter(120000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> { log.error("SSE连接异常", e); emitter.complete(); });

        // 解析对话历史
        List<Map<String, String>> historyList = new ArrayList<>();
        if (history != null && !history.isEmpty()) {
            try {
                String decoded = URLDecoder.decode(history, StandardCharsets.UTF_8);
                historyList = objectMapper.readValue(decoded, new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                log.warn("解析对话历史失败: {}", e.getMessage());
            }
        }

        try {
            chatService.askStream(userId, documentId, question, historyList, emitter);
        } catch (Exception e) {
            log.error("流式问答异常", e);
            try { emitter.send("处理请求时出现异常"); emitter.complete(); }
            catch (Exception ex) { emitter.complete(); }
        }
        return emitter;
    }

    @Data
    static class ChatRequest {
        private String question;
    }
}
