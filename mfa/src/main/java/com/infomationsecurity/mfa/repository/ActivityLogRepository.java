package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer>, JpaSpecificationExecutor<ActivityLog> {
}
