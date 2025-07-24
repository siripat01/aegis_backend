package com.aegis.aiservice.configuration;

import com.aegis.aiservice.dto.asisstant;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class modelConfiguration {
    @Value("${ollama.url}")
    String ollamaURL;

    @Value("${ollama.chatmodel}")
    String ollamaChatModel;

    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            private static final Logger log = LoggerFactory.getLogger(ChatModelListener.class);

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                System.out.println("onRequest(): " + requestContext);
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory());

        SpringRestClientBuilder springRestClientBuilder = SpringRestClient.builder()
                .restClientBuilder(restClientBuilder)
                .streamingRequestExecutor(new VirtualThreadTaskExecutor());

        return OllamaStreamingChatModel.builder()
                .httpClientBuilder(springRestClientBuilder)
                .baseUrl(ollamaURL)
                .temperature(1.2)
                .modelName(ollamaChatModel)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
    }

    @Bean
    public asisstant assistant(StreamingChatModel model) {
        return AiServices.builder(asisstant.class)
                .streamingChatModel(model)
                .build();
    }

}
