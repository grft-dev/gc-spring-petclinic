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

import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.ClinicHost;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Clinic {

	private final OwnerRepository owners;

	private final VetRepository vets;

	Clinic(OwnerRepository owners, VetRepository vets) {
		this.owners = owners;
		this.vets = vets;
	}

	public static List<Owner> listOwners() {
		return ClinicHost.clinic().listOwnersInternal();
	}

	public static Collection<Vet> listVets() {
		return ClinicHost.clinic().listVetsInternal();
	}

	public static Owner getOwner(int id) {
		return ClinicHost.clinic().getOwnerInternal(id);
	}

	public static List<Pet> getPets(int ownerId) {
		return ClinicHost.clinic().getPetsInternal(ownerId);
	}

	public static Collection<Visit> getVisits(int ownerId, int petId) {
		return ClinicHost.clinic().getVisitsInternal(ownerId, petId);
	}

	public static Visit addVisit(int ownerId, int petId, LocalDate date, String description) {
		return ClinicHost.clinic().addVisitInternal(ownerId, petId, date, description);
	}

	@Transactional(readOnly = true)
	List<Owner> listOwnersInternal() {
		return this.owners.findAll(Sort.by("id"));
	}

	@Transactional(readOnly = true)
	Collection<Vet> listVetsInternal() {
		return this.vets.findAll();
	}

	@Transactional(readOnly = true)
	Owner getOwnerInternal(int id) {
		return requireOwner(id);
	}

	@Transactional(readOnly = true)
	List<Pet> getPetsInternal(int ownerId) {
		return requireOwner(ownerId).getPets();
	}

	@Transactional(readOnly = true)
	Collection<Visit> getVisitsInternal(int ownerId, int petId) {
		return requirePet(ownerId, petId).getVisits();
	}

	@Transactional
	Visit addVisitInternal(int ownerId, int petId, LocalDate date, String description) {
		Owner owner = requireOwner(ownerId);
		Visit visit = new Visit();
		visit.setDate(date);
		visit.setDescription(description);
		owner.addVisit(petId, visit);
		this.owners.saveAndFlush(owner);
		return visit;
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

}
