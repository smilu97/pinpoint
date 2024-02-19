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

import com.navercorp.pinpoint.common.server.bo.ApplicationIndex;
import com.navercorp.pinpoint.common.server.bo.ServiceHasApplication;
import com.navercorp.pinpoint.common.server.bo.ServiceIndex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author youngjin.kim2
 */
@Service
public class CachedAgentHierarchyService implements AgentHierarchyService, InitializingBean {

    private final AgentHierarchyRepository agentHierarchyRepository;

    private final AtomicBoolean concurrentFence = new AtomicBoolean(false);
    private final AtomicReference<State> stateRef = new AtomicReference<>(State.EMPTY);

    public CachedAgentHierarchyService(
            AgentHierarchyRepository agentHierarchyRepository
    ) {
        this.agentHierarchyRepository = Objects.requireNonNull(agentHierarchyRepository, "agentHierarchyRepository");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reload();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void reload() {
        try {
            if (concurrentFence.compareAndSet(false, true)) {
                reload0();
            }
        } finally {
            concurrentFence.set(false);
        }
    }

    private void reload0() {
        List<ServiceIndex> serviceIndices = this.agentHierarchyRepository.getServices();
        List<ApplicationIndex> applicationIndices = this.agentHierarchyRepository.getApplications();
        List<ServiceHasApplication> serviceHasApplications = this.agentHierarchyRepository.getServiceHasApplications();

        State newState = State.build(serviceIndices, applicationIndices, serviceHasApplications);
        stateRef.set(newState);
    }

    @Override
    public void insertAgent(String serviceId, String applicationName, UUID agentId, String agentName) {
        long applicationId = insertApplication(serviceId, applicationName);
        insertAgent(agentId, agentName, applicationId);
    }

    @Override
    public long insertApplication(String serviceId, String applicationName) {
        long serviceIdId = insertService(serviceId);
        return insertApplication(serviceIdId, applicationName);
    }

    private Long insertService(String serviceId) {
        // Try on memory
        Long serviceIdIdInMemory = this.stateRef.get().getServiceIdByName(serviceId);
        if (serviceIdIdInMemory != null) {
            return serviceIdIdInMemory;
        }

        // Try on DB without write-lock
        Long serviceIdId = this.agentHierarchyRepository.getServiceId(serviceId);
        if (serviceIdId != null) {
            return serviceIdId;
        }

        return agentHierarchyRepository.insertService(serviceId);
    }

    private Long insertApplication(Long serviceId, String applicationName) {
        // Try on memory
        Long applicationIdInMemory = this.stateRef.get().getApplicationId(serviceId, applicationName);
        if (applicationIdInMemory != null) {
            return applicationIdInMemory;
        }

        // Try on DB without write-lock
        Long applicationId = this.agentHierarchyRepository.getApplication(serviceId, applicationName);
        if (applicationId != null) {
            return applicationId;
        }

        return agentHierarchyRepository.insertApplication(serviceId, applicationName);
    }

    private void insertAgent(UUID agentId, String agentName, long applicationId) {
        agentHierarchyRepository.insertAgent(applicationId, agentId, agentName);
    }

    private record State(
            TreeSet<ServiceIndex> servicesByNames,
            TreeSet<ApplicationIndex> applicationsByIds,
            TreeSet<ServiceHasApplication> serviceHasApplications
    ) {

        static final State EMPTY = new State(
                new TreeSet<>(Comparator.comparing(ServiceIndex::name)),
                new TreeSet<>(Comparator.comparingLong(ApplicationIndex::id)),
                new TreeSet<>(Comparator.comparingLong(ServiceHasApplication::serviceId))
        );

        static State build(
                List<ServiceIndex> serviceIndices,
                List<ApplicationIndex> applicationIndices,
                List<ServiceHasApplication> serviceHasApplications
        ) {
            TreeSet<ServiceIndex> servicesByNames = new TreeSet<>(Comparator.comparing(ServiceIndex::name));
            servicesByNames.addAll(serviceIndices);

            TreeSet<ApplicationIndex> applicationsByIds = new TreeSet<>(Comparator.comparingLong(ApplicationIndex::id));
            applicationsByIds.addAll(applicationIndices);

            TreeSet<ServiceHasApplication> serviceHasApplicationsSet =
                    new TreeSet<>(Comparator.comparingLong(ServiceHasApplication::serviceId));
            serviceHasApplicationsSet.addAll(serviceHasApplications);

            return new State(servicesByNames, applicationsByIds, serviceHasApplicationsSet);
        }

        Long getServiceIdByName(String name) {
            ServiceIndex ceil = servicesByNames.ceiling(new ServiceIndex(null, name));
            if (ceil == null) {
                return null;
            }
            if (ceil.name().equals(name)) {
                return ceil.id();
            }
            return null;
        }

        String getApplicationName(Long applicationId) {
            ApplicationIndex applicationIndex = applicationsByIds.ceiling(new ApplicationIndex(applicationId, null));
            if (applicationIndex == null) {
                return null;
            }
            if (applicationIndex.id().equals(applicationId)) {
                return applicationIndex.name();
            }
            return null;
        }

        Long getApplicationId(Long serviceId, String applicationName) {
            SortedSet<ServiceHasApplication> serviceHasApplications =
                    this.serviceHasApplications.tailSet(new ServiceHasApplication(serviceId, 0L));
            for (ServiceHasApplication serviceHasApplication : serviceHasApplications) {
                if (!Objects.equals(serviceHasApplication.serviceId(), serviceId)) {
                    break;
                }
                if (applicationName.equals(getApplicationName(serviceHasApplication.applicationId()))) {
                    return serviceHasApplication.applicationId();
                }
            }
            return null;
        }

    }

}
