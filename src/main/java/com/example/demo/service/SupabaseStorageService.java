package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String bucket;

    public SupabaseStorageService(WebClient.Builder webClientBuilder,
                                  @Value("${supabase.url}") String supabaseUrl,
                                  @Value("${supabase.bucket}") String bucket,
                                  @Value("${supabase.apiKey}") String apiKey) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.webClient = webClientBuilder
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "-" + cleanFileName(file.getOriginalFilename());
            MediaType contentType = file.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(file.getContentType());

            webClient.post()
                    .uri("/storage/v1/object/{bucket}/{fileName}", bucket, fileName)
                    .contentType(contentType)
                    .bodyValue(new ByteArrayResource(file.getBytes()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return getPublicUrl(fileName);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the uploaded file", exception);
        }
    }

    public void delete(String fileName) {
        webClient.delete()
                .uri("/storage/v1/object/{bucket}/{fileName}", bucket, fileName)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public String getPublicUrl(String fileName) {
        String encodedFileName = UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + encodedFileName;
    }

    private String cleanFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "upload";
        }
        return originalFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
