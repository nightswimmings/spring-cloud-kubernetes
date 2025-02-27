/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.kubernetes;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.server.mock.KubernetesServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.kubernetes.commons.KubernetesClientProperties;
import org.springframework.cloud.kubernetes.example.App;
import org.springframework.cloud.kubernetes.fabric8.Fabric8HealthIndicator;
import org.springframework.cloud.kubernetes.fabric8.Fabric8InfoContributor;
import org.springframework.cloud.kubernetes.fabric8.Fabric8PodUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class,
		properties = { "spring.cloud.kubernetes.client.password=mypassword",
				"spring.cloud.kubernetes.client.proxy-password=myproxypassword" })
public class Fabric8AutoConfigurationTests {

	public static KubernetesServer server = new KubernetesServer(false);

	@Autowired
	ConfigurableApplicationContext context;

	@BeforeAll
	public static void setUpBeforeClass() {
		server.before();
		KubernetesClient mockClient = server.getClient();

		// Configure the kubernetes master url to point to the mock server
		System.setProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY, mockClient.getConfiguration().getMasterUrl());
		System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");
		System.setProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY, "false");
		System.setProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY, "false");
		System.setProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY, "test");
		System.setProperty(Config.KUBERNETES_HTTP2_DISABLE, "true");
	}

	@AfterAll
	public static void afterClass() {
		server.after();
		System.clearProperty(Config.KUBERNETES_MASTER_SYSTEM_PROPERTY);
		System.clearProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY);
		System.clearProperty(Config.KUBERNETES_AUTH_TRYKUBECONFIG_SYSTEM_PROPERTY);
		System.clearProperty(Config.KUBERNETES_AUTH_TRYSERVICEACCOUNT_SYSTEM_PROPERTY);
		System.clearProperty(Config.KUBERNETES_NAMESPACE_SYSTEM_PROPERTY);
		System.clearProperty(Config.KUBERNETES_HTTP2_DISABLE);
	}

	@Test
	public void beansAreCreated() {
		assertThat(context.getBeanNamesForType(Config.class)).hasSize(1);
		assertThat(context.getBeanNamesForType(KubernetesClient.class)).hasSize(1);
		assertThat(context.getBeanNamesForType(Fabric8PodUtils.class)).hasSize(1);
		assertThat(context.getBeanNamesForType(Fabric8HealthIndicator.class)).hasSize(1);
		assertThat(context.getBeanNamesForType(Fabric8InfoContributor.class)).hasSize(1);
		assertThat(context.getBeanNamesForType(KubernetesClientProperties.class)).hasSize(1);

		Config config = context.getBean(Config.class);
		assertThat(config.getPassword()).isEqualTo("mypassword");
		assertThat(config.getProxyPassword()).isEqualTo("myproxypassword");
	}

}
