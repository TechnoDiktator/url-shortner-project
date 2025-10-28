package com.ulr.shortner.service;

import com.ulr.shortner.models.User;
import com.ulr.shortner.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;


    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);


    }


}
