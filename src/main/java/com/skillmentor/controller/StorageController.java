package com.skillmentor.controller;

import com.skillmentor.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        String url = storageService.uploadImage(file, folder);

        if (url != null) {
            return ResponseEntity.ok(Map.of("url", url));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to upload file to Supabase"));
        }
    }
}
