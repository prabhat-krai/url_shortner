package com.chutku.net.controller;

import com.chutku.net.dal.UrlMappingRepository;
import com.chutku.net.model.CreateShortened;
import com.chutku.net.model.UrlMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

@Controller
public class HomeController {

  private final UrlMappingRepository urlMappingRepository;

  public HomeController(UrlMappingRepository urlMappingRepository) {
    this.urlMappingRepository = urlMappingRepository;
  }

  @GetMapping("/home")
  public String home(CreateShortened createShortened) {
    return "home";
  }

  @GetMapping("/")
  public String nothing(CreateShortened createShortened) {
    return "home";
  }

  @PostMapping
  public Mono<String> addShort(CreateShortened createShortened, Model model) {
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
        .map(srt -> {
              model.addAttribute("message", "http://prabhatrai.com/" + srt.shortKey());
              return "home";
            }

        ).onErrorResume(
            k -> {
              model.addAttribute("message", "Could not create a short key for this.");
              return Mono.just("home");
            }
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
