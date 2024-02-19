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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.bo.AgentIndex;
import com.navercorp.pinpoint.common.server.bo.ApplicationIndex;
import com.navercorp.pinpoint.common.server.bo.ServiceIndex;
import com.navercorp.pinpoint.web.service.AgentHierarchyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@RestController
public class AgentHierarchyController {

    private final AgentHierarchyService agentHierarchyService;

    public AgentHierarchyController(AgentHierarchyService agentHierarchyService) {
        this.agentHierarchyService = Objects.requireNonNull(agentHierarchyService, "agentHierarchyService");
    }

    @GetMapping("/services")
    public List<ServiceIndex> getServices() {
        return this.agentHierarchyService.getServices();
    }

    @GetMapping("/services/{serviceId}/applications")
    public List<ApplicationIndex> getApplications(@PathVariable Long serviceId) {
        return this.agentHierarchyService.getApplications(serviceId);
    }

    @GetMapping("/applications/{applicationId}/agents")
    public List<AgentIndex> getAgents(@PathVariable Long applicationId) {
        return this.agentHierarchyService.getAgents(applicationId);
    }

}
