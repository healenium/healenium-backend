package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Selector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectorRepository extends JpaRepository<Selector, String> {

    List<Selector> findByCommandAndEnableHealing(String command, boolean enableHealing);

    List<Selector> findByCommandNull();

}