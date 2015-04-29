package org.jboss.apiman.qa.rest;

import org.apache.commons.codec.binary.Base64;

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

import io.apiman.gateway.engine.beans.Service;

/**
 * Base class for Apiman Plugin Tests.
 *
 * @author sbunciak
 */
@RunWith(Arquillian.class)
@RunAsClient
public abstract class AbstractPluginTest {

	protected static final String APIMAN_VERSION = System.getProperty("version.apiman", "1.1.1.Final");
	protected static final String APIMAN_HOST = System.getProperty("apiman.host", "localhost");
	protected static final String APIMAN_PORT = System.getProperty("apiman.port", "8080");
	protected static final String APIMAN_USER = System.getProperty("apiman.user", "admin");
	protected static final String APIMAN_PWD = System.getProperty("apiman.pwd", "admin123!");

	// Apiman service under test
	private Service apimanService;

	public AbstractPluginTest() {
		this.apimanService = createApimanService();
	}

	@Deployment(name = "echo", order = 1)
	public static WebArchive createEchoService() {
		WebArchive echo =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-quickstarts-echo-service:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		// System.out.println(api.toString(true));
		return echo;
	}

	@Deployment(name = "api", order = 2)
	public static WebArchive createApimanApi() {
		WebArchive api =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-gateway-platforms-war-wildfly8-api:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		// System.out.println(api.toString(true));
		return api;
	}

	@Deployment(name = "gateway", order = 3)
	public static WebArchive createApimanGateway() {
		WebArchive gateway =
				Maven.configureResolver().withMavenCentralRepo(true)
						.resolve("io.apiman:apiman-gateway-platforms-war-wildfly8-gateway:war:" + APIMAN_VERSION)
						.withTransitivity()
						.asSingle(WebArchive.class);

		// System.out.println(gateway.toString(true));
		return gateway;
	}

	@Before
	public void setupAbstractPluginTest() {
		// Publish Echo service
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(String.format(
				"http://%s:%s/apiman-gateway-api/services", APIMAN_HOST, APIMAN_PORT));
		// System.out.println("PUT: " +target.getUri());

		Response response = target.request().header("Authorization", getAuthHeader())
				.put(Entity.entity(apimanService, MediaType.APPLICATION_JSON));
		// Read output in string format
		// System.out.println(response.getStatus());
		response.close();
	}

	/**
	 * Method to be implemented by particular tests.
	 * 
	 * @return io.apiman.gateway.engine.beans.Service
	 */
	public abstract Service createApimanService();

	private String getAuthHeader() {
		return "Basic " + Base64.encodeBase64String(new String(APIMAN_USER + ":" + APIMAN_PWD).getBytes());
	}

	@After
	public void tearDownPluginTest() {
		// Delete service from registry
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(String.format(
				"http://%s:%s/apiman-gateway-api/services/%s/%s/%s", APIMAN_HOST, APIMAN_PORT,
				apimanService.getOrganizationId(), apimanService.getServiceId(), apimanService.getVersion()));
		// System.out.println("DELETE: "+target.getUri());

		Response response = target.request().header("Authorization", getAuthHeader()).delete();
		//Read output in string format

		// System.out.println(response.getStatus());
		response.close();
	}
}
