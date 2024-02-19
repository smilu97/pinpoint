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

package com.navercorp.pinpoint.web.dao.mysql;

import com.navercorp.pinpoint.common.server.bo.AgentIndex;
import com.navercorp.pinpoint.common.server.bo.ApplicationIndex;
import com.navercorp.pinpoint.common.server.bo.ServiceIndex;
import com.navercorp.pinpoint.web.dao.AgentHierarchyDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Repository
public class MysqlAgentHierarchyDao implements AgentHierarchyDao {

    private static final String NAMESPACE = MysqlAgentHierarchyDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlAgentHierarchyDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public List<ServiceIndex> getServices() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectServices");
    }

    @Override
    public List<ApplicationIndex> getApplications(Long serviceId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectApplications", serviceId);
    }

    @Override
    public List<AgentIndex> getAgents(Long applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAgents", applicationId);
    }

}
