package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.fiters.ActivityLogFilter;
import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.entity.ActivityLog;
import com.infomationsecurity.mfa.entity.TrustDevice;
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
    private final String LOG_PREFIX = "[ActivityLogService]:";

    private final ActivityLogRepository activityLogRepository;

    private final ActivityLogMapper activityLogMapper;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  ActivityLogMapper activityLogMapper) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
    }


    /**
     * @param accountId
     * @param trustDevice
     * @param logAction
     */
    @Override
    public void createActivityLog(Integer accountId, TrustDevice trustDevice, String logAction) {
        ActivityLog activityLog = activityLogMapper.toEntity(accountId, trustDevice, logAction);
        activityLogRepository.save(activityLog);
    }

    /**
     * @param filter
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageDTO<ActivityLogDTO> getActivityLogs(ActivityLogFilter filter, Integer page, Integer size) {
        log.info("{} getActivityLogs with filter: {}, page: {}, size: {}", LOG_PREFIX, filter, page, size);

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
