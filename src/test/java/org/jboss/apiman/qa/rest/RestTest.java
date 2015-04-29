package org.jboss.apiman.qa.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.extension.rest.client.Header;
import org.jboss.arquillian.test.api.ArquillianResource;

import org.junit.Test;

import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.net.URL;
import java.util.Arrays;

import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.manager.api.beans.system.SystemStatusBean;

public class RestTest extends AbstractPluginTest {

	@ArquillianResource
	private URL deploymentURL;

	/*
	 * Using authorization header for basic auth: Basic Base64.encode("admin:admin123!")
	 */
	@Test
	@Header(name = "Authorization", value = "Basic YWRtaW46YWRtaW4xMjMh")
	@Produces(MediaType.APPLICATION_JSON)
	@OperateOnDeployment("api")
	public void shouldRetrieveSystemStatus(@ArquillianResteasyResource("system/status/") WebTarget webTarget) {
		//        Given
		// you set up 'admin' user with roles: apiadmin  apipublisher apiuser

		//        When
		final SystemStatusBean statusBean = webTarget.request().get().readEntity(SystemStatusBean.class);

		//        Then
		assertNotNull(statusBean);
		assertTrue(statusBean.isUp());
	}

	@Test
	@Produces(MediaType.APPLICATION_JSON)
	@OperateOnDeployment("api")
	public void shouldReceiveUnauthorized(@ArquillianResteasyResource("system/status/") WebTarget webTarget) {
		//        Given
		// you set up 'admin' user with roles: apiadmin  apipublisher apiuser

		//        When
		int status = webTarget.request().get().getStatus();

		//        Then
		assertEquals(401, status);
	}

	@Override
	public Service getService() {
		Policy p = new Policy();
		p.setPolicyImpl("plugin:io.apiman.plugins:apiman-plugins-config-policy:1.1.1.Final:war/io.apiman.plugins.config_policy.ConfigPolicy");

		Service s = new Service();
		s.setEndpoint(String.format("http://%s:%s/apiman-echo/test", APIMAN_HOST, APIMAN_PORT));
		s.setEndpointType("REST");
		s.setPublicService(true);
		s.setOrganizationId("PluginTestPolicyTest");
		s.setServiceId("echo");
		s.setVersion("1.0");
		s.setServicePolicies(Arrays.asList(p));
		return s;
	}
}
