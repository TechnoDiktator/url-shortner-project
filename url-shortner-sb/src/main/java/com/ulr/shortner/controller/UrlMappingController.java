package com.ulr.shortner.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ulr.shortner.dtos.UrlMappingDto;
import com.ulr.shortner.models.User;
import com.ulr.shortner.service.UserService;

import lombok.AllArgsConstructor;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/url")
@AllArgsConstructor
public class UrlMappingController {

    private final com.ulr.shortner.service.UrlMappingService urlMappingService;
    private final UserService userService;

    //what is Principal here?
    // It represents the currently authenticated user



    // {"originalUrl" : "http://example.com/some/long/url"}
    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDto> createShortUrl(@RequestBody Map<String , String> request , Principal principal){
        String originalUrl = request.get("originalUrl");
        
        User user = userService.findByUsername(principal.getName());

        UrlMappingDto urlMappingDTO = urlMappingService.createShortUrl(originalUrl , user);
        return ResponseEntity.ok(urlMappingDTO);
    }
    

}
