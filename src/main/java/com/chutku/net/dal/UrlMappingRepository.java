package com.chutku.net.dal;

import com.chutku.net.model.UrlMapping;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UrlMappingRepository extends ReactiveCrudRepository<UrlMapping, Long> {

    Mono<UrlMapping> findUrlMappingByShortKey(String shortKey);

}
