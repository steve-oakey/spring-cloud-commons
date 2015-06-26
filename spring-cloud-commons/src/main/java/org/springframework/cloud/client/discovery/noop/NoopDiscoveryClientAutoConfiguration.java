/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client.discovery.noop;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DefaultDiscoveryServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

/**
 * @author Dave Syer
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnMissingBean(DiscoveryClient.class)
@CommonsLog
public class NoopDiscoveryClientAutoConfiguration implements
		ApplicationListener<ContextRefreshedEvent> {

	@Autowired(required = false)
	private ServerProperties server;

	@Autowired(required = false)
	private ManagementServerProperties management;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Environment environment;

	@Autowired(required = false)
	private HealthEndpoint healthEndpoint;

	@Autowired(required = false)
	private InfoEndpoint infoEndpoint;

	@Autowired(required = false)
	private EndpointHandlerMapping endpointHandlerMapping;

	private DefaultDiscoveryServiceInstance serviceInstance;

	@PostConstruct
	public void init() {
		String host = "localhost";
		try {
			host = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) {
			log.error("Cannot get host info", e);
		}
		int port = findPort();

		this.serviceInstance = new DefaultDiscoveryServiceInstance(
				this.environment.getProperty("spring.application.name", "application"),
				host, port, false);

		if (!ManagementServerPortUtils.isDisabled(context) && endpointHandlerMapping != null) {
			int managementPort = findManagementPort(port);
			this.serviceInstance
					.setHealthCheckUri(getPath(healthEndpoint, managementPort));
			this.serviceInstance.setStatusPageUri(getPath(infoEndpoint, managementPort));
		}
	}

	private URI getPath(AbstractEndpoint<?> endpoint, int port) {
		if (endpoint.isEnabled()) {
			String path = endpointHandlerMapping.getPath(endpoint.getId());
			return DefaultDiscoveryServiceInstance.getUri(this.serviceInstance, port,
					path);
		}
		return null;
	}

	private int findManagementPort(int serverPort) {
		if(ManagementServerPortUtils.isDifferent(context)) {
			return management.getPort();
		}
		return serverPort;
	}

	private int findPort() {
		int port = 0;
		if (this.server != null && this.server.getPort() != null) {
			port = this.server.getPort();
		}
		if (ClassUtils.isPresent(
				"org.springframework.web.context.support.GenericWebApplicationContext",
				null)) {
			if (this.context instanceof EmbeddedWebApplicationContext) {
				EmbeddedServletContainer container = ((EmbeddedWebApplicationContext) this.context)
						.getEmbeddedServletContainer();
				if (container != null) {
					port = container.getPort();
				}
			}
		}
		else {
			// Apparently spring-web is not on the classpath
			if (log.isDebugEnabled()) {
				log.debug("Could not locate port in embedded container (spring-web not available)");
			}
		}
		return port;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.context.publishEvent(new InstanceRegisteredEvent<>(this, this.environment));
	}

	@Bean
	public DiscoveryClient discoveryClient() {
		return new NoopDiscoveryClient(this.serviceInstance);
	}

}
