package com.ulr.shortner.repository;

import com.ulr.shortner.models.ClickEvent;
import com.ulr.shortner.models.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent , Long> {
    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping mapping , LocalDateTime startDate , LocalDateTime endDate);
    List<ClickEvent> findByUrlMappingInClickDateBetween(List<UrlMapping> urlmappings , LocalDateTime startDate , LocalDateTime endDate);
}



