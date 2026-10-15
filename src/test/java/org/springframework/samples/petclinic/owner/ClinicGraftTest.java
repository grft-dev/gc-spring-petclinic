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
package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ClinicGraftBoot.class)
@ActiveProfiles("graft")
class ClinicGraftTest {

	@Autowired
	private Clinic clinic;

	@Test
	void firstOwnerIsGeorgeFranklin() {
		OwnerDto owner = this.clinic.getOwnerInternal(1);
		assertThat(owner.getFirstName()).isEqualTo("George");
		assertThat(owner.getLastName()).isEqualTo("Franklin");
	}

	@Test
	void georgeHasLeoTheCat() {
		PetDto[] pets = this.clinic.getPetsInternal(1);
		assertThat(pets).hasSize(1);
		assertThat(pets[0].getName()).isEqualTo("Leo");
		assertThat(pets[0].getType()).isEqualTo("cat");
		assertThat(pets[0].getBirthDate()).isEqualTo("2010-09-07");
	}

	@Test
	void seededVisitIsSamanthasRabiesShot() {
		VisitDto[] visits = this.clinic.getVisitsInternal(6, 7);
		assertThat(visits).extracting(VisitDto::getDescription).contains("rabies shot");
		assertThat(visits).extracting(VisitDto::getDate).contains("2013-01-01");
	}

	@Test
	void addVisitDelegatesToOwner() {
		VisitDto created = this.clinic.addVisitInternal(1, 1, "2026-10-05", "graft checkup");
		assertThat(created.getDescription()).isEqualTo("graft checkup");
		assertThat(created.getDate()).isEqualTo("2026-10-05");
		assertThat(this.clinic.getVisitsInternal(1, 1)).extracting(VisitDto::getDescription).contains("graft checkup");
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		assertThatThrownBy(() -> this.clinic.getOwnerInternal(999)).isInstanceOf(ClinicException.class)
			.hasMessage("Owner with id 999 not found.");
	}

}
