package com.epam.training.gen.ai.repository;

import com.epam.training.gen.ai.entity.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContentRepository extends JpaRepository<ContentEntity, String> {
    List<ContentEntity> findByFileNameAndIndexInOrderByIndex(String fileName, Collection<Integer> indexes);
}
