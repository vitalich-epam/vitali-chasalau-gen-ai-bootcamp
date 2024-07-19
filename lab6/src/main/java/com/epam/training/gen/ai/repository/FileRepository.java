package com.epam.training.gen.ai.repository;

import com.epam.training.gen.ai.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Integer> {
    Optional<FileEntity> findByName(String name);
}
