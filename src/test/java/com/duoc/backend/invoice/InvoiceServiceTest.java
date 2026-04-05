package com.duoc.backend.invoice;

import com.duoc.backend.appointment.Appointment;
import com.duoc.backend.appointment.AppointmentRepository;
import com.duoc.backend.care.Care;
import com.duoc.backend.care.CareRepository;
import com.duoc.backend.medication.Medication;
import com.duoc.backend.medication.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private CareRepository careRepository;
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Appointment visit;
    private Medication med;
    private Care care;

    @BeforeEach
    void setUp() {
        visit = new Appointment();
        visit.setId(1L);
        med = new Medication();
        med.setId(10L);
        med.setCost(100.0);
        care = new Care();
        care.setId(20L);
        care.setCost(50.0);
    }

    @Test
    void getAllInvoicesDelegatesToRepository() {
        when(invoiceRepository.findAll()).thenReturn(List.of(new Invoice()));
        assertThat(invoiceService.getAllInvoices()).hasSize(1);
    }

    @Test
    void getInvoiceByIdReturnsOptional() {
        Invoice inv = new Invoice();
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(inv));
        assertThat(invoiceService.getInvoiceById(1L)).contains(inv);
        when(invoiceRepository.findById(2L)).thenReturn(Optional.empty());
        assertThat(invoiceService.getInvoiceById(2L)).isEmpty();
    }

    @Test
    void getInvoicesByAppointmentIdDelegates() {
        when(invoiceRepository.findByAppointment_Id(3L)).thenReturn(List.of(new Invoice()));
        assertThat(invoiceService.getInvoicesByAppointmentId(3L)).hasSize(1);
    }

    @Test
    void saveInvoiceSuccessWithMedicationsAndCaresAndExtras() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setMedications(new ArrayList<>(List.of(med)));
        input.setCares(new ArrayList<>(List.of(care)));
        input.setAdditionalCharges(new ArrayList<>(List.of(new AdditionalChargeItem("x", 5.0))));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(medicationRepository.findAllById(List.of(10L))).thenReturn(List.of(med));
        when(careRepository.findAllById(List.of(20L))).thenReturn(List.of(care));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice saved = invoiceService.saveInvoice(input);
        assertThat(saved.getTotalCost()).isEqualTo(155.0);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void saveInvoiceEmptyMedicationsAndCares() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice saved = invoiceService.saveInvoice(input);
        assertThat(saved.getMedications()).isEmpty();
        assertThat(saved.getCares()).isEmpty();
        assertThat(saved.getTotalCost()).isZero();
    }

    @Test
    void saveInvoiceRejectsMissingAppointment() {
        Invoice input = new Invoice();
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("visita");
    }

    @Test
    void saveInvoiceRejectsMissingAppointmentId() {
        Invoice input = new Invoice();
        input.setAppointment(new Appointment());
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveInvoiceRejectsUnknownVisit() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(99L));
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void saveInvoiceRejectsMedicationWithoutId() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        Medication m = new Medication();
        input.setMedications(List.of(m));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("medicamento");
    }

    @Test
    void saveInvoiceRejectsUnknownMedication() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setMedications(List.of(med));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(medicationRepository.findAllById(List.of(10L))).thenReturn(List.of());
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("medicamentos");
    }

    @Test
    void saveInvoiceRejectsCareWithoutId() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setCares(List.of(new Care()));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("servicio");
    }

    @Test
    void saveInvoiceRejectsUnknownCare() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setCares(List.of(care));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(careRepository.findAllById(List.of(20L))).thenReturn(List.of());
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("servicios");
    }

    @Test
    void saveInvoiceRejectsBlankAdditionalDescription() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setAdditionalCharges(List.of(new AdditionalChargeItem("  ", 1.0)));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("descripción");
    }

    @Test
    void saveInvoiceRejectsNegativeAdditionalAmount() {
        Invoice input = new Invoice();
        input.setAppointment(appointmentRef(1L));
        input.setAdditionalCharges(List.of(new AdditionalChargeItem("ok", -1.0)));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(visit));
        assertThatThrownBy(() -> invoiceService.saveInvoice(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monto");
    }

    @Test
    void deleteInvoiceSuccess() {
        when(invoiceRepository.existsById(1L)).thenReturn(true);
        invoiceService.deleteInvoice(1L);
        verify(invoiceRepository).deleteById(1L);
    }

    @Test
    void deleteInvoiceNotFound() {
        when(invoiceRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> invoiceService.deleteInvoice(1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    private static Appointment appointmentRef(Long id) {
        Appointment a = new Appointment();
        a.setId(id);
        return a;
    }
}
