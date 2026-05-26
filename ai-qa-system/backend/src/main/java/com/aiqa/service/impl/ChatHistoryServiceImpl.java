package com.aiqa.service.impl;

import com.aiqa.common.BusinessException;
import com.aiqa.entity.ChatHistory;
import com.aiqa.entity.Document;
import com.aiqa.mapper.ChatHistoryMapper;
import com.aiqa.mapper.DocumentMapper;
import com.aiqa.service.ChatHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final ChatHistoryMapper chatHistoryMapper;
    private final DocumentMapper documentMapper;

    @Override
    public List<ChatHistory> listByUserId(Long userId) {
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getUserId, userId)
                .orderByDesc(ChatHistory::getCreateTime);
        List<ChatHistory> historyList = chatHistoryMapper.selectList(wrapper);

        List<Long> documentIds = historyList.stream()
                .map(ChatHistory::getDocumentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (!documentIds.isEmpty()) {
            Map<Long, String> documentNameMap = documentMapper.selectBatchIds(documentIds)
                    .stream()
                    .collect(Collectors.toMap(Document::getId, Document::getFileName));

            for (ChatHistory history : historyList) {
                if (history.getDocumentId() != null) {
                    history.setDocumentName(documentNameMap.get(history.getDocumentId()));
                }
            }
        }

        return historyList;
    }

    @Override
    public void delete(Long id, Long userId) {
        ChatHistory history = chatHistoryMapper.selectById(id);
        if (history == null) {
            throw new BusinessException("对话记录不存在");
        }
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该对话记录");
        }
        chatHistoryMapper.deleteById(id);
    }
}
