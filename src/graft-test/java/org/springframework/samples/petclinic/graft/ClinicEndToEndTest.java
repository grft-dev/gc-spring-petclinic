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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import graft.maven.org.springframework.samples.petclinic.owner.Clinic;
import graft.maven.org.springframework.samples.petclinic.owner.OwnerDto;
import graft.maven.org.springframework.samples.petclinic.owner.PetDto;
import graft.maven.org.springframework.samples.petclinic.owner.VisitDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClinicEndToEndTest {

	@BeforeAll
	static void pointAtGateway() {
		ClinicGraftBootstrap.configureHostForTests("ws://localhost:8090/ws");
		ClinicGraftBootstrap.ensureReady();
	}

	@Test
	void firstOwnerIsGeorgeFranklin() {
		OwnerDto owner = Clinic.getOwner(1);
		assertThat(owner.getFirstName()).isEqualTo("George");
		assertThat(owner.getLastName()).isEqualTo("Franklin");
	}

@Test
	@Disabled("JVM graft client does not decode array return types with stateless=false (see petclinic-graftcode-defects.md #4)")
	void georgeHasLeoTheCat() {
		PetDto[] pets = Clinic.getPets(1);
		assertThat(pets).hasSize(1);
		assertThat(pets[0].getName()).isEqualTo("Leo");
		assertThat(pets[0].getType()).isEqualTo("cat");
		assertThat(pets[0].getBirthDate()).isEqualTo("2010-09-07");
	}

	@Test
	@Disabled("JVM graft client does not decode array return types with stateless=false (see petclinic-graftcode-defects.md #4)")
	void seededVisitIsSamanthasRabiesShot() {
		VisitDto[] visits = Clinic.getVisits(6, 7);
		assertThat(visits).extracting(VisitDto::getDescription).contains("rabies shot");
		assertThat(visits).extracting(VisitDto::getDate).contains("2013-01-01");
	}

	@Test
	@Disabled("JVM graft client does not decode array return types with stateless=false (see petclinic-graftcode-defects.md #4)")
	void addVisitIsVisibleThroughTheGraft() {
		VisitDto created = Clinic.addVisit(1, 1, "2026-10-05", "graft checkup");
		assertThat(created.getDescription()).isEqualTo("graft checkup");
		assertThat(created.getDate()).isEqualTo("2026-10-05");
		assertThat(Clinic.getVisits(1, 1)).extracting(VisitDto::getDescription).contains("graft checkup");
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		assertThatThrownBy(() -> Clinic.getOwner(999)).hasMessageContaining("Owner with id 999 not found");
	}

}
