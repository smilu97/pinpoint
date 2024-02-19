package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.service.AgentIdCompatService;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HeaderServerInterceptorFactory {

    @Bean
    public AgentIdCompatInterceptor agentIdCompatInterceptor(AgentIdCompatService agentIdCompatService) {
        return new AgentIdCompatInterceptor(agentIdCompatService);
    }

    @Bean
    public List<ServerInterceptor> agentInterceptorList(AgentIdCompatInterceptor agentIdCompatInterceptor) {
        return newServerInterceptors(agentIdCompatInterceptor, "agent");
    }

    @Bean
    public List<ServerInterceptor> spanInterceptorList(AgentIdCompatInterceptor agentIdCompatInterceptor) {
        return newServerInterceptors(agentIdCompatInterceptor, "span");
    }

    @Bean
    public List<ServerInterceptor> statInterceptorList(AgentIdCompatInterceptor agentIdCompatInterceptor) {
        return newServerInterceptors(agentIdCompatInterceptor, "stat");
    }

    private List<ServerInterceptor> newServerInterceptors(AgentIdCompatInterceptor agentIdCompatInterceptor, String name) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name);
        ServerInterceptor headerPropagationInterceptor = new HeaderPropagationInterceptor(headerReader);
        return List.of(agentIdCompatInterceptor, headerPropagationInterceptor);
    }
}
