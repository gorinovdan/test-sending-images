package org.example.repository;

import org.example.entity.ImageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageEventRepository extends JpaRepository<ImageEvent, Long> {
}