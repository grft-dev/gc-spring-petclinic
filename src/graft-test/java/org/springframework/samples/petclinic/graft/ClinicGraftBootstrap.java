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
package org.springframework.samples.petclinic.graft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import graft.maven.petclinic_clinic.GraftConfig;

/**
 * JVM graft client bootstrap for cross-boundary tests against the hosted module.
 */
public final class ClinicGraftBootstrap {

	private static final String DEFAULT_GRAFT_HOST = "ws://localhost:8090/ws";

	private static String hostOverride;

	private static volatile boolean graftConfigured;

	static {
		System.setProperty("spring.devtools.restart.enabled", "false");
		ensureModuleOnJavaClasspath();
	}

	private ClinicGraftBootstrap() {
	}

	public static void configureHostForTests(String host) {
		if (host == null || host.isBlank()) {
			throw new IllegalArgumentException("host must be non-blank");
		}
		hostOverride = host.trim();
	}

	public static void ensureReady() {
		configureGraftOnce();
	}

	private static void configureGraftOnce() {
		if (graftConfigured) {
			return;
		}
		synchronized (ClinicGraftBootstrap.class) {
			if (graftConfigured) {
				return;
			}
			GraftConfig.host = resolveHost();
			GraftConfig.stateless = false;
			graftConfigured = true;
		}
	}

	private static String resolveHost() {
		if (hostOverride != null && !hostOverride.isBlank()) {
			return hostOverride;
		}
		String host = System.getenv("GRAFT_HOST");
		if (host != null && !host.isBlank()) {
			return host.trim();
		}
		return DEFAULT_GRAFT_HOST;
	}

	private static void ensureModuleOnJavaClasspath() {
		Path moduleDir = Paths.get(System.getProperty("user.dir"), "resources", "maven", "org.springframework.samples",
				"petclinic-clinic");
		try {
			Files.createDirectories(moduleDir);
		}
		catch (IOException ignored) {
			// SDK may create the tree during GraftConfig.init()
		}
		String absolute = moduleDir.toAbsolutePath().normalize().toString();
		String classpath = System.getProperty("java.class.path", "");
		boolean present = Arrays.stream(classpath.split(File.pathSeparator))
			.filter(entry -> !entry.isBlank())
			.anyMatch(entry -> absolute.equals(Paths.get(entry).toAbsolutePath().normalize().toString()));
		if (!present) {
			System.setProperty("java.class.path",
					classpath.isEmpty() ? absolute : classpath + File.pathSeparator + absolute);
		}
	}

}
