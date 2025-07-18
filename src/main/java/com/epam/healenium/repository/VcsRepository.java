package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Vcs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VcsRepository extends JpaRepository<Vcs, String> {
}
