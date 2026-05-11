package com.tuyensinh.web.config;

import com.tuyensinh.web.filter.AuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> reg = new FilterRegistrationBean<>(new AuthFilter());
        reg.addUrlPatterns("/tra-cuu-ket-qua");
        reg.setName("authFilter");
        return reg;
    }
}
