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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.model.Vaccine;
import org.springframework.samples.petclinic.repository.VaccineRepository;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided
 * by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we
 * don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code>{@link
 * VaccineServiceTests#clinicService clinicService}</code> instance variable, which uses
 * autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in
 * its own transaction, which is automatically rolled back by default. Thus, even if tests
 * insert or otherwise change database state, there is no need for a teardown or cleanup
 * script.
 * <li>An {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.</li>
 * </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
class VaccineServiceTests {      

	private static final int TEST_VACCINE_DELETE = 8;
	private static final int TEST_VACCINE_EXPIRATED_ID1 = 2;
	private static final int TEST_VACCINE_EXPIRATED_ID2 = 4;
	private static final int TEST_VACCINE_EXPIRATED_ID3 = 8;
	private static final int TEST_VACCINE_STOCK_ID1 = 8;
	private static final int TEST_VACCINE_STOCK_ID2 = 7;
	private static final int TEST_VACCINE_STOCK_ID3 = 3;
	private static final int TEST_VACCINE_STOCK_ID4 = 2;

	@Mock
	private VaccineRepository vaccineRepository;
    @Autowired
	protected VaccineService vaccineService;

    @Autowired
    protected PetService petService;

    @Autowired
    protected InsuranceService insuranceService; 

    @Autowired
    protected InsuranceBaseService insuranceBaseService;

    private Vaccine vaccineCoronavirus;

	@Test
	void shouldFindAll() {
		Collection<Vaccine> vaccines = this.vaccineService.findAll();
		assertThat(vaccines.size()).isEqualTo(13);
	}

	@Test
	void shouldFindById() {
		Vaccine vaccine = this.vaccineService.findById(1);
		assertThat(vaccine.getId()).isEqualTo(1);
	}

	@Test
	void shouldFindAllExpirated() {
		Vaccine vaccine2 = this.vaccineService.findById(TEST_VACCINE_EXPIRATED_ID1);
		Vaccine vaccine4 = this.vaccineService.findById(TEST_VACCINE_EXPIRATED_ID2);
		Vaccine vaccine8 = this.vaccineService.findById(TEST_VACCINE_EXPIRATED_ID3);
		List<Vaccine> vaccinesList1 = this.vaccineService.findAllExpirated();
		List<Vaccine> vaccinesList2 = new ArrayList<>();
		vaccinesList2.add(vaccine2);
		vaccinesList2.add(vaccine4);
		vaccinesList2.add(vaccine8);
		assertThat(vaccinesList1.get(0).getExpiration()).isBefore(LocalDate.now());
		assertThat(vaccinesList1.get(1).getExpiration()).isBefore(LocalDate.now());
		assertThat(vaccinesList1.get(1)).isEqualTo(vaccinesList2.get(1));
		assertThat(vaccinesList1.get(2)).isEqualTo(vaccinesList2.get(2));
		assertThat(vaccinesList1.size()).isEqualTo(3);
	}
	
	@ParameterizedTest
	@ValueSource(ints= {8,7,3,2})
	void shouldFindVaccinesWithLowStock(int argument) {
		Vaccine test = this.vaccineService.findById(argument);
		List<Vaccine> listaStock = this.vaccineService.findVaccinesWithLowStock();
		assertThat(listaStock).contains(test);
	}

	
	@ParameterizedTest
	@ValueSource(ints= {1,5})
	void shouldNotFindVaccinesWithLowStock(int argument) {
		Vaccine test = this.vaccineService.findById(argument);
		List<Vaccine> listaStock = this.vaccineService.findVaccinesWithLowStock();
		Assertions.assertFalse(listaStock.contains(test));
	}
	@Test
	@Transactional
	public void shouldInsertVaccine() {
		Collection<Vaccine> vaccines = this.vaccineService.findAll();
		int found = vaccines.size();

		vaccineCoronavirus = new Vaccine();
        vaccineCoronavirus.setInformation("Vacuna del coronavirus en pruebas, testeado en monos");
        vaccineCoronavirus.setExpiration(LocalDate.of(2021, Month.APRIL, 3));
        vaccineCoronavirus.setName("Vacuna contra el coronavirus");
        vaccineCoronavirus.setPrice(325.25);
        vaccineCoronavirus.setProvider("China");
        vaccineCoronavirus.setSideEffects("Puede provocar crisis nerviosas");
        vaccineCoronavirus.setStock(235);
        
        Collection<PetType> petTypes = this.petService.findPetTypes();
        vaccineCoronavirus.setPetType(EntityUtils.getById(petTypes, PetType.class, 4));

		this.vaccineService.saveVaccine(vaccineCoronavirus);

		vaccines = this.vaccineService.findAll();
		assertThat(vaccines.size()).isEqualTo(found + 1);
		// checks that id has been generated
        assertThat(vaccineCoronavirus.getId()).isNotNull();
	}

	@Test
	@Transactional
	void shouldDeleteVaccine() {
		Collection<Vaccine> vaccines = this.vaccineService.findAll();
		int found = vaccines.size();
		Vaccine vaccine = this.vaccineService.findById(TEST_VACCINE_DELETE);
		this.vaccineService.deleteVaccine(vaccine);
		int numIns = this.insuranceService.findInsurances().size();
		int numInsBas = this.insuranceBaseService.findInsurancesBases().size();
		compruebaNoHayVacunaEliminadaEnSeguro(numIns);
		compruebaNoHayVacunaEliminadaEnSeguroBase(numInsBas);
		vaccines = this.vaccineService.findAll();
		assertThat(vaccines.size()).isEqualTo(found - 1);
		assertThat(this.vaccineService.findById(TEST_VACCINE_DELETE)).isEqualTo(null);
	}

	private void compruebaNoHayVacunaEliminadaEnSeguroBase(int numInsBas) {
		for(int i = 0; i < numInsBas; i++) {
			int z = this.insuranceBaseService.findInsurancesBases().stream().collect(Collectors.toList()).get(i).getId();
			List<Vaccine> v = this.insuranceBaseService.findInsuranceBaseById(z).getVaccines().stream().collect(Collectors.toList());
			for(int j = 0; j < v.size(); j++) {
				Boolean res2 = v.get(j).getId() == TEST_VACCINE_DELETE;
				assertThat(res2).isEqualTo(false);
			}
		}
		
	}

	private void compruebaNoHayVacunaEliminadaEnSeguro(int numIns) {
		for(int i = 0; i < numIns; i++) {
			int z = this.insuranceService.findInsurances().stream().collect(Collectors.toList()).get(i).getId();
			List<Vaccine> v = this.insuranceService.findInsuranceById(z).getVaccines().stream().collect(Collectors.toList());
			for(int j = 0; j < v.size(); j++) {
				Boolean res1 = v.get(j).getId() == TEST_VACCINE_DELETE;
				assertThat(res1).isEqualTo(false);
			}
		}
		
	}
	
	@ParameterizedTest
	@ValueSource(ints= {15,-6,100})
	void shouldFailFindSingleVaccineById(int argument) {
		Assertions.assertThrows(NullPointerException.class, () -> {this.vaccineService.findById(argument).getInformation();});
	}
	
	@ParameterizedTest
	@ValueSource(strings= {"Dientes","2020/04/15","null","14.2"})
	void shouldFailSetTreatmentPriceByStrings(String argument) {
		try {
			this.vaccineService.findById(1).setPrice(Double.parseDouble(argument));
		} catch (NumberFormatException ex) {
			Logger.getLogger(VaccineServiceTests.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Test
	public void addNullVaccineTest() {
		Vaccine dummy = null;
		assertThrows(Exception.class, () -> this.vaccineService.saveVaccine(dummy));
	} 
	 
	@Test
	public void testInvalidVaccine() throws Exception {
       Vaccine vaccine = new Vaccine();
       vaccine.setId(-3);
       VaccineRepository vaccineRepository = mock(VaccineRepository.class);
       when(vaccineRepository.findById(-3)).thenThrow(new RuntimeException());
       VaccineService vaccineService = new VaccineService(vaccineRepository);
       assertThrows(RuntimeException.class, () -> vaccineService.findById((vaccine.getId())));   
	}
	
	@Test
    void shouldFindPetTypes() {
        PetType samplePetType = new PetType();
        samplePetType.setId(1);
        samplePetType.setName("Test");
        List<PetType> samplePets = new ArrayList<PetType>();
        samplePets.add(samplePetType);
        when(vaccineRepository.findPetTypes()).thenReturn(samplePets);
        VaccineService vacService = new VaccineService(vaccineRepository);
        Collection<PetType> petTypes = vacService.findPetTypes();
        assertThat(petTypes).hasSize(1);
        PetType pet = petTypes.iterator().next();
        assertThat(pet.getId()).isEqualTo(1);
        assertThat(pet.getName()).isEqualTo("Test");
    }
	
}
