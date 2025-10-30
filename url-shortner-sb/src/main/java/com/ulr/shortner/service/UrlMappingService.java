package com.ulr.shortner.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.ulr.shortner.dtos.ClickEventDto;
import com.ulr.shortner.repository.ClickEventRepository;
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
    private ClickEventRepository clickEventRepository;
    public UrlMappingDto createShortUrl(String originalUrl, User user) {
        
        String shortUrl = generateShortUrl(originalUrl);

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        
        UrlMapping savedUrlMapping =  urlMappingRepository.save(urlMapping); // urlMappingRepository.save(urlMapping); (Assuming repository save operation)
    
        return convertToDto(savedUrlMapping);
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
        //what is the significance of this line
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random  =  new Random();
        StringBuilder shortUrl =  new StringBuilder(0);
        for(int i  = 0 ; i<8 ; i++){
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    public List<UrlMappingDto> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto).toList();
    }

    public List<ClickEventDto> getClickEventsByDate(String shorturl, LocalDateTime start, LocalDateTime end) {
        //we are goin to get the click events from the repo and then reun the corresponding dtos

        UrlMapping urlMapping   =  urlMappingRepository.findByShortUrl(shorturl);


        //so what are we trying to do here
        //we are trying to get the click events for a particular url mapping between the start and end date
        //then we are grouping them by date and counting the number of clicks for each date
        //then we are mapping them to the dto
        //finally we are collecting them to a list and returning them
        if(urlMapping != null){
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping , start , end).stream()
                    .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate()  , Collectors.counting() ))
                    .entrySet().stream().map(entry -> {
                        ClickEventDto clickEventDto =  new ClickEventDto();
                        clickEventDto.setClickDate(entry.getKey());
                        clickEventDto.setCount(entry.getValue());
                        return clickEventDto;
                    })
                    .collect(Collectors.toList());


        }
        return null;

    }
}
