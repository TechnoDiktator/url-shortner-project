package com.ulr.shortner.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ulr.shortner.models.UrlMapping;
import com.ulr.shortner.service.UrlMappingService;

import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//What does all args constructor do here?
//It generates a constructor with 1 parameter for each field in your class.
//In this case, it will create a constructor that takes UrlMappingService as a parameter.
//and inject it when creating an instance of RedirectController.


@RestController
@AllArgsConstructor
public class RedirectController {
    private UrlMappingService urlMappingService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        // Implementation for redirecting to the original URL
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);

        if(urlMapping != null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(httpHeaders).build();
        }
        return ResponseEntity.notFound().build();
    }
    
}
