package com.infomationsecurity.mfa.specification;

import com.infomationsecurity.mfa.dto.request.fiters.TrustDeviceFilter;
import com.infomationsecurity.mfa.entity.TrustDevice;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class TrustDeviceSpecification {
    public static Specification<TrustDevice> filter(TrustDeviceFilter filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (filter.getAccountId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("account").get("id"), filter.getAccountId()));
            }

            if (filter.getDeviceName() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("deviceName"), "%" + filter.getDeviceName() + "%"));
            }

            if (filter.getDeviceIsActive() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("deviceIsActive"), filter.getDeviceIsActive()));
            }

            if (filter.getDeviceIsVerified() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("deviceIsVerified"), filter.getDeviceIsVerified()));
            }

            if (filter.getFromDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("deviceCreatedAt"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("deviceCreatedAt"), filter.getToDate()));
            }

            return predicate;
        };
    }
}
