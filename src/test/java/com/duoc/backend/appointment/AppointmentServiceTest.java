package com.duoc.backend.appointment;

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
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void crud_flow() {
        Appointment a = new Appointment();
        a.setId(1L);
        when(appointmentRepository.findAll()).thenReturn(List.of(a));
        assertThat(appointmentService.getAllAppointments()).hasSize(1);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));
        assertThat(appointmentService.getAppointmentById(1L)).isSameAs(a);
        when(appointmentRepository.save(a)).thenReturn(a);
        assertThat(appointmentService.saveAppointment(a)).isSameAs(a);
        appointmentService.deleteAppointment(1L);
        verify(appointmentRepository).deleteById(1L);
    }
}
