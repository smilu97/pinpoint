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

package com.navercorp.pinpoint.collector.dao.mysql;

import com.navercorp.pinpoint.collector.dao.AgentIdCompatDao;
import com.navercorp.pinpoint.common.server.bo.AgentIdCompat;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Repository
@ConditionalOnProperty(name = "pinpoint.experimental.service-index", havingValue = "mysql")
public class MysqlAgentIdCompatDao implements AgentIdCompatDao {

    private static final String NAMESPACE = AgentIdCompatDao.class.getName() + '.';

    private final SqlSessionTemplate template;

    public MysqlAgentIdCompatDao(SqlSessionTemplate template) {
        this.template = Objects.requireNonNull(template, "sqlSessionTemplate");
    }

    @Override
    public void insertAgentIdCompat(String agentIdStr, UUID agentId) {
        AgentIdCompat param = new AgentIdCompat(agentIdStr, agentId);
        this.template.insert(NAMESPACE + "insertAgentIdCompat", param);
    }

    @Override
    public UUID selectAgentIdCompat(String agentIdStr) {
        return this.template.selectOne(NAMESPACE + "selectAgentIdCompat", agentIdStr);
    }

}
