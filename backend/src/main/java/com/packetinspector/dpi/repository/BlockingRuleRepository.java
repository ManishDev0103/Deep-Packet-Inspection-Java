package com.packetinspector.dpi.repository;

import com.packetinspector.dpi.model.entity.BlockingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BlockingRuleRepository extends JpaRepository<BlockingRuleEntity, Long> {
    List<BlockingRuleEntity> findByRuleType(String ruleType);

    @Transactional
    void deleteByRuleTypeAndRuleValue(String ruleType, String ruleValue);

    boolean existsByRuleTypeAndRuleValue(String ruleType, String ruleValue);
}
