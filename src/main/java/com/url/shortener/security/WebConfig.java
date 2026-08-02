package com.url.shortener.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global configuration for Web MVC.
 * Primarily handles Cross-Origin Resource Sharing (CORS) configurations 
 * to allow secure interaction between the separate frontend client and this backend API.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /*@Value("${frontend.url}")
    private String frontendUrl;*/

    /**
     * Defines global CORS mappings using WebMvcConfigurer.
     * 
     * @return a customized WebMvcConfigurer bean instance
     */
    /*@Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendUrl)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }*/
}
