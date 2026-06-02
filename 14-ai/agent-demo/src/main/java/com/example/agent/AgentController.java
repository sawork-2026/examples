package com.example.agent;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 对话接口。
 *
 * 与 tool-calling-demo 的区别：
 *   - tool-calling-demo: 用户问题明确（"ORD-001到哪了"），LLM 一次性决定调哪个工具
 *   - agent-demo: 用户给模糊目标（"分析最差商品"），LLM 多轮动态决策调什么
 *
 * Spring AI 的 ToolCallAdvisor 自动驱动 ReAct 循环：
 *   LLM → tool_calls → 执行 → 结果加回 messages → LLM → ... → 无 tool_calls → 结束
 */
@RestController
public class AgentController {

    private final ChatClient agentClient;

    public AgentController(ChatClient agentClient) {
        this.agentClient = agentClient;
    }

    @GetMapping("/agent")
    Map<String, String> agent(@RequestParam String q) {
        System.out.println("\n========== Agent 开始 ==========");
        System.out.println("目标: " + q);
        String answer = agentClient.prompt()
                .user(q)
                .call()
                .content();
        System.out.println("========== Agent 结束 ==========\n");
        return Map.of("question", q, "answer", answer);
    }
}
