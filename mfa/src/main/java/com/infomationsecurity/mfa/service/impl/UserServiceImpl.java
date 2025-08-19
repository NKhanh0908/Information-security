package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.userDTO.UserUpdateDTO;
import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.User;
import com.infomationsecurity.mfa.mapper.UserMapper;
import com.infomationsecurity.mfa.repository.UserRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final AccountService accountService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           @Lazy AccountService accountService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountService = accountService;
    }

    @Override
    public User create(User user) {
        log.info("Creating user with username: {}", user.getUserName());

        return userRepository.save(user);
    }

    @Override
    public UserDTO update(UserUpdateDTO userUpdateDTO) {
        return null;
    }

    @Override
    public UserDTO getById(Integer userId) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public UserDTO getCurrentUser() {
        log.info("Fetching current user information");
        AccountDTO account = accountService.getAccountAuth();

        return userMapper.entityToDTO(
                userRepository.findByAccountId(account.getAccountId()));
    }
}
