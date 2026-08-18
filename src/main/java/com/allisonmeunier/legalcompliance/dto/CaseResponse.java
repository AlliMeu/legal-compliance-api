package com.allisonmeunier.legalcompliance.dto;

import com.allisonmeunier.legalcompliance.model.Case;

public record CaseResponse(String reference, String matterName, String status) {

    public static CaseResponse from(Case c) {
        return new CaseResponse(c.getReference(), c.getMatterName(), c.getStatus());
    }
}
