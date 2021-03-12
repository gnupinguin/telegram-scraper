package io.github.gnupinguin.tlgscraper.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@ComponentScan
@Configuration
public class DbConfiguration {

    //TODO optimize it https://www.baeldung.com/hikaricp
    //https://techcommunity.microsoft.com/t5/azure-database-for-postgresql/steps-to-install-and-setup-pgbouncer-connection-pooling-proxy/ba-p/730555

    @Bean
    public HikariConfig hikariConfig(DbProperties dbProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbProperties.getUrl());
        config.setUsername(dbProperties.getUsername());
        config.setPassword(dbProperties.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }

    @Bean
    public DataSource hikariDataSource(HikariConfig config) {
        return new HikariDataSource(config);
    }

}
