package com.aiqa.service.impl;

import com.aiqa.entity.ChatHistory;
import com.aiqa.entity.Document;
import com.aiqa.mapper.ChatHistoryMapper;
import com.aiqa.service.ChatService;
import com.aiqa.service.DocumentService;
import com.aiqa.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final LlmService llmService;
    private final DocumentService documentService;
    private final ChatHistoryMapper chatHistoryMapper;

    @Override
    public String ask(Long userId, String question) {
        String answer = llmService.generate("请回答以下问题：" + question);
        saveChatHistory(userId, null, question, answer);
        return answer;
    }

    @Override
    public void askStream(Long userId, Long documentId, String question,
                          List<Map<String, String>> history, SseEmitter emitter) {

        Document doc = documentService.getById(documentId);
        String docName = doc != null ? doc.getFileName() : "未知文档";
        String docContent = "";
        if (doc != null && doc.getContent() != null) {
            docContent = doc.getContent();
            int maxLen = 4000;
            if (docContent.length() > maxLen) {
                docContent = docContent.substring(0, maxLen) + "...";
            }
        }

        // 构建带历史上下文的提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能文档助手。请基于以下文档内容回答用户问题。\n\n");
        prompt.append("【当前文档】").append(docName).append("\n");
        prompt.append(docContent).append("\n\n");

        // 添加对话历史作为上下文
        if (history != null && !history.isEmpty()) {
            prompt.append("【对话历史】\n");
            int recentCount = Math.min(history.size(), 6); // 最多保留最近6轮
            int startIdx = history.size() - recentCount;
            for (int i = startIdx; i < history.size(); i++) {
                Map<String, String> msg = history.get(i);
                prompt.append("用户: ").append(msg.get("user")).append("\n");
                prompt.append("助手: ").append(msg.get("ai")).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("【当前问题】\n").append(question);
        prompt.append("\n\n请基于文档内容准确回答。如果文档中没有相关信息，请如实告知。");

        StringBuilder fullAnswer = new StringBuilder();
        llmService.generateStreamWithCollector(prompt.toString(), emitter, fullAnswer);

        try {
            saveChatHistory(userId, documentId, question, fullAnswer.toString());
        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }

    private void saveChatHistory(Long userId, Long documentId, String question, String answer) {
        try {
            ChatHistory history = new ChatHistory();
            history.setUserId(userId);
            history.setDocumentId(documentId);
            history.setQuestion(question);
            history.setAnswer(answer);
            history.setCreateTime(LocalDateTime.now());
            chatHistoryMapper.insert(history);
        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }
}
