package com.subtracker.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaRepositories(basePackages = ["com.subtracker.repository"])
@EntityScan(basePackages = ["com.subtracker.model"])
@EnableTransactionManagement
class JpaConfig