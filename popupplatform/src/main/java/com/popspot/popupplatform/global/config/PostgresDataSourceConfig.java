package com.popspot.popupplatform.global.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = "com.popspot.popupplatform.mapper.postgres",
        sqlSessionFactoryRef = "postgresSqlSessionFactory"
)
public class PostgresDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.postgres")
    public HikariDataSource postgresDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public SqlSessionFactory postgresSqlSessionFactory(
            @Qualifier("postgresDataSource") DataSource dataSource
    ) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);

        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:/mappers-postgres/**/*.xml")
        );

        // ✅ 동일 적용
        org.apache.ibatis.session.Configuration mybatisConfig =
                new org.apache.ibatis.session.Configuration();
        mybatisConfig.setMapUnderscoreToCamelCase(true);
        factory.setConfiguration(mybatisConfig);

        return factory.getObject();
    }

    @Bean
    public DataSourceTransactionManager postgresTxManager(
            @Qualifier("postgresDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }
}
