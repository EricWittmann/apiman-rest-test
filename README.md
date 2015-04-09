# Arquillian REST Extension example for Apiman Endpoints

This example is pre-configured to talk to Apiman Manager REST endpoints.

## Prerequisities
You need to unzip a wildfly8 distribution and set its path to arquillian.xml, and add a jboss user 'admin' with roles 'apiuser', 'apipublisher', 'apiadmin' for Apiman.

## Running
	mvn clean install
