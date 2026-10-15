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

import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.samples.petclinic.ClinicGraftBoot;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Clinic {

	private static volatile Clinic bean;

	private final OwnerRepository owners;

	private final VetRepository vets;

	Clinic(OwnerRepository owners, VetRepository vets) {
		this.owners = owners;
		this.vets = vets;
	}

	public static OwnerDto[] listOwners() {
		return service().listOwnersInternal();
	}

	public static VetDto[] listVets() {
		return service().listVetsInternal();
	}

	public static OwnerDto getOwner(int id) {
		return service().getOwnerInternal(id);
	}

	public static PetDto[] getPets(int ownerId) {
		return service().getPetsInternal(ownerId);
	}

	public static VisitDto[] getVisits(int ownerId, int petId) {
		return service().getVisitsInternal(ownerId, petId);
	}

	public static VisitDto addVisit(int ownerId, int petId, String date, String description) {
		return service().addVisitInternal(ownerId, petId, date, description);
	}

	private static Clinic service() {
		Clinic current = bean;
		if (current == null) {
			synchronized (Clinic.class) {
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

	@Transactional(readOnly = true)
	OwnerDto[] listOwnersInternal() {
		return this.owners.findAll(Sort.by("id")).stream().map(Clinic::toOwner).toArray(OwnerDto[]::new);
	}

	@Transactional(readOnly = true)
	VetDto[] listVetsInternal() {
		return this.vets.findAll().stream().map(Clinic::toVet).toArray(VetDto[]::new);
	}

	@Transactional(readOnly = true)
	OwnerDto getOwnerInternal(int id) {
		return toOwner(requireOwner(id));
	}

	@Transactional(readOnly = true)
	PetDto[] getPetsInternal(int ownerId) {
		return requireOwner(ownerId).getPets().stream().map(Clinic::toPet).toArray(PetDto[]::new);
	}

	@Transactional(readOnly = true)
	VisitDto[] getVisitsInternal(int ownerId, int petId) {
		return requirePet(ownerId, petId).getVisits().stream().map(Clinic::toVisit).toArray(VisitDto[]::new);
	}

	@Transactional
	VisitDto addVisitInternal(int ownerId, int petId, String date, String description) {
		Owner owner = requireOwner(ownerId);
		Visit visit = new Visit();
		visit.setDate(LocalDate.parse(date));
		visit.setDescription(description);
		owner.addVisit(petId, visit);
		this.owners.saveAndFlush(owner);
		Visit stored = requirePet(ownerId, petId).getVisits()
			.stream()
			.filter(item -> visit.getDate().equals(item.getDate()) && description.equals(item.getDescription()))
			.reduce((first, second) -> second)
			.orElseThrow(() -> new ClinicException("Visit was not stored for pet " + petId + "."));
		return toVisit(stored);
	}

	private Owner requireOwner(int id) {
		return this.owners.findById(id).orElseThrow(() -> new ClinicException("Owner with id " + id + " not found."));
	}

	private Pet requirePet(int ownerId, int petId) {
		Owner owner = requireOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new ClinicException("Pet with id " + petId + " not found for owner " + ownerId + ".");
		}
		return pet;
	}

	private static OwnerDto toOwner(Owner owner) {
		OwnerDto dto = new OwnerDto();
		dto.setId(owner.getId());
		dto.setFirstName(owner.getFirstName());
		dto.setLastName(owner.getLastName());
		dto.setAddress(owner.getAddress());
		dto.setCity(owner.getCity());
		dto.setTelephone(owner.getTelephone());
		return dto;
	}

	private static PetDto toPet(Pet pet) {
		PetDto dto = new PetDto();
		dto.setId(pet.getId());
		dto.setName(pet.getName());
		dto.setBirthDate(pet.getBirthDate().toString());
		dto.setType(pet.getType().getName());
		return dto;
	}

	private static VetDto toVet(Vet vet) {
		VetDto dto = new VetDto();
		dto.setId(vet.getId());
		dto.setFirstName(vet.getFirstName());
		dto.setLastName(vet.getLastName());
		dto.setSpecialties(vet.getSpecialties().stream().map(specialty -> specialty.getName()).toArray(String[]::new));
		return dto;
	}

	private static VisitDto toVisit(Visit visit) {
		VisitDto dto = new VisitDto();
		dto.setId(visit.getId());
		dto.setDate(visit.getDate().toString());
		dto.setDescription(visit.getDescription());
		return dto;
	}

}
