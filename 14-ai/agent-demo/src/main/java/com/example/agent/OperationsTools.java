package com.example.agent;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 电商运营工具集——模拟多个业务系统的 API。
 *
 * Agent 需要根据用户的模糊目标（如"分析上月表现最差的商品"）
 * 动态决定调用哪些工具、什么顺序。
 */
@Component
public class OperationsTools {

    @Tool(description = "查询指定月份的商品销量排行，返回销量最高和最低的商品列表")
    public String getSalesReport(
            @ToolParam(description = "月份，格式 yyyy-MM，如 2026-05") String month) {
        System.out.println("  [TOOL] getSalesReport(" + month + ")");
        return """
                %s 销量报告:
                - 销量最高: 联想小新Pro16 (1200台, ¥5499)
                - 销量第二: Sony WH-1000XM5 (800台, ¥2299)
                - 销量最低: 智能手表X1 (仅23台, ¥899)
                - 销量倒二: USB-C扩展坞Pro (45台, ¥399)
                """.formatted(month);
    }

    @Tool(description = "查询指定商品的详细信息，包括价格、成本、评分、用户评价摘要")
    public String getProductDetail(
            @ToolParam(description = "商品名称") String productName) {
        System.out.println("  [TOOL] getProductDetail(" + productName + ")");
        if (productName.contains("手表") || productName.contains("X1")) {
            return """
                    商品: 智能手表X1
                    价格: ¥899, 成本: ¥650, 毛利率: 27.7%
                    评分: 2.3/5.0 (共156条评价)
                    差评关键词: "续航差"(52次), "表盘太大"(38次), "App难用"(29次)
                    上架时间: 2025-12-01
                    """;
        }
        return "商品: " + productName + "\n价格: ¥399, 评分: 4.2/5.0, 状态正常";
    }

    @Tool(description = "搜索指定商品的竞品信息，返回竞品名称、价格、评分")
    public String searchCompetitors(
            @ToolParam(description = "商品名称") String productName) {
        System.out.println("  [TOOL] searchCompetitors(" + productName + ")");
        if (productName.contains("手表") || productName.contains("X1")) {
            return """
                    智能手表X1 的竞品:
                    1. 小米手环9 Pro - ¥499, 评分4.6, 月销5000+
                    2. 华为Watch Fit 3 - ¥799, 评分4.5, 月销3000+
                    3. Apple Watch SE - ¥1999, 评分4.7, 月销2000+
                    结论: 竞品在 ¥499-799 价位段表现强势，X1定价¥899偏高
                    """;
        }
        return "未找到 " + productName + " 的竞品信息";
    }

    @Tool(description = "查询指定商品最近的营销活动和曝光数据")
    public String getMarketingData(
            @ToolParam(description = "商品名称") String productName) {
        System.out.println("  [TOOL] getMarketingData(" + productName + ")");
        if (productName.contains("手表") || productName.contains("X1")) {
            return """
                    智能手表X1 营销数据:
                    - 最近30天广告投入: ¥5000 (全店最低)
                    - 搜索曝光: 12000次 (同类平均 45000次)
                    - 点击率: 1.2% (同类平均 3.5%)
                    - 详情页转化率: 0.8% (同类平均 2.1%)
                    结论: 曝光不足+转化率低，营销投入严重不足
                    """;
        }
        return productName + " 营销数据正常";
    }
}
