package org.springframework.cloud.client.discovery;

import java.net.URI;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DefaultDiscoveryServiceInstance extends DefaultServiceInstance implements DiscoveryServiceInstance {

    private URI healthCheckUri;
    private URI statusPageUri;

    public DefaultDiscoveryServiceInstance(String serviceId, String host, int port, boolean secure) {
	super(serviceId, host, port, secure);
    }

    /**
     * Get the URI for a management endpoint path.
     * 
     * @param instance
     *            the {@link DiscoveryServiceInstance}.
     * @param managementPort
     *            the management port.
     * @param endpointPath
     *            the path.
     * @return the URI of the endpoint.
     */
    public static URI getUri(DiscoveryServiceInstance instance, int managementPort, String endpointPath) {
	return UriComponentsBuilder.newInstance().scheme(getScheme(instance)).host(instance.getHost())
		.port(managementPort).path(endpointPath).build().toUri();
    }
}
