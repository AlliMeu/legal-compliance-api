package com.allisonmeunier.legalcompliance.repository;

import com.allisonmeunier.legalcompliance.model.Case;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CaseRepository extends MongoRepository<Case, String> {

    Optional<Case> findByReference(String reference);
}
