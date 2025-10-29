package com.ulr.shortner.service;

import java.time.LocalDateTime;

import com.ulr.shortner.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.ulr.shortner.dtos.UrlMappingDto;
import com.ulr.shortner.models.UrlMapping;
import com.ulr.shortner.models.User;

@Service
@AllArgsConstructor
public class UrlMappingService {


    private UrlMappingRepository urlMappingRepository;

    public UrlMappingDto createShortUrl(String originalUrl, User user) {
        
        String shortUrl = generateShortUrl(originalUrl);

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        
        UrlMapping savedUrlMapping =  urlMappingRepository.save(urlMapping); // urlMappingRepository.save(urlMapping); (Assuming repository save operation)
    
        return convertToDto(urlMapping);
    }

    private UrlMappingDto convertToDto(UrlMapping urlMapping){
        UrlMappingDto urlMappingDto =  new UrlMappingDto();
        urlMappingDto.setId(urlMapping.getId());
        urlMappingDto.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDto.setClickCount(urlMapping.getClickCount());
        urlMappingDto.setShortUrl(urlMapping.getShortUrl());
        urlMappingDto.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDto.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDto;
    }

    private String generateShortUrl(String originalUrl) {
        // Simple hash-based short URL generation (for demonstration purposes)
        return Integer.toHexString(originalUrl.hashCode());
    }

}
