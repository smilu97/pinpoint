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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.AgentIndex;
import com.navercorp.pinpoint.common.server.bo.ApplicationIndex;
import com.navercorp.pinpoint.common.server.bo.ServiceIndex;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import com.navercorp.pinpoint.web.dao.AgentHierarchyDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class AgentHierarchyServiceImpl implements AgentHierarchyService {

    private final AgentHierarchyDao agentHierarchyDao;

    public AgentHierarchyServiceImpl(AgentHierarchyDao agentHierarchyDao) {
        this.agentHierarchyDao = Objects.requireNonNull(agentHierarchyDao, "agentHierarchyDao");
    }

    @Override
    @Cacheable(value = CacheConfiguration.SERVICES_CACHE_NAME, key = "'services'")
    public List<ServiceIndex> getServices() {
        return agentHierarchyDao.getServices();
    }

    @Override
    @Cacheable(value = CacheConfiguration.APPLICATIONS_CACHE_NAME, key = "#serviceId")
    public List<ApplicationIndex> getApplications(Long serviceId) {
        return agentHierarchyDao.getApplications(serviceId);
    }

    @Override
    @Cacheable(value = CacheConfiguration.AGENTS_CACHE_NAME, key = "#applicationId")
    public List<AgentIndex> getAgents(Long applicationId) {
        return agentHierarchyDao.getAgents(applicationId);
    }

}
