package org.jboss.apiman.qa.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.extension.rest.client.Header;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.net.URL;

import io.apiman.manager.api.beans.system.SystemStatusBean;

/**
 * Demo on how to use Arquillian REST Extension.
 * <br/>
 * For more use cases see https://github.com/arquillian/arquillian-extension-rest/blob/master/rest-client/ftest/ftest-impl-3x/src/test/java/rest/RestClientTestCase.java
 * 
 * @author sbunciak
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RestApiTest {

	@ArquillianResource
	private URL deploymentURL;

	@Deployment(testable = false)
	public static WebArchive create() {
		WebArchive apiman =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-manager-api-war-wildfly8:war:1.1.0.RC3").withTransitivity()
						.asSingle(WebArchive.class);

		apiman.addAsWebInfResource("apiman-ds.xml");
		// System.out.println(apiman.toString(true));
		return apiman;
	}

	/*
	 * Using authorization header for basic auth: Basic Base64.encode("admin:admin123!")
	 */
	@Test
	@Header(name = "Authorization", value = "Basic YWRtaW46YWRtaW4xMjMh")
	@Produces(MediaType.APPLICATION_JSON)
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
	public void shouldReceiveUnauthorized(@ArquillianResteasyResource("system/status/") WebTarget webTarget) {
		//        Given
		// you set up 'admin' user with roles: apiadmin  apipublisher apiuser

		//        When
		int status = webTarget.request().get().getStatus();

		//        Then
		assertEquals(401, status);
	}
}
