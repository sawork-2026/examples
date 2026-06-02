package com.example.toolcalling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 模拟电商业务工具——LLM 通过 @Tool 注解发现并调用。
 *
 * LLM 不执行代码，它只"选择"调哪个工具、传什么参数。
 * Spring AI 负责实际执行方法并将结果喂回 LLM。
 */
@Component
public class OrderTools {

    private final Map<String, OrderInfo> orders = new ConcurrentHashMap<>();

    record OrderInfo(String orderId, String product, String status, String tracking, double amount) {}

    @PostConstruct
    void init() {
        orders.put("ORD-001", new OrderInfo("ORD-001", "联想小新 Pro 16", "已发货", "SF1234567890", 5499.0));
        orders.put("ORD-002", new OrderInfo("ORD-002", "Sony WH-1000XM5 耳机", "已签收", "YT9876543210", 2299.0));
        orders.put("ORD-003", new OrderInfo("ORD-003", "Apple iPad Air", "待支付", null, 4799.0));
    }

    @Tool(description = "根据订单号查询订单的商品、状态和物流信息")
    public String queryOrder(@ToolParam(description = "订单号，如 ORD-001") String orderId) {
        OrderInfo order = orders.get(orderId);
        if (order == null) {
            return "未找到订单: " + orderId;
        }
        return String.format("订单 %s: 商品[%s], 状态[%s], 快递单号[%s], 金额[%.2f元]",
                order.orderId, order.product, order.status,
                order.tracking != null ? order.tracking : "无", order.amount);
    }

    @Tool(description = "查询指定城市的当前天气（温度、天气状况）")
    public String getWeather(@ToolParam(description = "城市名称，如 上海、北京") String city) {
        // 模拟天气数据
        return switch (city) {
            case "上海" -> "上海: 多云, 28°C, 湿度 65%";
            case "北京" -> "北京: 晴, 32°C, 湿度 40%";
            case "杭州" -> "杭州: 小雨, 25°C, 湿度 80%";
            default -> city + ": 晴, 26°C, 湿度 55%";
        };
    }

    @Tool(description = "计算两个数的数学运算结果（加减乘除）")
    public String calculate(
            @ToolParam(description = "运算符: +, -, *, /") String operator,
            @ToolParam(description = "第一个数") double a,
            @ToolParam(description = "第二个数") double b) {
        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            default -> Double.NaN;
        };
        return String.format("%.2f %s %.2f = %.2f", a, operator, b, result);
    }
}
