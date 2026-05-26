package com.aiqa.service.impl;

import com.aiqa.common.BusinessException;
import com.aiqa.entity.Document;
import com.aiqa.mapper.DocumentMapper;
import com.aiqa.service.DocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentMapper documentMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public Document upload(Long userId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!".pdf".equals(suffix) && !".txt".equals(suffix)) {
            throw new BusinessException("仅支持PDF和TXT格式的文件");
        }

        try {
            
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(uploadDir, dateDir);
            Files.createDirectories(uploadPath);

            String newFileName = UUID.randomUUID().toString() + suffix;
            Path filePath = uploadPath.resolve(newFileName);

            
            file.transferTo(filePath.toFile());

            
            String content = parseDocument(filePath.toFile(), suffix);

            
            Document document = new Document();
            document.setUserId(userId);
            document.setFileName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setContent(content);
            document.setCreateTime(LocalDateTime.now());
            documentMapper.insert(document);

            return document;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败，请重试");
        }
    }

    @Override
    public List<Document> listByUserId(Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getUserId, userId)
                .orderByDesc(Document::getCreateTime);
        return documentMapper.selectList(wrapper);
    }

    @Override
    public Document getById(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该文档");
        }

        
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", e.getMessage());
        }

        
        documentMapper.deleteById(id);
    }

    @Override
    public List<Document> searchByContent(Long userId, String keyword) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getUserId, userId)
                .like(Document::getContent, keyword)
                .orderByDesc(Document::getCreateTime);
        return documentMapper.selectList(wrapper);
    }

        private String parseDocument(File file, String suffix) {
        try {
            if (".pdf".equals(suffix)) {
                return parsePdf(file);
            } else if (".txt".equals(suffix)) {
                return parseTxt(file);
            }
            return "";
        } catch (Exception e) {
            log.error("文档解析失败", e);
            return "";
        }
    }

        private String parsePdf(File file) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdDocument);
        }
    }

        private String parseTxt(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
