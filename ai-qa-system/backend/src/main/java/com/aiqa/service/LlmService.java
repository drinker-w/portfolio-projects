package com.aiqa.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface LlmService {

    /**
     * 调用大模型生成答案（非流式）
     */
    String generate(String prompt);

    /**
     * 调用大模型生成答案（流式）
     */
    void generateStream(String prompt, SseEmitter emitter);

    /**
     * 调用大模型生成答案（流式，带收集器）
     */
    void generateStreamWithCollector(String prompt, SseEmitter emitter, StringBuilder collector);
}
