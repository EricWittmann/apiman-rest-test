package org.jboss.apiman.qa.rest;

import org.apache.maven.shared.invoker.MavenInvocationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.Collections;

import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.manager.api.beans.plugins.PluginBean;

/**
 * Demo on how to use Arquillian REST Extension.
 * <br/>
 * For more use cases see https://github.com/arquillian/arquillian-extension-rest/blob/master/rest-client/ftest/ftest-impl-3x/src/test/java/rest/RestClientTestCase.java
 * 
 * @author sbunciak
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractPluginTest {

	private static final String APIMAN_VERSION = System.getProperty("version.apiman", "1.1.0.Final");

	@Deployment(name = "echo", order = 1)
	public static WebArchive createEchoService() {
		WebArchive echo =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-quickstarts-echo-service:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		//		System.out.println(api.toString(true));
		return echo;
	}

	@Deployment(name = "api", order = 2)
	public static WebArchive createApimanApi() {
		WebArchive api =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-gateway-platforms-war-wildfly8-api:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		//		System.out.println(api.toString(true));
		return api;
	}

	@Deployment(name = "gateway", order = 3)
	public static WebArchive createApimanGateway() {
		WebArchive gateway =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-gateway-platforms-war-wildfly8-gateway:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		//	System.out.println(gateway.toString(true));
		return gateway;
	}
//TODO executing multiple times!!!!
	@Before
	public void setupAbstractPluginTest() throws MavenInvocationException {
		// Publish Echo service
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(String.format(
				"http://%s:%s/apiman-gateway-api/services", getApimanHost(), getApimanPort()));
		System.out.println(target.getUri());

		Response response = target.request().header("Authorization", getAuthHeader())
				.put(Entity.entity(getService(), MediaType.APPLICATION_JSON));
		//Read output in string format
		System.out.println(response.getStatus());
		response.close();
	}

	public Service getService() {
		Policy p = new Policy();
		p.setPolicyImpl("plugin:io.apiman.plugins:apiman-plugins-config-policy:1.1.0.Final:war/io.apiman.plugins.config_policy.ConfigPolicy");
		
		Service s = new Service();
		s.setEndpoint(String.format("http://%s:%s/apiman-echo/test", getApimanHost(), getApimanPort()));
		s.setEndpointType("REST");
		s.setPublicService(true);
		s.setOrganizationId("PluginTestPolicyTest");
		s.setServiceId("echo");
		s.setVersion("1.0");
		s.setServicePolicies(Arrays.asList(p));
		return s;
	}
	
	// TODO
	public String getApimanHost() {
		return "localhost";
	}

	// TODO
	public int getApimanPort() {
		return 8080;
	}
	
	// TODO
	public String getAuthHeader() {
		return "Basic YWRtaW46YWRtaW4xMjMh";
	}

	@After
	public void tearDownPluginTest() {
		// TODO
	}
}
