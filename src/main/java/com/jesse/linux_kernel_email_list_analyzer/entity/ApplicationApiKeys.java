package com.jesse.linux_kernel_email_list_analyzer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** 第三方应用访问 API Keys 表实体类。*/
@Data
@NoArgsConstructor
@Table(name = "application_api_keys")
public class ApplicationApiKeys
{
    @Id
    private Long id;

    private String applicationName;

    private String apiKey;
}