package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    List<Report> findAllByOrderByCreatedDateDesc();
    List<Report> findByCreatedDateBetweenOrderByCreatedDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    List<Report> findByKeyIdInOrderByCreatedDateDesc(Collection<UUID> keyIds);
    List<Report> findByCreatedDateBetweenAndKeyIdInOrderByCreatedDateDesc(LocalDateTime startDate, LocalDateTime endDate, Collection<UUID> keyIds);
}