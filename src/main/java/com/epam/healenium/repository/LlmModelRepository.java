package com.epam.healenium.repository;

import com.epam.healenium.model.domain.LlmModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmModelRepository extends JpaRepository<LlmModel, String> {

    Optional<LlmModel> findByName(String name);
}
