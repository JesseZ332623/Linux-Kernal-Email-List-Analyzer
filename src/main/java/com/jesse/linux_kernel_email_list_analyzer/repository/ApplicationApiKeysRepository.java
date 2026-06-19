package com.jesse.linux_kernel_email_list_analyzer.repository;

import com.jesse.linux_kernel_email_list_analyzer.entity.ApplicationApiKeys;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** 第三方应用访问 API Keys 表仓库类。*/
@Repository
public interface ApplicationApiKeysRepository
    extends CrudRepository<ApplicationApiKeys, Long>
{
    /** 通过第三方 APP 名查询对应的 API Key。*/
    @Query("""
        SELECT
            api_key
        FROM
            application_api_keys
        WHERE
            application_name = :appName
    """)
    String findByAppName(String appName);
}