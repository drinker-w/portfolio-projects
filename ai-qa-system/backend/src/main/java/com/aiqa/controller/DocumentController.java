package com.aiqa.controller;

import com.aiqa.common.Result;
import com.aiqa.entity.Document;
import com.aiqa.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

        @PostMapping("/upload")
    public Result<Document> upload(@RequestAttribute Long userId,
                                   @RequestParam("file") MultipartFile file) {
        Document document = documentService.upload(userId, file);
        return Result.success("上传成功", document);
    }

        @GetMapping("/list")
    public Result<List<Document>> list(@RequestAttribute Long userId) {
        List<Document> documents = documentService.listByUserId(userId);
        return Result.success(documents);
    }

        @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestAttribute Long userId) {
        documentService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
