package com.example.agent;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Agent 配置：Tool + RAG + Memory 全部组装在一起。
 *
 * 这就是 Harness 的 Spring AI 实现：
 *   - Tool Orchestration: OperationsTools (@Tool)
 *   - Context & Memory: RAG (VectorStore + RetrievalAugmentationAdvisor)
 *   - Execution Loop: ToolCallAdvisor 自动驱动 ReAct 循环
 */
@Configuration
public class AgentConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ChatClient agentClient(ChatClient.Builder builder,
                           VectorStore vectorStore,
                           OperationsTools operationsTools) {
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .build();
        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();

        return builder
                .defaultSystem("""
                        你是好买网的运营分析助手。你可以：
                        1. 查询销量数据、商品详情、竞品信息、营销数据
                        2. 检索公司内部文档（定价策略等）

                        工作方式：根据用户的目标，一步步分析，每一步说明你的思考过程。
                        不要一次调用所有工具，而是根据上一步的结果决定下一步该做什么。
                        最终给出有数据支撑的、可执行的建议。
                        """)
                .defaultAdvisors(ragAdvisor)
                .defaultTools(operationsTools)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadDocuments(ApplicationReadyEvent event) throws IOException {
        var vectorStore = event.getApplicationContext().getBean(VectorStore.class);
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:docs/*.md");
        var splitter = new TokenTextSplitter();
        for (Resource resource : resources) {
            var reader = new MarkdownDocumentReader(resource,
                    MarkdownDocumentReaderConfig.defaultConfig());
            List<Document> chunks = splitter.apply(reader.get());
            vectorStore.add(chunks);
            System.out.printf("[RAG] 已加载: %s → %d 个文档块%n",
                    resource.getFilename(), chunks.size());
        }
    }
}
