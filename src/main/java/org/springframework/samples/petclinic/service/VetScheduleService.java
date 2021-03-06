/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Appointment;
import org.springframework.samples.petclinic.model.VetSchedule;
import org.springframework.samples.petclinic.repository.VetScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all Petclinic controllers Also a placeholder
 * for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 */
@Service
public class VetScheduleService {

	private VetScheduleRepository vetScheduleRepository;


	@Autowired
	public VetScheduleService(VetScheduleRepository vetScheduleRepository) {
		this.vetScheduleRepository = vetScheduleRepository;
	}		

	public List<Appointment> findAppointmentsByDay(LocalDate day) {
		return vetScheduleRepository.findAppointmentsByDay(day);
	}		
  public Collection<VetSchedule> findAll() {
		
		return this.vetScheduleRepository.findAll();
	}

	public void saveVtSchedule(VetSchedule vtSchedule) {
		 this.vetScheduleRepository.save(vtSchedule);
		
	}
	
	@Transactional
	public VetSchedule findById(int id) {
		return vetScheduleRepository.findById(id);	
	}

}
