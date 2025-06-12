package com.epam.healenium.repository;

import com.epam.healenium.model.domain.HealingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealingResultRepository extends JpaRepository<HealingResult, Integer> {

    @Query("select hr from HealingResult hr WHERE hr.healing.uid = :id")
    List<HealingResult> findByHealingId(@Param("id") String id);

    @Query("select hr from HealingResult hr WHERE hr.successHealing = false")
    List<HealingResult> findUnsuccessfulHealings();

}