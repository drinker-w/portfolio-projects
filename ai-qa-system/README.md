# AI 智能问答系统

基于大模型 API 的文档问答系统，上传 PDF/TXT 文档后可进行基于文档内容的对话。

## 技术栈

Spring Boot + Vue 3 + Element Plus + MySQL + Redis + 通义千问 API + Docker

## 功能

- JWT 登录/注册
- PDF/TXT 文档上传与自动解析
- 基于文档内容增强的智能问答
- SSE 流式输出
- 对话历史
- Redis 缓存（带降级）

## 本地运行

1. 复制 `.env.example` 为 `.env`，填入 `DASHSCOPE_API_KEY`
2. `docker-compose up -d`
3. 浏览器打开 `http://localhost:82`

## 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/register | POST | 注册 |
| /api/auth/login | POST | 登录 |
| /api/documents/upload | POST | 上传文档 |
| /api/documents/list | GET | 文档列表 |
| /api/documents/{id} | DELETE | 删除文档 |
| /api/chat/ask | POST | 问答 |
| /api/chat/stream | GET | 流式问答（SSE） |
| /api/chat/history | GET | 对话历史 |

## 在线演示

http://114.55.98.103/
