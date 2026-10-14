package com.allisonmeunier.legalcompliance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cases")
public class Case {

    @Id
    private String id;
    private String reference;
    private String subject;
    private String status;

    public Case() {
    }

    public Case(String reference, String subject, String status) {
        this.reference = reference;
        this.subject = subject;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getSubject() {
        return subject;
    }

    public String getStatus() {
        return status;
    }
}
