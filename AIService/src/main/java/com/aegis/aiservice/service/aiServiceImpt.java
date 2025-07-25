package com.aegis.aiservice.service;

import com.aegis.aiservice.dto.asisstant;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.aegis.aiservice.dto.userGenerateRequest;

import java.util.List;

@Service
public class aiServiceImpt {

//    @Bean
//    public asisstant buildModel() {
//
//        StreamingChatModel model = OllamaStreamingChatModel.builder()
//                .baseUrl("http://localhost:11435")
//                .temperature(1.2)
//                .modelName("mistral-nemo:12b")
//                .logRequests(true)
//                .logResponses(true)
//                .build();
//
//        return AiServices.builder(asisstant.class)
//                .streamingChatModel(model)
//                .build();
//    }

//    static class PersistentChatMemoryStore implements ChatMemoryStore {
//
//        private final DB db = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable().make();
//        private final Map<Integer, String> map = db.hashMap("messages", INTEGER, STRING).createOrOpen();
//
//        @Override
//        public List<ChatMessage> getMessages(Object memoryId) {
//            String json = map.get((int) memoryId);
//            return messagesFromJson(json);
//        }
//
//        @Override
//        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
//            String json = messagesToJson(messages);
//            map.put((int) memoryId, json);
//            db.commit();
//        }
//
//        @Override
//        public void deleteMessages(Object memoryId) {
//            map.remove((int) memoryId);
//            db.commit();
//        }
//    }

    public String getMessage(userGenerateRequest request) {
        return request.getMessage();
    }
}
