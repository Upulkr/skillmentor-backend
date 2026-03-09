package com.skillmentor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.bucket:skillmentor-images}")
    private String bucketName;

    private final RestTemplate restTemplate;

    public StorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Upload an image to Supabase Bucket
     * 
     * @param file:   The file from MultipartRequest
     * @param folder: The sub-folder inside the bucket (e.g. "profiles", "subjects")
     * @return The public URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (supabaseUrl == null || supabaseUrl.isEmpty() || supabaseKey == null || supabaseKey.isEmpty()) {
            log.warn("Supabase Storage credentials not configured. Returning null.");
            return null;
        }

        try {
            String fileName = UUID.randomUUID() + "_"
                    + Objects.requireNonNull(file.getOriginalFilename()).replaceAll("\\s+", "_");
            String path = folder + "/" + fileName;

            // Supabase Upload API URL
            String uploadUrl = supabaseUrl + "/object/" + bucketName + "/" + path;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            String contentType = file.getContentType();
            headers.setContentType(MediaType.parseMediaType(
                    contentType != null ? contentType : "application/octet-stream"));

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

            log.info("Uploading file to Supabase: {}", uploadUrl);

            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Public URL (Assumes bucket is public)
                // Format: https://[ID].supabase.co/storage/v1/object/public/[bucket]/[path]
                return supabaseUrl + "/object/public/" + bucketName + "/" + path;
            } else {
                log.error("Failed to upload to Supabase: {}", response.getBody());
                return null;
            }

        } catch (IOException e) {
            log.error("IO Exception during file upload: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during file upload: {}", e.getMessage());
            return null;
        }
    }
}
