package com.epam.healenium.repository;

import java.util.List;

import com.epam.healenium.model.domain.Selector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectorRepository extends JpaRepository<Selector, String> {

    List<Selector> findByLocator(String locatorValue);

}