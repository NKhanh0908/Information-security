package com.infomationsecurity.mfa.specification;

import com.infomationsecurity.mfa.dto.request.fiters.ActivityLogFilter;
import com.infomationsecurity.mfa.entity.ActivityLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ActivityLogSpecification {
    public static Specification<ActivityLog> filter(ActivityLogFilter filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (filter.getAccountId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("accountId"), filter.getAccountId()));
            }

            if (filter.getDeviceId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("device").get("deviceId"), filter.getDeviceId()));
            }

            if (filter.getLogAction() != null && !filter.getLogAction().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("logAction"), "%" + filter.getLogAction() + "%"));
            }

            if (filter.getStartDate() != null && !filter.getStartDate().isEmpty()) {
                LocalDateTime startDate = LocalDateTime.parse(filter.getStartDate());
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("logTimestamp"), startDate));
            }

            if (filter.getEndDate() != null && !filter.getEndDate().isEmpty()) {
                LocalDateTime endDate = LocalDateTime.parse(filter.getEndDate());
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("logTimestamp"), endDate));
            }

            return predicate;
        };
    }
}
