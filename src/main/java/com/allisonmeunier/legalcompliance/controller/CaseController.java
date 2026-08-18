package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.audit.CaseAccessAuditService;
import com.allisonmeunier.legalcompliance.dto.AccessRequest;
import com.allisonmeunier.legalcompliance.dto.CaseResponse;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import com.allisonmeunier.legalcompliance.util.CaseReferenceMasker;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/cases")
public class CaseController {

    private static final Logger log = LoggerFactory.getLogger(CaseController.class);

    private final CaseRepository caseRepository;
    private final CaseAccessAuditService auditService;

    public CaseController(CaseRepository caseRepository, CaseAccessAuditService auditService) {
        this.caseRepository = caseRepository;
        this.auditService = auditService;
    }

    @GetMapping("/{reference}")
    public CaseResponse getCase(@PathVariable String reference) {
        log.info("Fetching case {}", CaseReferenceMasker.mask(reference));
        return caseRepository.findByReference(reference)
                .map(CaseResponse::from)
                .orElseThrow(() -> new CaseNotFoundException(reference));
    }

    @PostMapping("/{reference}/access")
    public ResponseEntity<Void> recordAccess(@PathVariable String reference,
                                              @Valid @RequestBody AccessRequest request,
                                              Principal principal) {
        auditService.recordAccess(reference, principal.getName(), request.category());
        return ResponseEntity.accepted().build();
    }
}
