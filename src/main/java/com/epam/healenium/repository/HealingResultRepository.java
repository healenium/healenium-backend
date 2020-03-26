package com.epam.healenium.repository;

import com.epam.healenium.model.domain.HealingResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealingResultRepository extends MongoRepository<HealingResult, String> {

}