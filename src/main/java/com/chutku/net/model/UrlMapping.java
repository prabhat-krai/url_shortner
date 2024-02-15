package com.chutku.net.model;

import org.springframework.data.annotation.Id;

public record UrlMapping(@Id Long id, String url, String shortKey) {
}
