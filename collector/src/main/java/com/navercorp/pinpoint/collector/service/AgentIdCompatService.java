/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.collector.dao.AgentIdCompatDao;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Service
public class AgentIdCompatService {

    private static final Logger logger = LogManager.getLogger(AgentIdCompatService.class);

    private final AgentIdCompatDao agentIdCompatDao;

    private final Cache<String, UUID> agentIdCompatCache;

    public AgentIdCompatService(AgentIdCompatDao agentIdCompatDao) {
        this.agentIdCompatDao = Objects.requireNonNull(agentIdCompatDao, "agentIdCompatDao");

        this.agentIdCompatCache = CacheBuilder.newBuilder()
                .initialCapacity(1024)
                .maximumSize(65535)
                .expireAfterAccess(Duration.ofMinutes(5))
                .build();
    }


    /**
     * Decode or compat agentId.
     * @param agentId agentId in old or new format <br>
     *                old format: any string of which length is less than 25 <br>
     *                new format: UrlBase64 encoded UUID
     * @return decoded agentId <br>
     *         if agentId is in old format, it is compatibly managed in the database and returned as UUID <br>
     *         if agentId is in new format, it is decoded and returned as UUID
     */
    public UUID decodeAgentId(String agentId) {
        try {
            return AgentUuidUtils.decode(agentId);
        } catch (IllegalArgumentException e) {
            return compatAgentId(agentId);
        }
    }

    private UUID compatAgentId(String agentId) {
        UUID agentIdFromCompat = this.getAgentIdFromCompat(agentId);
        if (agentIdFromCompat == null) {
            throw new RuntimeException("Failed to get agentId from compat. agentId: " + agentId);
        }
        return agentIdFromCompat;
    }

    @Nullable
    private UUID getAgentIdFromCompat(String agentIdStr) {
        UUID agentIdInCache = this.agentIdCompatCache.getIfPresent(agentIdStr);
        if (agentIdInCache != null) {
            return agentIdInCache;
        }

        UUID agentId = this.loadAgentIdFromDao(agentIdStr);
        if (agentId != null) {
            return agentId;
        }

        UUID newAgentId = AgentUuidUtils.generateUUIDv7();
        logger.debug("Generated new agentId: {} -> {}", agentIdStr, newAgentId);
        if (this.putAgentIdCompatInDao(agentIdStr, newAgentId)) {
            return newAgentId;
        }

        return this.loadAgentIdFromDao(agentIdStr);
    }

    private UUID loadAgentIdFromDao(String agentIdStr) {
        try {
            UUID agentId = this.agentIdCompatDao.selectAgentIdCompat(agentIdStr);
            if (agentId == null) {
                return null;
            }

            this.agentIdCompatCache.put(agentIdStr, agentId);
            return agentId;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean putAgentIdCompatInDao(String agentIdStr, UUID agentId) {
        try {
            this.agentIdCompatDao.insertAgentIdCompat(agentIdStr, agentId);
            this.agentIdCompatCache.put(agentIdStr, agentId);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (Exception e) {
            logger.warn("Failed to put agentIdCompat in dao. agentIdStr: {}, agentId: {}", agentIdStr, agentId, e);
            return false;
        }
    }

}
