package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findAllByOrderByCreatedDateDesc();

}