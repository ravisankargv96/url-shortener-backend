package com.url.shortener.repository;

import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    List<ClickEvent> findByUrlMappingAndClickDateBetween(UrlMapping urlmapping, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT ce FROM ClickEvent ce WHERE ce.urlMapping IN :urlMappings AND ce.clickDate BETWEEN :startDate AND :endDate")
    List<ClickEvent> findByUrlMappingAndClickDateBetween(
            @Param("urlMappings") List<UrlMapping> urlMappings,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ce FROM ClickEvent ce WHERE ce.urlMapping IN :urlMappings AND ce.clickDate BETWEEN :startDateTime AND :endDateTime")
    List<ClickEvent> findClicksByDateAndTimeRange(
            @Param("urlMappings") List<UrlMapping> urlMappings,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    // Delete all click events linked to a specific URL mapping
    void deleteByUrlMapping(UrlMapping urlMapping);
}
