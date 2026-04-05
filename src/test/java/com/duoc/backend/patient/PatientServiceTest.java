package com.duoc.backend.patient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void crud_flow() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findAll()).thenReturn(List.of(p));
        assertThat(patientService.getAllPatients()).hasSize(1);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThat(patientService.getPatientById(1L)).isSameAs(p);
        when(patientRepository.save(p)).thenReturn(p);
        assertThat(patientService.savePatient(p)).isSameAs(p);
        patientService.deletePatient(1L);
        verify(patientRepository).deleteById(1L);
    }
}
