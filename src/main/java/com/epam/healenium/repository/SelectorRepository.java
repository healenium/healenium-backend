package com.epam.healenium.repository;

import java.util.List;

import com.epam.healenium.model.domain.Selector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectorRepository extends MongoRepository<Selector, String> {

    List<Selector> findByLocator(String locatorValue);

}