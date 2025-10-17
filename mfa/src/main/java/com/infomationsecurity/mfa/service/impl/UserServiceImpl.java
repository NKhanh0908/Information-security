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
        // Đây là luôn lấy User dựa theo account hiện tại trong SecurityContext, chứ không dựa vào JSON gửi đi .
        // // Lấy ra account hiện tại 
        // AccountDTO acc = accountService.getAccountAuth(); // phương thức lấy ra account hiện tại
        // // Tìm thông tin account thông qua id của user
        // User oldUser = userRepository.findByAccountId(acc.getAccountId()); // Thông tin user thể hiện đầy đủ trong file user bên trong entity
    
        // // Kiểm tra xem có tìm ra thông tin user hay không
        // if(oldUser == null){
        //     throw new RuntimeException("User not found for account id: " + acc.getAccountId()); // Thông báo không tìm thấy thông tin user
        // }

        // // Không phải thông tin nào cũng sẽ thay đổi, kiểm tra xem thông tin nào cần thay đổi sẽ cập nhật lại còn bỏ trống sẽ chỉ cập nhật những thông tin truyền vào 
        // // Phương thức isBlank để kiểm tra trường nhập liệu kiểu String có empty hay không, chỉ kiểm tra được kiểu String
        // if (userUpdateDTO.getUserName() != null && !userUpdateDTO.getUserName().isBlank()) {
        //     oldUser.setUserName(userUpdateDTO.getUserName());
        // }

        
        // Đây là cách xử lí khi gửi thông tin user theo JSON
        User oldUser = getUser();
        if (oldUser == null) {
            throw new RuntimeException("User not found: " + userUpdateDTO.getUserName());
        }

        if(userUpdateDTO.getUserName() != null && !userUpdateDTO.getUserName().equals(oldUser.getUserName())) {
            oldUser.setUserName(userUpdateDTO.getUserName());
        }
        
        if (userUpdateDTO.getUserGender() != null) {
            oldUser.setUserGender(userUpdateDTO.getUserGender());
        }
        
        if (userUpdateDTO.getUserDateOfBirth() != null) {
            oldUser.setUserDateOfBirth(userUpdateDTO.getUserDateOfBirth());
        }
        
        if (userUpdateDTO.getUserAddress() != null && !userUpdateDTO.getUserAddress().isBlank()) {
            oldUser.setUserAddress(userUpdateDTO.getUserAddress());
        }
        
        if (userUpdateDTO.getUserPhone() != null && !userUpdateDTO.getUserPhone().isBlank()) {
            oldUser.setUserPhone(userUpdateDTO.getUserPhone());
        }
        

        // Cập nhật thông tin user mới sau khi thay đổi
        User newUser = userRepository.save(oldUser);
        return userMapper.entityToDTO(newUser); // Chuyển đổi entity sang DTO sau khi cập nhật thông tin và sẽ luôn trả về đúng với thông tin trên UserDTO
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

        return userMapper.entityToDTO(getUser());
    }

    private User getUser(){
        AccountDTO account = accountService.getAccountAuth();
     return userRepository.findByAccountId(account.getAccountId());
    }
}
