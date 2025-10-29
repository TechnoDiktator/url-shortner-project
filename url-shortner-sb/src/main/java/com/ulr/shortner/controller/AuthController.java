package com.ulr.shortner.controller;


import com.ulr.shortner.dtos.LoginRequest;
import com.ulr.shortner.dtos.RegisterRequest;
import com.ulr.shortner.models.User;
import com.ulr.shortner.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;


    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok( userService.authenticateUser(loginRequest));
    }


    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setRole("ROLE_USER");
            user.setEmail(registerRequest.getEmail());
            
            userService.registerUser(user);
            
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    

    
}



