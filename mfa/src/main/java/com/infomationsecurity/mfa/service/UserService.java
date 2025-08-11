package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.userDTO.UserUpdateDTO;
import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User create(User user);

    UserDTO update(UserUpdateDTO userUpdateDTO);

    UserDTO getById(Integer userId);


}
