package com.pajamas.common.config;


import com.pajamas.common.advice.CommonExceptionAdvice;
import com.pajamas.common.advice.WrapperResponseBodyAdvice;
import com.pajamas.common.filters.RequestIdFilter;
import com.pajamas.common.interceptor.UseInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
@ConditionalOnClass({DispatcherServlet.class, Filter.class})
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UseInfoInterceptor());
    }
    @Bean
    public CommonExceptionAdvice commonExceptionAdvice(){
        return new CommonExceptionAdvice();
    }
    @Bean
    public WrapperResponseBodyAdvice wrapperResponseBodyAdvice(){
        return new WrapperResponseBodyAdvice();
    }
    @Bean
    public RequestIdFilter requestIdFilter(){
        return new RequestIdFilter();
    }
}
