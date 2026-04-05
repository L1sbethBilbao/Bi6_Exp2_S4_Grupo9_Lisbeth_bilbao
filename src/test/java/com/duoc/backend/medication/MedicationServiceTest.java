package com.duoc.backend.medication;

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
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void crud_flow() {
        Medication m = new Medication();
        m.setId(1L);
        when(medicationRepository.findAll()).thenReturn(List.of(m));
        assertThat(medicationService.getAllMedications()).hasSize(1);
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(m));
        assertThat(medicationService.getMedicationById(1L)).isSameAs(m);
        when(medicationRepository.save(m)).thenReturn(m);
        assertThat(medicationService.saveMedication(m)).isSameAs(m);
        medicationService.deleteMedication(1L);
        verify(medicationRepository).deleteById(1L);
    }
}
