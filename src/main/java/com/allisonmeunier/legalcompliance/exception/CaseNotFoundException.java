package com.allisonmeunier.legalcompliance.exception;

public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException(String caseReference) {
        super("No case found for reference " + caseReference);
    }
}
