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
import javax.ws.rs.core.Response;

import java.net.URL;
import java.util.Arrays;

import io.apiman.quickstarts.echo.EchoResponse;

import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.manager.api.beans.system.SystemStatusBean;

/**
 * Demo on how to use Arquillian REST Extension against Apiman Gateway.
 * <br/>
 * For more use cases of Arquillian REST Extension see
 * https://github.com/arquillian/arquillian-extension-rest/blob/master/rest-client/ftest/ftest-impl-3x/src/test/java/rest/RestClientTestCase.java
 *
 *
 * @author sbunciak
 *
 */
public class PolicyPluginTest extends AbstractPluginTest {

	@ArquillianResource
	private URL deploymentURL;

	private static final String ORG_ID = "PluginTestPolicyTest";
	private static final String SERVICE_ID = "echo";
	private static final String SERVICE_VER = "1.0";
	private static final String SERVICE_ENDPOINT= ORG_ID + "/" + SERVICE_ID + "/" + SERVICE_VER;
	
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

	/*
	* Using authorization header for basic auth: Basic Base64.encode("admin:admin123!")
	*/
	@Test
	@Header(name = "Authorization", value = "Basic YWRtaW46YWRtaW4xMjMh")
	@Produces(MediaType.APPLICATION_JSON)
	@OperateOnDeployment("gateway")
	public void shouldContainTestHeader(@ArquillianResteasyResource(SERVICE_ENDPOINT) WebTarget target) {

		Response r = target.request().get();
		assertEquals(200, r.getStatus());

		EchoResponse echoResp = r.readEntity(EchoResponse.class);
		assertTrue(echoResp.getHeaders().containsKey("Test-Policy"));

		r.close();
	}

	@Override
	public Service createApimanService() {
		Policy p = new Policy();
		p.setPolicyImpl("plugin:io.apiman.plugins:apiman-plugins-test-policy:1.1.1.Final:war/io.apiman.plugins.test_policy.TestPolicy");
		p.setPolicyJsonConfig("");

		Service s = new Service();
		s.setEndpoint(String.format("http://%s:%s/apiman-echo/test", APIMAN_HOST, APIMAN_PORT));
		s.setEndpointType("REST");
		s.setPublicService(true);
		s.setOrganizationId(ORG_ID);
		s.setServiceId(SERVICE_ID);
		s.setVersion(SERVICE_VER);
		s.setServicePolicies(Arrays.asList(p));
		return s;
	}
}
