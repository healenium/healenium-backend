package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Healing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealingRepository extends MongoRepository<Healing, String> {

    @Query("{ 'selector.uid' : ?0 }")
    List<Healing> findBySelectorId(String selectorId);

}