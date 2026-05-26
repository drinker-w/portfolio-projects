package com.aiqa.service.impl;

import com.aiqa.service.LlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generate(String prompt) {
        try {
            String requestBody = buildRequestBody(prompt, false);
            log.info("调用大模型API，请求体: {}", requestBody);
            HttpURLConnection connection = createConnection(requestBody);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                
                String errorResponse = readResponse(connection);
                log.error("大模型API调用失败，状态码: {}，响应: {}", responseCode, errorResponse);
                return "AI service unavailable, please retry.";
            }

            String response = readResponse(connection);
            log.info("API response: {}", response);
            return extractContent(response);
        } catch (Exception e) {
            log.error("API call failed", e);
            return "AI service error, please retry.";
        }
    }

    @Override
    public void generateStream(String prompt, SseEmitter emitter) {
        try {
            String requestBody = buildRequestBody(prompt, true);
            HttpURLConnection connection = createConnection(requestBody);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                emitter.send("AI service unavailable.");
                emitter.complete();
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder contentBuffer = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        try {
                            JsonNode jsonNode = objectMapper.readTree(data);
                            JsonNode choices = jsonNode.get("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").asText();
                                    if (!content.isEmpty()) {
                                        contentBuffer.append(content);
                                        emitter.send(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析流式数据异常: {}", e.getMessage());
                        }
                    }
                }

                emitter.send("[DONE]");
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("Stream API call failed", e);
            try {
                emitter.send("AI service error.");
                emitter.complete();
            } catch (Exception ex) {
                log.error("Failed to send error event", ex);
            }
        }
    }

    @Override
    public void generateStreamWithCollector(String prompt, SseEmitter emitter, StringBuilder collector) {
        try {
            String requestBody = buildRequestBody(prompt, true);
            HttpURLConnection connection = createConnection(requestBody);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMsg = "AI service unavailable.";
                collector.append(errorMsg);
                emitter.send(errorMsg);
                emitter.complete();
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }

                        try {
                            JsonNode jsonNode = objectMapper.readTree(data);
                            JsonNode choices = jsonNode.get("choices");
                            if (choices != null && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").asText();
                                    if (!content.isEmpty()) {
                                        collector.append(content);
                                        emitter.send(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("解析流式数据异常: {}", e.getMessage());
                        }
                    }
                }

                emitter.send("[DONE]");
                emitter.complete();
                log.info("流式输出完成，收集到的内容长度: {}", collector.length());
            }
        } catch (Exception e) {
            log.error("Stream API call failed", e);
            try {
                String errorMsg = "AI service error.";
                collector.append(errorMsg);
                emitter.send(errorMsg);
                emitter.complete();
            } catch (Exception ex) {
                log.error("发送错误事件异常", ex);
            }
        }
    }

        private String buildRequestBody(String prompt, boolean stream) throws Exception {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", model);

        ArrayNode messagesArray = objectMapper.createArrayNode();

        
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant. Answer questions based on the provided document content. If the document does not contain relevant information, say so honestly.");
        messagesArray.add(systemMessage);

        
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messagesArray.add(userMessage);

        rootNode.set("messages", messagesArray);

        
        if (stream) {
            rootNode.put("stream", true);
        }

        return objectMapper.writeValueAsString(rootNode);
    }

        private HttpURLConnection createConnection(String requestBody) throws Exception {
        URL url = new URL(baseUrl + "/compatible-mode/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        return connection;
    }

        private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

        private String extractContent(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");
            if (choices != null && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
            return "Failed to parse AI response.";
        } catch (Exception e) {
            log.error("Failed to parse response", e);
            return "Failed to parse response.";
        }
    }
}
