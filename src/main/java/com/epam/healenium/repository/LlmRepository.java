package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Llm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmRepository extends JpaRepository<Llm, String> {
}
