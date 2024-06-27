package store.novabook.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import lombok.extern.slf4j.Slf4j;
import store.novabook.gateway.filter.CustomGlobalFilter;
import store.novabook.gateway.filter.ResponseHeaderLoggingFilter;

@Slf4j
@Configuration
public class FilterConfig {

    @Bean
    public GlobalFilter customFilter() {
        return new CustomGlobalFilter();
    }

    // @Bean
    // public GlobalFilter responseHeaderLoggingFilter() {
    //     return new ResponseHeaderLoggingFilter();
    // }

}
