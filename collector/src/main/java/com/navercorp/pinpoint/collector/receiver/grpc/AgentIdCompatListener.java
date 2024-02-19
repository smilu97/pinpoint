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
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import io.grpc.ServerCall.Listener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class AgentIdCompatListener<T> extends Listener<T> {

    private static final Logger logger = LogManager.getLogger(AgentIdCompatListener.class);

    private final AgentIdCompatService agentIdCompatService;
    private final Listener<T> delegate;


    public AgentIdCompatListener(
            AgentIdCompatService agentIdCompatService,
            Listener<T> delegate
    ) {
        this.agentIdCompatService = Objects.requireNonNull(agentIdCompatService, "agentIdCompatService");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void onMessage(T message) {
        delegate.onMessage(interceptMessage(message));
    }

    private T interceptMessage(T message) {
        try {

            if (message instanceof PSpanMessage spanMessage) {
                return (T) interceptSpanMessage(spanMessage);
            } else {
                return message;
            }
        } catch (Exception e) {
            logger.warn("Failed to intercept gRPC message for agent id compat: {}", message, e);
            return message;
        }
    }

    private PSpanMessage interceptSpanMessage(PSpanMessage spanMessage) {
        if (spanMessage.hasSpan()) {
            return PSpanMessage.newBuilder(spanMessage)
                    .setSpan(interceptSpan(spanMessage.getSpan()))
                    .build();
        }
        if (spanMessage.hasSpanChunk()) {
            return PSpanMessage.newBuilder(spanMessage)
                    .setSpanChunk(interceptSpanChunk(spanMessage.getSpanChunk()))
                    .build();
        }
        return spanMessage;
    }

    private PSpan interceptSpan(PSpan span) {
        return PSpan.newBuilder(span)
                .setTransactionId(interceptTransactionId(span.getTransactionId()))
                .build();
    }

    private PSpanChunk interceptSpanChunk(PSpanChunk spanChunk) {
        return PSpanChunk.newBuilder(spanChunk)
                .setTransactionId(interceptTransactionId(spanChunk.getTransactionId()))
                .build();
    }

    private PTransactionId interceptTransactionId(PTransactionId transactionId) {
        return PTransactionId.newBuilder(transactionId)
                .setAgentId(interceptAgentId(transactionId.getAgentId()))
                .build();
    }

    private String interceptAgentId(String agentId) {
        UUID agentUuid = agentIdCompatService.decodeAgentId(agentId);
        return AgentUuidUtils.encode(agentUuid);
    }

    @Override
    public void onHalfClose() {
        delegate.onHalfClose();
    }

    @Override
    public void onCancel() {
        delegate.onCancel();
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
    }

    @Override
    public void onReady() {
        delegate.onReady();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
