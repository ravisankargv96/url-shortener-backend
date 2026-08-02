package com.url.shortener.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
* Controller to handle the root endpoint health check.
* This is primarily used by hosting services (like Vercel or Render)
* to verify that the application container is up and running successfully.
*/
@RestController
public class HealthController {


   @GetMapping("/")
   public ResponseEntity<String> healthCheck() {
       return ResponseEntity.ok("Backend is running successfully!");
   }
}


