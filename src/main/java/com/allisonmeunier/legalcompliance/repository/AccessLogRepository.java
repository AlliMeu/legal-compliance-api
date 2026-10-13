package com.allisonmeunier.legalcompliance.repository;

import com.allisonmeunier.legalcompliance.model.AccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessLogRepository extends MongoRepository<AccessLog, String> {
}
