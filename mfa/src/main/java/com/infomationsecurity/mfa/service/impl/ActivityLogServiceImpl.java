package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.fiters.ActivityLogFilter;
import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.entity.ActivityLog;
import com.infomationsecurity.mfa.mapper.AccountMapper;
import com.infomationsecurity.mfa.mapper.ActivityLogMapper;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.repository.ActivityLogRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.ActivityLogService;
import com.infomationsecurity.mfa.specification.ActivityLogSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    private final ActivityLogMapper activityLogMapper;

    private final AccountService accountService;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  ActivityLogMapper activityLogMapper,
                                  @Lazy AccountService accountService) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
        this.accountService = accountService;
    }


    /**
     * @param accountId
     * @param deviceId
     * @param logAction
     */
    @Override
    public void createActivityLog(Integer accountId, Integer deviceId, String logAction) {

    }

    /**
     * @param filter
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageDTO<ActivityLogDTO> getActivityLogs(ActivityLogFilter filter, Integer page, Integer size) {
        log.info("Fetching activity logs with filter: {}, page: {}, size: {}", filter, page, size);

        Specification<ActivityLog> activityLogSpecification = ActivityLogSpecification.filter(filter);

        Pageable pageable = PageRequest.of(page, size);

        Page<ActivityLog> activityLogPage = activityLogRepository.findAll(activityLogSpecification, pageable);

        return PageDTO.<ActivityLogDTO>builder()
                .content(activityLogPage.getContent().stream()
                        .map(activityLogMapper::entityToDTO)
                        .toList())
                .page(page)
                .size(size)
                .totalElements(activityLogPage.getTotalElements())
                .totalPages(activityLogPage.getTotalPages())
                .build();
    }
}
