package com.example.filecopy;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class FileCopyConfig {

    @Value("${filecopy.input-dir:input}")
    private String inputDir;

    @Value("${filecopy.output-dir:output}")
    private String outputDir;

    @Value("${filecopy.pattern:*.txt}")
    private String filePattern;

    @Bean
    public MessageChannel fileChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(value = "fileChannel",
            poller = @Poller(fixedDelay = "5000"))
    public FileReadingMessageSource fileReader() {
        FileReadingMessageSource reader = new FileReadingMessageSource();
        reader.setDirectory(new File(inputDir));
        reader.setFilter(new SimplePatternFileListFilter(filePattern));
        return reader;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileChannel")
    public MessageHandler fileWriter() {
        FileWritingMessageHandler handler =
                new FileWritingMessageHandler(new File(outputDir));
        handler.setFileExistsMode(FileExistsMode.REPLACE);
        handler.setExpectReply(false);
        return handler;
    }
}
