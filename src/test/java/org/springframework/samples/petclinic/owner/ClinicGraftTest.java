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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vet.SpecialtyNames;
import org.springframework.samples.petclinic.vet.Vet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClinicGraftTest {

	@Test
	void firstOwnerIsGeorgeFranklin() {
		Owner owner = Clinic.getOwner(1);
		assertThat(owner.getFirstName()).isEqualTo("George");
		assertThat(owner.getLastName()).isEqualTo("Franklin");
	}

	@Test
	void georgeHasLeoTheCat() {
		List<Pet> pets = Clinic.getPets(1);
		assertThat(pets).hasSize(1);
		assertThat(pets.get(0).getName()).isEqualTo("Leo");
		assertThat(pets.get(0).getType().getName()).isEqualTo("cat");
		assertThat(pets.get(0).getBirthDate()).isEqualTo(LocalDate.of(2010, 9, 7));
	}

	@Test
	void seededVisitIsSamanthasRabiesShot() {
		Collection<Visit> visits = Clinic.getVisits(6, 7);
		assertThat(visits).extracting(Visit::getDescription).contains("rabies shot");
		assertThat(visits).extracting(Visit::getDate).contains(LocalDate.of(2013, 1, 1));
	}

	@Test
	void addVisitDelegatesToOwner() {
		Visit created = Clinic.addVisit(1, 1, LocalDate.of(2026, 10, 5), "graft checkup");
		assertThat(created.getDescription()).isEqualTo("graft checkup");
		assertThat(created.getDate()).isEqualTo(LocalDate.of(2026, 10, 5));
		assertThat(Clinic.getVisits(1, 1)).extracting(Visit::getDescription).contains("graft checkup");
	}

	@Test
	void lindaDouglasHasDentistryAndSurgery() {
		Collection<Vet> vets = Clinic.listVets();
		assertThat(vets).filteredOn(vet -> vet.getLastName().equals("Douglas"))
			.singleElement()
			.satisfies(vet -> {
				assertThat(vet.getFirstName()).isEqualTo("Linda");
				assertThat(SpecialtyNames.of(vet)).containsExactlyInAnyOrder("dentistry", "surgery");
			});
	}

	@Test
	void missingOwnerKeepsTheMessage() {
		assertThatThrownBy(() -> Clinic.getOwner(999)).isInstanceOf(ClinicException.class)
			.hasMessage("Owner with id 999 not found.");
	}

}
