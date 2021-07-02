package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Healing;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealingRepository extends JpaRepository<Healing, String>, JpaSpecificationExecutor<Healing> {

    @Query("select h from Healing h WHERE h.selector.uid = :id")
    List<Healing> findBySelectorId(@Param("id") String id);

    @Query("select h from Healing h WHERE h.selector.uid = :id order by h.createdDate desc")
    List<Healing> findLastBySelectorId(@Param("id") String id, Pageable pageable);
}