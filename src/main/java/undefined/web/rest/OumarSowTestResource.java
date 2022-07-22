package com.mycompany.myapp.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * OumarSowTestResource controller
 */
@RestController
@RequestMapping("/api/oumar-sow-test")
public class OumarSowTestResource {

    private final Logger log = LoggerFactory.getLogger(OumarSowTestResource.class);

    /**
     * POST creation
     */
    @PostMapping("/creation")
    public Mono<String> creation() {
        return Mono.just("creation");
    }

    /**
     * PUT createTest
     */
    @PutMapping("/create-test")
    public Mono<String> createTest() {
        return Mono.just("createTest");
    }
}
