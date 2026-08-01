package com.url.shortener.service;

import com.url.shortener.dtos.ClickEventDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class UrlMappingServiceAnalyticsTest {

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
    public void testGetTotalClicksByUserAndDate() {
        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setUser(testUser);

        ClickEvent click1 = new ClickEvent();
        click1.setClickDate(LocalDateTime.now());
        click1.setUrlMapping(mapping);

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        when(urlMappingRepository.findByUser(testUser)).thenReturn(Arrays.asList(mapping));
        when(clickEventRepository.findByUrlMappingAndClickDateBetween(eq(Arrays.asList(mapping)), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(click1));

        Map<LocalDate, Long> result = urlMappingService.getTotalClicksByUserAndDate(testUser, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(LocalDate.now()));
        assertEquals(1L, result.get(LocalDate.now()));
    }

    @Test
    public void testGetTotalClicksByUserAndDateTime() {
        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setUser(testUser);

        LocalDateTime now = LocalDateTime.now();
        ClickEvent click1 = new ClickEvent();
        click1.setClickDate(now);
        click1.setUrlMapping(mapping);

        LocalDateTime startDateTime = now.minusHours(1);
        LocalDateTime endDateTime = now.plusHours(1);

        when(urlMappingRepository.findByUser(testUser)).thenReturn(Arrays.asList(mapping));
        when(clickEventRepository.findClicksByDateAndTimeRange(eq(Arrays.asList(mapping)), eq(startDateTime), eq(endDateTime)))
                .thenReturn(Arrays.asList(click1));

        Map<LocalDateTime, Long> result = urlMappingService.getTotalClicksByUserAndDateTime(testUser, startDateTime, endDateTime);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(now));
        assertEquals(1L, result.get(now));
    }

    @Test
    public void testGetClickEventsByDate() throws Throwable {
        String shortUrl = "short";
        UrlMapping mapping = new UrlMapping();
        mapping.setId(1L);
        mapping.setUser(testUser);

        LocalDateTime now = LocalDateTime.now();
        ClickEvent click1 = new ClickEvent();
        click1.setClickDate(now);
        click1.setUrlMapping(mapping);

        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);

        when(urlMappingRepository.findByShortUrl(shortUrl)).thenReturn(mapping);
        when(clickEventRepository.findByUrlMappingAndClickDateBetween(mapping, startDate, endDate))
                .thenReturn(Arrays.asList(click1));

        List<ClickEventDTO> result = urlMappingService.getClickEventsByDate(shortUrl, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCount());
        assertEquals(now.toLocalDate(), result.get(0).getClickDate());
    }
}

