package com.thousif.trading.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("com.thousif.trading.platform.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class DatabaseConfig {
}
