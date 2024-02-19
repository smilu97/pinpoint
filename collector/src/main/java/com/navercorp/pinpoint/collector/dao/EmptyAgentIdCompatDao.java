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

package com.navercorp.pinpoint.collector.dao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Service
@ConditionalOnProperty(name = "pinpoint.experimental.service-index", havingValue = "empty", matchIfMissing = true)
public class EmptyAgentIdCompatDao implements AgentIdCompatDao {

    @Override
    public void insertAgentIdCompat(String agentIdStr, UUID agentId) {

    }

    @Override
    public UUID selectAgentIdCompat(String agentIdStr) {
        return null;
    }

}
