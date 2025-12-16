package com.popspot.popupplatform.mapper.postgres;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestPostgresMapper {
    int selectOne();
}
