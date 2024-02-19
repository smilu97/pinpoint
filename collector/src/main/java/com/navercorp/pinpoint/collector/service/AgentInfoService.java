/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

/**
 * @author emeroad
 * @author koo.taejin
 * @author jaehong.kim
 */
@Service
@Validated
public class AgentInfoService {

    private final AgentInfoDao agentInfoDao;

    private final ApplicationIndexDao applicationIndexDao;

    private final AgentHierarchyService agentHierarchyService;

    public AgentInfoService(
            AgentInfoDao agentInfoDao,
            ApplicationIndexDao applicationIndexDao,
            AgentHierarchyService agentHierarchyService
    ) {
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentHierarchyService = Objects.requireNonNull(agentHierarchyService, "agentHierarchyRepository");
    }

    public void insert(@Valid final AgentInfoBo agentInfoBo) {
        agentInfoDao.insert(agentInfoBo);
        applicationIndexDao.insert(agentInfoBo);

        insertService(agentInfoBo);
    }

    private void insertService(AgentInfoBo agentInfoBo) {
        String serviceId = agentInfoBo.getServiceId();
        String applicationName = agentInfoBo.getApplicationName();
        UUID agentId = AgentUuidUtils.decode(agentInfoBo.getAgentId());
        String agentName = agentInfoBo.getAgentName();

        this.agentHierarchyService.insertAgent(serviceId, applicationName, agentId, agentName);
    }

    public AgentInfoBo getAgentInfo(@NotBlank final String agentId, @PositiveOrZero final long timestamp) {
        return agentInfoDao.getAgentInfo(agentId, timestamp);
    }
}