package com.aiqa.service;

import com.aiqa.entity.ChatHistory;

import java.util.List;

public interface ChatHistoryService {

    /**
     * 获取用户对话历史列表
     */
    List<ChatHistory> listByUserId(Long userId);

    /**
     * 删除对话历史
     */
    void delete(Long id, Long userId);
}
