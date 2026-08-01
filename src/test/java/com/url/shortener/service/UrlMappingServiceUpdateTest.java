package com.url.shortener.service;

import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.exceptions.ShortUrlTooLongException;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import com.url.shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UrlMappingServiceUpdateTest {

    @InjectMocks
    private UrlMappingService urlMappingService;

    @Mock
    private UrlMappingRepository urlMappingRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    public void testUpdateShortUrl() {
        String shortUrl = "oldShort";
        String newShortUrl = "newShort";

        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setId(1L);
        existingMapping.setShortUrl(shortUrl);
        existingMapping.setUser(testUser);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(existingMapping);
        when(urlMappingRepository.existsByShortUrl(newShortUrl)).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(existingMapping);

        UrlMappingDTO result = urlMappingService.updateShortUrl(shortUrl, newShortUrl, testUser);

        assertEquals(newShortUrl, result.getShortUrl());
    }

    @Test
    public void testUpdateShortUrl_Unauthorized() {
        String shortUrl = "oldShort";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setId(1L);
        User wrongUser = new User();
        wrongUser.setUsername("wronguser");
        existingMapping.setUser(wrongUser);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(existingMapping);

        assertThrows(RuntimeException.class, () -> {
            urlMappingService.updateShortUrl(shortUrl, "newShort", testUser);
        });
    }

    @Test
    public void testUpdateShortUrl_AlreadyExists() {
        String shortUrl = "oldShort";
        String newShortUrl = "newShort";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setId(1L);
        existingMapping.setUser(testUser);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(existingMapping);
        when(urlMappingRepository.existsByShortUrl(newShortUrl)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            urlMappingService.updateShortUrl(shortUrl, newShortUrl, testUser);
        });
    }

    @Test
    public void testUpdateShortUrl_TooLong() {
        String shortUrl = "oldShort";
        String newShortUrl = "newShortUrlWayTooLong";
        UrlMapping existingMapping = new UrlMapping();
        existingMapping.setId(1L);
        existingMapping.setUser(testUser);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(existingMapping);
        when(urlMappingRepository.existsByShortUrl(newShortUrl)).thenReturn(false);

        assertThrows(ShortUrlTooLongException.class, () -> {
            urlMappingService.updateShortUrl(shortUrl, newShortUrl, testUser);
        });
    }
}

