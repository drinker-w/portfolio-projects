package com.aiqa.service;

import com.aiqa.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    /**
     * 上传文档
     */
    Document upload(Long userId, MultipartFile file);

    /**
     * 获取用户文档列表
     */
    List<Document> listByUserId(Long userId);

    /**
     * 根据ID获取文档
     */
    Document getById(Long id);

    /**
     * 删除文档
     */
    void delete(Long id, Long userId);

    /**
     * 检索文档内容（关键词匹配）
     */
    List<Document> searchByContent(Long userId, String keyword);
}
