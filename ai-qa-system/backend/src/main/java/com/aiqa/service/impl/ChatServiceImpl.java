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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final LlmService llmService;
    private final DocumentService documentService;
    private final ChatHistoryMapper chatHistoryMapper;

    @Override
    public String ask(Long userId, String question) {
        
        String prompt = buildPrompt(userId, question);

        String answer = llmService.generate(prompt);

        saveChatHistory(userId, null, question, answer);

        return answer;
    }

    @Override
    public void askStream(Long userId, Long documentId, String question, SseEmitter emitter) {
        
        String prompt = buildPrompt(userId, question);

        StringBuilder fullAnswer = new StringBuilder();

        llmService.generateStreamWithCollector(prompt, emitter, fullAnswer);

        try {
            saveChatHistory(userId, documentId, question, fullAnswer.toString());
            log.info("对话历史已保存，问题: {}，回答长度: {}", question, fullAnswer.length());
        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }

        private String buildPrompt(Long userId, String question) {
        StringBuilder promptBuilder = new StringBuilder();

        
        List<Document> documents = documentService.listByUserId(userId);

        if (documents.isEmpty()) {
            promptBuilder.append("用户问题：").append(question);
        } else {
            
            promptBuilder.append("请根据以下文档内容回答用户的问题。\n\n");
            promptBuilder.append("【参考文档】\n");

            int maxLength = 3000;
            for (Document doc : documents) {
                String content = doc.getContent();
                if (content != null && content.length() > maxLength) {
                    content = content.substring(0, maxLength) + "...";
                }
                promptBuilder.append("文档名：").append(doc.getFileName()).append("\n");
                promptBuilder.append("内容：").append(content).append("\n\n");
            }

            promptBuilder.append("【用户问题】\n");
            promptBuilder.append(question);
            promptBuilder.append("\n\n请基于上述文档内容给出准确、详细的回答。如果文档中没有相关信息，请如实告知。");
        }

        return promptBuilder.toString();
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
