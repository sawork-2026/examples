package com.example.toolcalling;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tool Calling 对话接口：
 *   GET /chat?q=...       — 带 Tool（LLM 可调用订单查询/天气/计算器）
 *   GET /chat/plain?q=... — 不带 Tool（对比，LLM 无法查询实时数据）
 */
@RestController
public class ToolCallingController {

    private final ChatClient toolChatClient;
    private final ChatClient plainChatClient;

    public ToolCallingController(ChatClient toolChatClient, ChatClient plainChatClient) {
        this.toolChatClient = toolChatClient;
        this.plainChatClient = plainChatClient;
    }

    @GetMapping("/chat")
    Map<String, String> chat(@RequestParam String q) {
        String answer = toolChatClient.prompt()
                .user(q)
                .call()
                .content();
        return Map.of("question", q, "answer", answer, "mode", "Tool Calling");
    }

    @GetMapping("/chat/plain")
    Map<String, String> plainChat(@RequestParam String q) {
        String answer = plainChatClient.prompt()
                .user(q)
                .call()
                .content();
        return Map.of("question", q, "answer", answer, "mode", "plain (no tools)");
    }

    @Configuration
    static class ChatConfig {

        @Bean
        ChatClient toolChatClient(ChatClient.Builder builder, OrderTools orderTools) {
            return builder
                    .defaultSystem("你是好买网的智能客服。可以查询订单、天气和做数学计算。")
                    .defaultTools(orderTools)
                    .build();
        }

        @Bean
        ChatClient plainChatClient(ChatClient.Builder builder) {
            return builder
                    .defaultSystem("你是好买网的智能客服。")
                    .build();
        }
    }
}
