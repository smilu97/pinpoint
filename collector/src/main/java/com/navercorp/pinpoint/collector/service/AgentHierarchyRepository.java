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

import com.navercorp.pinpoint.collector.dao.AgentHierarchyDao;
import com.navercorp.pinpoint.common.server.bo.ApplicationIndex;
import com.navercorp.pinpoint.common.server.bo.ServiceHasApplication;
import com.navercorp.pinpoint.common.server.bo.ServiceIndex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Service
public class AgentHierarchyRepository {

    private final AgentHierarchyDao agentHierarchyDao;

    public AgentHierarchyRepository(AgentHierarchyDao agentHierarchyDao) {
        this.agentHierarchyDao = Objects.requireNonNull(agentHierarchyDao, "serviceIndexDao");
    }

    public Long getServiceId(String serviceId) {
        return this.agentHierarchyDao.selectServiceIdByName(serviceId, false);
    }

    public Long getApplication(Long serviceIdId, String applicationName) {
        return this.agentHierarchyDao.selectApplicationIdByServiceIdAndApplicationName(serviceIdId, applicationName, false);
    }

    public List<ServiceIndex> getServices() {
        return this.agentHierarchyDao.selectAllServices();
    }

    public List<ApplicationIndex> getApplications() {
        return this.agentHierarchyDao.selectAllApplications();
    }

    public List<ServiceHasApplication> getServiceHasApplications() {
        return this.agentHierarchyDao.selectAllServiceHasApplications();
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long insertService(String serviceId) {
        Long id = this.agentHierarchyDao.selectServiceIdByName(serviceId, true);
        if (id != null) {
            return id;
        }

        return this.agentHierarchyDao.insertService(serviceId);
    }

    public Long getApplicationId(Long serviceId, String applicationName) {
        return this.agentHierarchyDao.selectApplicationIdByServiceIdAndApplicationName(serviceId, applicationName, false);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long insertApplication(Long serviceId, String applicationName) {
        Long applicationId = this.agentHierarchyDao.selectApplicationIdByServiceIdAndApplicationName(serviceId, applicationName, true);
        if (applicationId != null) {
            return applicationId;
        }

        Long newApplicationId = this.agentHierarchyDao.insertApplication(applicationName);
        this.agentHierarchyDao.insertServiceHasApplication(serviceId, newApplicationId);
        return newApplicationId;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void insertAgent(long applicationId, UUID agentId, String agentName) {
        this.agentHierarchyDao.insertAgent(agentId, agentName);
        this.agentHierarchyDao.insertApplicationHasAgent(applicationId, agentId);
    }

}
