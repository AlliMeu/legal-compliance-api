package com.allisonmeunier.legalcompliance.model;

import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "access_logs")
public class AccessLog {

    @Id
    private String id;
    private String caseReference;
    private String userId;
    private String category;
    private AccessChannel channel;
    private Instant accessedAt;

    public AccessLog() {
    }

    public AccessLog(String caseReference, String userId, String category, AccessChannel channel, Instant accessedAt) {
        this.caseReference = caseReference;
        this.userId = userId;
        this.category = category;
        this.channel = channel;
        this.accessedAt = accessedAt;
    }

    public String getId() {
        return id;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public AccessChannel getChannel() {
        return channel;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }
}
