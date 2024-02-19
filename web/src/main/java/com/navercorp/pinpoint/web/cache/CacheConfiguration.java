/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration implements CachingConfigurer {

    public static final String SERVICES_CACHE_NAME = "services";
    public static final String APPLICATIONS_CACHE_NAME = "applications";
    public static final String AGENTS_CACHE_NAME = "agents";
    public static final String API_METADATA_CACHE_NAME = "apiMetaData";
    public static final String APPLICATION_LIST_CACHE_NAME = "applicationNameList";

    @Bean
    @Primary
    public CacheManager cacheManager(
            @Value("${pinpoint.cache.services.expire-ttl:5}") long servicesExpireAfterWrite,
            @Value("${pinpoint.cache.applications.expire-ttl:10}") long applicationsExpireAfterWrite,
            @Value("${pinpoint.cache.agents.expire-ttl:20}") long agentsExpireAfterWrite
    ) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        // Set default cache configuration
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(200)
                .maximumSize(1000));

        // Register custom cache configurations

        caffeineCacheManager.registerCustomCache(API_METADATA_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(600, TimeUnit.SECONDS)
                .initialCapacity(500)
                .maximumSize(10000)
                .build());

        caffeineCacheManager.registerCustomCache(APPLICATION_LIST_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200)
                .build());

        caffeineCacheManager.registerCustomCache(SERVICES_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(servicesExpireAfterWrite, TimeUnit.SECONDS)
                .initialCapacity(1)
                .weakKeys()
                .maximumSize(4)
                .build());

        caffeineCacheManager.registerCustomCache(APPLICATIONS_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(applicationsExpireAfterWrite, TimeUnit.SECONDS)
                .initialCapacity(1000)
                .weakKeys()
                .maximumSize(1000)
                .build());

        caffeineCacheManager.registerCustomCache(AGENTS_CACHE_NAME, Caffeine.newBuilder()
                .expireAfterWrite(agentsExpireAfterWrite, TimeUnit.SECONDS)
                .initialCapacity(10000)
                .maximumSize(10000)
                .build());

        return caffeineCacheManager;
    }

}
