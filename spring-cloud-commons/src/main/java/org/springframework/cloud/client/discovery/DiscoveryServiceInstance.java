package org.springframework.cloud.client.discovery;

import java.net.URI;

import org.springframework.cloud.client.ServiceInstance;

/**
 * A {@link DiscoveryServiceInstance} provides extra information about
 * a service that may be useful for discovery clients.
 * @author Steve Oakey
 *
 */
public interface DiscoveryServiceInstance extends ServiceInstance {

	/**
	  * @return the URI of the health check information.
	  */
	 URI getHealthCheckUri();

	/**
	  * @return the URI if the status information.
	  */
	 URI getStatusPageUri();
}
