package com.ulr.shortner.repository;

import com.ulr.shortner.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User , Long> {

    Optional<User> findBuUsername(String username);



}
