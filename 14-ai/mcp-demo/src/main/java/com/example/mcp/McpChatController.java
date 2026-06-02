package com.example.mcp;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP 对话接口：LLM 通过 MCP 协议调用 Filesystem Server 读写文件。
 *
 * MCP Client 在启动时连接 mcp-servers.json 配置的 Server，
 * 调用 tools/list 自动发现可用工具，注册到 ChatClient。
 * LLM 完全不知道 MCP 的存在——它看到的和 @Tool 一样。
 */
@RestController
public class McpChatController {

    private final ChatClient chatClient;

    public McpChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    Map<String, String> chat(@RequestParam String q) {
        String answer = chatClient.prompt()
                .user(q)
                .call()
                .content();
        return Map.of("question", q, "answer", answer);
    }

    @Configuration
    static class ChatConfig {
        @Bean
        ChatClient chatClient(ChatClient.Builder builder, SyncMcpToolCallbackProvider mcpTools) {
            return builder
                    .defaultSystem("你是一个文件管理助手。可以帮用户查看目录内容、读取文件、创建文件。工作目录是 /tmp/mcp-workspace。")
                    .defaultToolCallbacks(mcpTools.getToolCallbacks())
                    .build();
        }
    }
}
