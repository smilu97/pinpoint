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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.service.AgentIdCompatService;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class AgentIdCompatInterceptor implements ServerInterceptor {

    private final AgentIdCompatService agentIdCompatService;
    private final Context.Key<Header> contextKey;

    public AgentIdCompatInterceptor(AgentIdCompatService agentIdCompatService) {
        this(agentIdCompatService, ServerContext.getAgentInfoKey());
    }

    public AgentIdCompatInterceptor(AgentIdCompatService agentIdCompatService, Context.Key<Header> contextKey) {
        this.agentIdCompatService = Objects.requireNonNull(agentIdCompatService, "agentIdCompatService");
        this.contextKey = Objects.requireNonNull(contextKey, "contextKey");
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context ctx = Context.current();
        Header header = this.contextKey.get(ctx);
        if (header == null) {
            return interceptListener(next.startCall(call, headers));
        }
        Context newCtx = ctx.withValue(this.contextKey, interceptHeader(header));
        return interceptListener(Contexts.interceptCall(newCtx, call, headers, next));
    }

    private Header interceptHeader(Header header) {
        String agentId = header.getAgentId();
        UUID agentIdUuid = this.agentIdCompatService.decodeAgentId(agentId);
        String encodedAgentIdUuid = AgentUuidUtils.encode(agentIdUuid);
        if (encodedAgentIdUuid.equals(agentId)) {
            return header;
        } else {
            return header.withAgentId(encodedAgentIdUuid);
        }
    }

    private <ReqT> Listener<ReqT> interceptListener(Listener<ReqT> listener) {
        return new AgentIdCompatListener<>(this.agentIdCompatService, listener);
    }
}
