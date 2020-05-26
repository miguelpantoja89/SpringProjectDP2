package org.springframework.samples.petclinic.repository;



import java.util.List;


import org.springframework.samples.petclinic.model.Appointment;




public interface AppointmentRepository {

	void save(Appointment appointment);

	Appointment findAppById(int appointementId);

	List<Appointment> findAll();

}
