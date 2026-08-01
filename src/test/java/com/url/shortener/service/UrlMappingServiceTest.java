package com.url.shortener.service;

import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.exceptions.ShortUrlTooLongException;
import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import com.url.shortener.repository.ClickEventRepository;
import com.url.shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UrlMappingServiceTest {

    @InjectMocks
    private UrlMappingService urlMappingService;

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    public void testCreateShortUrl() {
        String originalUrl = "https://example.com";
        UrlMapping savedMapping = new UrlMapping();
        savedMapping.setId(1L);
        savedMapping.setOriginalUrl(originalUrl);
        savedMapping.setShortUrl("abcdefgh");
        savedMapping.setUser(testUser);
        savedMapping.setCreatedDate(LocalDateTime.now());
        
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(savedMapping);

        UrlMappingDTO result = urlMappingService.createShortUrl(originalUrl, testUser);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals("abcdefgh", result.getShortUrl());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    public void testGetUrlsByUser() {
        UrlMapping mapping1 = new UrlMapping();
        mapping1.setOriginalUrl("https://site1.com");
        mapping1.setShortUrl("short1");
        mapping1.setUser(testUser);

        when(urlMappingRepository.findByUser(testUser)).thenReturn(Arrays.asList(mapping1));

        List<UrlMappingDTO> results = urlMappingService.getUrlsByUser(testUser);

        assertEquals(1, results.size());
        assertEquals("short1", results.get(0).getShortUrl());
    }

    @Test
    public void testDeleteShortUrl_Success() {
        String shortUrl = "short";
        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setUser(testUser);

        when(urlMappingRepository.findByShortUrlAndUser(shortUrl, testUser)).thenReturn(Optional.of(mapping));

        boolean result = urlMappingService.deleteShortUrl(shortUrl, testUser);

        assertTrue(result);
        verify(clickEventRepository).deleteByUrlMapping(mapping);
        verify(urlMappingRepository).delete(mapping);
    }

    @Test
    public void testDeleteShortUrl_NotFound() {
        String shortUrl = "short";

        when(urlMappingRepository.findByShortUrlAndUser(shortUrl, testUser)).thenReturn(Optional.empty());

        boolean result = urlMappingService.deleteShortUrl(shortUrl, testUser);

        assertFalse(result);
    }

    @Test
    public void testGetOriginalUrl() {
        String shortUrl = "short";
        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setOriginalUrl("https://example.com");
        mapping.setClickCount(5);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(mapping);

        UrlMapping result = urlMappingService.getOriginalUrl(shortUrl);

        assertNotNull(result);
        assertEquals(6, result.getClickCount()); // Assert click count incremented
        verify(urlMappingRepository).save(mapping);
        verify(clickEventRepository).save(any(ClickEvent.class));
    }
}
