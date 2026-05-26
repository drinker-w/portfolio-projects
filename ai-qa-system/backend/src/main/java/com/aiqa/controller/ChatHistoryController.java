package com.aiqa.controller;

import com.aiqa.common.Result;
import com.aiqa.entity.ChatHistory;
import com.aiqa.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

        @GetMapping
    public Result<List<ChatHistory>> list(@RequestAttribute Long userId) {
        List<ChatHistory> historyList = chatHistoryService.listByUserId(userId);
        return Result.success(historyList);
    }

        @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute Long userId) {
        chatHistoryService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
