package com.example.rag;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 对话接口：
 *   GET /chat?q=...       — 带 RAG（基于文档回答）
 *   GET /chat/plain?q=... — 不带 RAG（对比，可能幻觉）
 */
@RestController
public class RagController {

    private final ChatClient ragChatClient;
    private final ChatClient plainChatClient;

    public RagController(@Qualifier("ragChatClient") ChatClient ragChatClient,
                         @Qualifier("plainChatClient") ChatClient plainChatClient) {
        this.ragChatClient = ragChatClient;
        this.plainChatClient = plainChatClient;
    }

    // 带 RAG：QuestionAnswerAdvisor 自动检索文档 → 注入 prompt → LLM 基于文档回答
    @GetMapping("/chat")
    Map<String, String> chat(@RequestParam String q) {
        String answer = ragChatClient.prompt()
                .user(q)
                .call()
                .content();
        return Map.of("question", q, "answer", answer, "mode", "RAG");
    }

    // 不带 RAG：LLM 凭自身知识回答（可能编造）
    @GetMapping("/chat/plain")
    Map<String, String> plainChat(@RequestParam String q) {
        String answer = plainChatClient.prompt()
                .user(q)
                .call()
                .content();
        return Map.of("question", q, "answer", answer, "mode", "plain (no RAG)");
    }
}
