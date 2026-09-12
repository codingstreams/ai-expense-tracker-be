package com.example.et.core.config;

import com.example.et.module.ai.tool.FinanceAiTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  ChatClient chatClient(ChatClient.Builder chatClientBuilder, FinanceAiTools financeAiTools) {
    return chatClientBuilder
        .defaultTools(financeAiTools)
        .build();
  }
}