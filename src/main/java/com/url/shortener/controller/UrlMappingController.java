package com.url.shortener.controller;

import com.url.shortener.dtos.ClickEventDTO;
import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.exceptions.ShortUrlTooLongException;
import com.url.shortener.models.User;
import com.url.shortener.service.UrlMappingService;
import com.url.shortener.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlMappingController {

    //Long Url --> short url(8 character)

    @Autowired
    private UrlMappingService urlMappingService;

    @Autowired
    private UserService userService;

    /**
     * Creates a new short URL map for a given original URL.
     *
     * @param request JSON payload containing the "originalUrl"
     * @param principal the authenticated user context
     * @return ResponseEntity containing the mapped URL details
     */
    @PostMapping(value = "/shorten", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request,
                                                        Principal principal){
        String originalUrl = request.get("originalUrl");
        User user = userService.findByUsername(principal.getName());
        //Call Service
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);

    }

    /**
     * Deletes a specific short URL mapping owned by the authenticated user.
     *
     * @param shortUrl the 8-character ID representing the map
     * @param principal the authenticated user context
     * @return ResponseEntity signifying success or unauthorized/Not found
     */
    @DeleteMapping("/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteShortUrl(@PathVariable String shortUrl, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        boolean deleted = urlMappingService.deleteShortUrl(shortUrl, user);

        if (deleted) {
            return ResponseEntity.ok("Short URL deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found or unauthorized.");
        }
    }

    /**
     * Retrieves all short URLs mapped by the authenticated user.
     *
     * @param principal the authenticated user context
     * @return ResponseEntity with a list of UrlMappingDTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal){
        //getting the user with help of security context
        User user = userService.findByUsername(principal.getName());
        //getting urls for a particular user
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }

    /**
     * Aggregates click events for a specific URL between a given date range.
     *
     * @param shortUrl the 8-character mapped alias
     * @param startDate standard ISO localized time (e.g., 2025-12-01T00:00:00)
     * @param endDate standard ISO localized time expected boundary format
     * @return List of DTOs outlining click metadata records
     */
    @GetMapping("/{shortUrl}/analytics")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl,
                                                               @RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate) throws Throwable {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        // 2025-12-01T00:00:00
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<ClickEventDTO> clickEventDTOS = urlMappingService.getClickEventsByDate(shortUrl, start, end);
        return ResponseEntity.ok(clickEventDTOS);
    }

    /**
     * Retrieves aggregated total clicks partitioned globally by LocalDate tracking ranges.
     * 
     * @param principal the authenticated user context
     * @param startDate beginning boundary string (yyyy-MM-dd)
     * @param endDate ending boundary string (yyyy-MM-dd)
     * @return Date mapped key-value pairs representing metrics
     */
    @GetMapping("/total-clicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(Principal principal,
                                                                     @RequestParam("startDate") String startDate,
                                                                     @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        User user = userService.findByUsername(principal.getName());
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        Map<LocalDate, Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);

    }

    /**
     * Generates extremely granular deep-date time metrics indicating tracking behaviors.
     */
    @GetMapping("/total-clicks-by-datetime")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDateTime, Long>> getTotalClicksByDateTime(
            Principal principal,
            @RequestParam("startDateTime") String startDateTime,
            @RequestParam("endDateTime") String endDateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        User user = userService.findByUsername(principal.getName());
        LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);
        Map<LocalDateTime, Long> clickData = urlMappingService.getTotalClicksByUserAndDateTime(user, start, end);
        return ResponseEntity.ok(clickData);
    }

    /**
     * Replaces standard system-generated aliases with customized brownfield user alias requests.
     * Restricts inputs overriding 15 characters.
     */
    @PutMapping("/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> updateShortUrl(
            @PathVariable String shortUrl,
            @RequestBody Map<String, String> request,
            Principal principal) {

        String newShortUrl = request.get("newShortUrl");
        User user = userService.findByUsername(principal.getName());

        try{
            UrlMappingDTO updatedMapping = urlMappingService.updateShortUrl(shortUrl, newShortUrl, user);
            return ResponseEntity.ok(updatedMapping);
        } catch (ShortUrlTooLongException ex){
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body((UrlMappingDTO) Collections.singletonMap("error", "Custom short URL must not exceed 15 characters."));

        } catch (RuntimeException ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((UrlMappingDTO) Collections.singletonMap("error", ex.getMessage()));
        }
    }
}
