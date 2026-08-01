package com.url.shortener.service;

import com.url.shortener.dtos.ClickEventDTO;
import com.url.shortener.dtos.UrlMappingDTO;
import com.url.shortener.exceptions.ShortUrlTooLongException;
import com.url.shortener.models.ClickEvent;
import com.url.shortener.models.UrlMapping;
import com.url.shortener.models.User;
import com.url.shortener.repository.ClickEventRepository;
import com.url.shortener.repository.UrlMappingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional; //
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    /**
     * Creates a new short URL mapping for the given original URL and associating it with the user.
     * Generates a random 8-character string for the short URL.
     *
     * @param originalUrl the long URL to be shortened
     * @param user        the user creating the short URL
     * @return UrlMappingDTO containing the saved URL mapping details
     */
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl(); //call to generateShortUrl() method
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    /**
     * Converts a UrlMapping entity to a UrlMappingDTO.
     *
     * @param urlMapping the entity to convert
     * @return the mapped Data Transfer Object
     */
    private UrlMappingDTO convertToDto(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }

    /**
     * Generates a random 8-character alphanumeric string.
     *
     * @return the generated short URL string
     */
    private String generateShortUrl() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);
        for(int i=0; i<8; i++){
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    /**
     * Retrieves all URL mappings associated with a specific user.
     *
     * @param user the user whose URLs are being retrieved
     * @return List of UrlMappingDTOs belonging to the user
     */
    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Deletes a given short URL and its associated click events, provided the user owns it.
     *
     * @param shortUrl the short URL to delete
     * @param user     the user requesting the deletion
     * @return true if deletion is successful, false if not found or unauthorized
     */
    @Transactional
    public boolean deleteShortUrl(String shortUrl, User user) {
        Optional<UrlMapping> urlMappingOpt = urlMappingRepository.findByShortUrlAndUser(shortUrl, user);

        if (urlMappingOpt.isPresent()) {
            UrlMapping urlMapping = urlMappingOpt.get();

            // Delete related click events first
            clickEventRepository.deleteByUrlMapping(urlMapping);

            // Now delete the URL mapping itself
            urlMappingRepository.delete(urlMapping);

            return true;
        }
        return false;
    }

    /**
     * Retrieves grouped click events (by Date) for a specific short URL within a time range.
     *
     * @param shortUrl the short URL mapping to query
     * @param start    the start datetime boundary
     * @param end      the end datetime boundary
     * @return List of ClickEventDTOs containing the date and click counts
     * @throws Throwable if parsing or retrieval errors occur
     */
    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) throws Throwable {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        if(urlMapping!=null){

            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end)
                    .stream()
                    .collect(Collectors
                            .groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Retrieves total clicks for all URLs owned by a specific user, grouped by date.
     *
     * @param user  the user whose global URL clicks are being aggregated
     * @param start the start date boundary
     * @param end   the end date boundary
     * @return A map of Dates as keys and their respective total click counts as values
     */
    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {

        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()));

    }

    /**
     * Looks up the original URL mapped to a short URL, increments its click count,
     * and asynchronously records a click event.
     *
     * @param shortUrl the short URL accessed
     * @return the associated UrlMapping entity, or null if not found
     */
    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
                //.orElseThrow(() -> new RuntimeException("not found")); //new
        if (urlMapping != null) {
            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);

            // Record click event
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEventRepository.save(clickEvent);
        }
        return urlMapping;
    }

    /**
     * Retrieves total clicks for all URLs owned by a specific user, grouped by exact DateTime.
     *
     * @param user  the associated owner
     * @param start the start datetime boundary
     * @param end   the end datetime boundary
     * @return A map of exact DateTimes and their respective click counts
     */
    public Map<LocalDateTime, Long> getTotalClicksByUserAndDateTime(User user, LocalDateTime start, LocalDateTime end) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);

        List<ClickEvent> clickEvents = clickEventRepository.findClicksByDateAndTimeRange(urlMappings, start, end);

        return clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate(), Collectors.counting()));
    }

    /**
     * Updates an existing short URL with a custom alias provided by the user.
     * Validates user ownership, uniqueness of the custom URL, and string length bounds.
     *
     * @param shortUrl    the existing short URL string
     * @param newShortUrl the required custom alias
     * @param user        the user requesting the change
     * @return UrlMappingDTO of the updated mapping
     * @throws RuntimeException          if unauthorized or if the custom URL is already in use
     * @throws ShortUrlTooLongException  if the requested alias exceeds 15 characters
     */
    public UrlMappingDTO updateShortUrl(String shortUrl, String newShortUrl, User user) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);

        // validate the user ownership
        if(!urlMapping.getUser().getUsername().equals(user.getUsername())){
            throw new RuntimeException("User is not allowed");
        }

        //check the custom url for uniquenesss
        if(urlMappingRepository.existsByShortUrl(newShortUrl)){
            throw new RuntimeException("This url is already taken, please try another one!");
        }

        //
        if(newShortUrl.length()>15){
            throw new ShortUrlTooLongException("The custom url should be under 15 characters");
        }

        //update the generated short url
        urlMapping.setShortUrl(newShortUrl);
        UrlMapping updatedMapping = urlMappingRepository.save(urlMapping);

        return convertToDto(updatedMapping);
    }
}
