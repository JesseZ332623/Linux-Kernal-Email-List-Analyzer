package com.jesse.linux_kernel_email_list_analyzer.config;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import org.apache.hc.client5.http.classic.HttpClient;

/** Spring RestTemplate 配置类。*/
@Configuration
public class RestTemplateConfig
{
    @Bean
    public RestTemplate restTemplate()
    {
        final HttpClient httpClient
            = HttpClientBuilder.create().build();

        final HttpComponentsClientHttpRequestFactory factory
            = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}