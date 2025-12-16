package com.popspot.popupplatform.controller;

import com.popspot.popupplatform.mapper.postgres.TestPostgresMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostgresTestService {

    private final TestPostgresMapper testPostgresMapper;

    @Transactional(transactionManager = "postgresTxManager", readOnly = true)
    public int test() {
        return testPostgresMapper.selectOne();
    }
}

