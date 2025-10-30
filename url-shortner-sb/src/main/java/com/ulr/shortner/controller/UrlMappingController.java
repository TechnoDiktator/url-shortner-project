package com.ulr.shortner.controller;

import com.ulr.shortner.dtos.ClickEventDto;
import com.ulr.shortner.models.ClickEvent;
import org.springframework.web.bind.annotation.*;

import com.ulr.shortner.dtos.UrlMappingDto;
import com.ulr.shortner.models.User;
import com.ulr.shortner.service.UserService;

import lombok.AllArgsConstructor;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;


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
    
    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDto>> getUserUrls(Principal principal){
        User user = userService.findByUsername(principal.getName());
        List<UrlMappingDto> urlMappings = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urlMappings);

    }


    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDto>> getUrlAnalytics(@PathVariable String shorturl,
            @RequestParam("startDate") String startDate
            , @RequestParam("endDate") String endDate){

        DateTimeFormatter formatter  = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        //2025-12-01T00:00:00

        LocalDateTime start =  LocalDateTime.parse(startDate , formatter);
        LocalDateTime end   =  LocalDateTime.parse(endDate , formatter);

        List<ClickEventDto> clickEventDtos = urlMappingService.getClickEventsByDate(shorturl , start , end);
        return ResponseEntity.ok(clickEventDtos);
    }

    

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate , Long>> getTotalClicksByDate(Principal principal,
            @RequestParam("startDate") String startDate
            , @RequestParam("endDate") String endDate){

        DateTimeFormatter formatter  = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        //2025-12-01T00:00:00

        LocalDateTime start =  LocalDateTime.parse(startDate , formatter);
        LocalDateTime end   =  LocalDateTime.parse(endDate , formatter);

        User user = userService.findByUsername(principal.getName());

        Map<LocalDate , Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user , start , end);
        return ResponseEntity.ok(totalClicks);  

    }



}



