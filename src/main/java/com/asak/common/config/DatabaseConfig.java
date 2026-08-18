package com.asak.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {"com.asak.admin.mapper", "com.asak.user.mapper"})
public class DatabaseConfig {}
