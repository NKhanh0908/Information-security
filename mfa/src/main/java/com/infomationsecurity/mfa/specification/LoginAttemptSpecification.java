package com.infomationsecurity.mfa.specification;

import com.infomationsecurity.mfa.dto.request.fiters.LoginAttemptFilter;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class LoginAttemptSpecification {
    public static Specification<LoginAttempt> filter(LoginAttemptFilter filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Lọc theo accountId
            if (filter.getAccountId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("account").get("accountId"), filter.getAccountId()));
            }

            // Lọc theo trustDeviceId
            if (filter.getTrustDeviceId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("trustDevice").get("deviceId"), filter.getTrustDeviceId()));
            }

            // Lọc theo attemptSuccess
            if (filter.getAttemptSuccess() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("attemptSuccess"), filter.getAttemptSuccess()));
            }

            // Lọc theo khoảng thời gian (startDate, endDate)
            if (filter.getStartDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("attemptCreatedAt"), filter.getStartDate()));
            }

            if (filter.getEndDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("attemptCreatedAt"), filter.getEndDate()));
            }

            return predicate;
        };
    }
}
