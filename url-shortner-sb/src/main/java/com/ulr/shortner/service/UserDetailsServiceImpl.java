package com.ulr.shortner.service;

import com.ulr.shortner.models.User;
import com.ulr.shortner.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user  =  userRepository.findBuUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("User not found With name: " + username));

        return UserDetailsImpl.build(user);


    }

}
