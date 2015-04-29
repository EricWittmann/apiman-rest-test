# Apiman plugin test suite

## About
Maven build prepares a fresh installation of wildfly, deploys apiman-modules, configures user and provides further apiman configuration.
Arquillian test suite then deploys apiman-gateway along with the neccessary gateway api, and spins up the well-known echo service so plugin developers can test their plugins out-of-the-box.

## Usage
Only thing user needs to implement is their assertions and provide the test suite with Apiman service definition to be published before running the tests. Could look like the following:

	@Override
	public Service createApimanService() {
		Policy p = new Policy();
		p.setPolicyImpl("plugin:io.apiman.plugins:apiman-plugins-test-policy:1.1.1.Final:war/io.apiman.plugins.test_policy.TestPolicy");
		p.setPolicyJsonConfig("");

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

See PolicyPluginTest.

## Running
	mvn clean install [-Dapiman.host=<host> -Dapiman.port=<port> -Dapiman.user=<user> -Dapiman.pwd=<pwd>]
