package com.popspot.popupplatform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class aCon {
    private final PostgresTestService postgresTestService;

    @GetMapping("/test/postgres")
    public String testPostgres() {
        int result = postgresTestService.test();
        return "Postgres OK, result = " + result;
    }
}
