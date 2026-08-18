package com.allisonmeunier.legalcompliance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cases")
public class Case {

    @Id
    private String id;
    private String reference;
    private String matterName;
    private String status;

    public Case() {
    }

    public Case(String reference, String matterName, String status) {
        this.reference = reference;
        this.matterName = matterName;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getMatterName() {
        return matterName;
    }

    public String getStatus() {
        return status;
    }
}
