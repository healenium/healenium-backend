package com.epam.healenium.repository;

import com.epam.healenium.model.domain.HealingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealingResultRepository extends JpaRepository<HealingResult, Integer> {

}