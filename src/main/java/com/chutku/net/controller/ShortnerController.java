package com.chutku.net.controller;

import com.chutku.net.dal.UrlMappingRepository;
import com.chutku.net.model.CreateShortened;
import com.chutku.net.model.UrlMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@RestController
public class ShortnerController {

    private final UrlMappingRepository urlMappingRepository;

    public ShortnerController(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @GetMapping(value = "/api/srt/{short-key}")
    public Mono<ResponseEntity<Object>> getShort(@PathVariable(value = "short-key") String shortKey) {
        return urlMappingRepository.findUrlMappingByShortKey(shortKey)
                .filter((r) -> !r.url().isEmpty())
                .map(UrlMapping::url)
                .map(url1 -> ResponseEntity
                        .status(301)
                        .header("Location", url1)
                        .build())
                .defaultIfEmpty(
                        ResponseEntity.status(404).build()
                ).doOnError(
                        e -> {
                            Logger.getAnonymousLogger().info("Did not find the requested short key " + shortKey);
                        }
                ).onErrorReturn(
                        ResponseEntity.status(422).build()
                );
    }

    @PostMapping("/api/srt")
    public Mono<ResponseEntity<String>> addShort(@RequestBody CreateShortened createShortened) {
        String shortKey = shortenString(createShortened.url());
        return urlMappingRepository.findUrlMappingByUrl(createShortened.url())
            .switchIfEmpty(
                urlMappingRepository.save(new UrlMapping(null, createShortened.url(), shortKey))
                    .onErrorResume(
                        (t) -> urlMappingRepository.save(
                            new UrlMapping(null, createShortened.url(), shortKey + "a")
                        )
                    )
            )
            .map(srt -> ResponseEntity.ok(srt.shortKey()))
            .onErrorReturn(
                ResponseEntity.status(422).build()
            );
    }

    public static String shortenString(String input) {
        // Instance of the SHA-256 digest
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // Perform the hash operation, which returns the hashed bytes
        byte[] hashBytes = digest.digest(input.getBytes());

        // Convert bytes to hex format
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hashBytes.length; i++) {
            String hex = Integer.toHexString(0xff & hashBytes[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        // Return the first 8 characters of the hex string for brevity
        return hexString.substring(0, 8);
    }
}
