/*
 * Copyright 2012-2025 the original author or authors.
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
package org.springframework.samples.petclinic;

import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.samples.petclinic.owner.Clinic;

/**
 * Starts the Spring context on the first graft call. {@code gg} does not boot Spring itself.
 * Public so {@code owner.Clinic} can reach the package-private {@link ClinicGraftBoot}.
 */
public final class ClinicHost {

	private static volatile Clinic bean;

	private ClinicHost() {
	}

	public static Clinic clinic() {
		Clinic current = bean;
		if (current == null) {
			synchronized (ClinicHost.class) {
				current = bean;
				if (current == null) {
					preferLogbackOverGatewayNop();
					ConfigurableApplicationContext context = SpringApplication.run(ClinicGraftBoot.class);
					current = context.getBean(Clinic.class);
					bean = current;
				}
			}
		}
		return current;
	}

	/**
	 * Sample workaround when the host runs inside gg: hypertube-java-core may bind
	 * NOPLoggerFactory before Spring Boot starts. Product fix belongs in the gg JVM
	 * runtime, not in host apps.
	 */
	private static void preferLogbackOverGatewayNop() {
		System.setProperty("slf4j.provider", "ch.qos.logback.classic.spi.LogbackServiceProvider");
		LoggerFactoryFriend.reset();
		LoggerFactory.getILoggerFactory();
	}

}
