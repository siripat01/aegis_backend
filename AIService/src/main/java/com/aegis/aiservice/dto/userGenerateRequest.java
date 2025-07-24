package com.aegis.aiservice.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class userGenerateRequest {
    private String message;
    private String sessionId;
}
