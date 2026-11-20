package com.packetinspector.dpi.repository;

import com.packetinspector.dpi.model.entity.ConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Long> {
    List<ConnectionEntity> findTop100ByOrderByIdDesc();
}
