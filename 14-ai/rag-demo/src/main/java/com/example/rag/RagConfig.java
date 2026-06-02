package com.example.rag;

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
 * RAG 配置：
 *   1. SimpleVectorStore — 内存向量库（无需外部数据库）
 *   2. ChatClient — 带 RAG Advisor 的对话客户端
 *   3. 启动时自动加载 docs/ 下的文档
 */
@Configuration
public class RagConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
        var retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .build();
        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
        return builder
                .defaultSystem("你是好买网的智能客服。基于提供的文档内容回答用户问题，如果文档中没有相关信息，如实回答不知道。")
                .defaultAdvisors(ragAdvisor)
                .build();
    }

    @Bean
    ChatClient plainChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是好买网的智能客服。")
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadDocuments(ApplicationReadyEvent event) throws IOException {
        var vectorStore = event.getApplicationContext().getBean(VectorStore.class);
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:docs/*.md");

        var splitter = new TokenTextSplitter();
        int totalChunks = 0;
        for (Resource resource : resources) {
            var reader = new MarkdownDocumentReader(resource,
                    MarkdownDocumentReaderConfig.defaultConfig());
            List<Document> docs = reader.get();
            List<Document> chunks = splitter.apply(docs);
            vectorStore.add(chunks);
            totalChunks += chunks.size();
            System.out.printf("[RAG] 已加载: %s → %d 个文档块%n",
                    resource.getFilename(), chunks.size());
        }
        System.out.printf("[RAG] 共加载 %d 个文件, %d 个文档块%n",
                resources.length, totalChunks);
    }
}
