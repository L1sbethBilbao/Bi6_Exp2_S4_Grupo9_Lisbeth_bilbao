package com.duoc.backend;

import com.duoc.backend.appointment.Appointment;
import com.duoc.backend.care.Care;
import com.duoc.backend.invoice.AdditionalChargeItem;
import com.duoc.backend.invoice.Invoice;
import com.duoc.backend.medication.Medication;
import com.duoc.backend.patient.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelsTest {

    @Test
    void userUserDetailsAndProperties() {
        User u = new User();
        u.setId(1);
        u.setUsername("u");
        u.setEmail("e@e");
        u.setPassword("p");
        assertThat(u.getUsername()).isEqualTo("u");
        assertThat(u.getPassword()).isEqualTo("p");
        assertThat(u.getEmail()).isEqualTo("e@e");
        assertThat(u.getId()).isEqualTo(1);
        assertThat(u.getAuthorities()).isNotEmpty();
        assertThat(u.isAccountNonExpired()).isTrue();
        assertThat(u.isAccountNonLocked()).isTrue();
        assertThat(u.isCredentialsNonExpired()).isTrue();
        assertThat(u.isEnabled()).isTrue();
    }

    @Test
    void patientGettersSetters() {
        Patient p = new Patient();
        p.setId(1L);
        p.setName("n");
        p.setSpecies("dog");
        p.setBreed("b");
        p.setAge(3);
        p.setOwner("o");
        assertThat(p.getName()).isEqualTo("n");
        assertThat(p.getSpecies()).isEqualTo("dog");
        assertThat(p.getBreed()).isEqualTo("b");
        assertThat(p.getAge()).isEqualTo(3);
        assertThat(p.getOwner()).isEqualTo("o");
        assertThat(p.getId()).isEqualTo(1L);
    }

    @Test
    void appointmentGettersSetters() {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setDate(LocalDate.of(2026, 1, 2));
        a.setTime(LocalTime.of(10, 30));
        a.setReason("r");
        a.setVeterinarian("v");
        assertThat(a.getDate()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(a.getTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(a.getReason()).isEqualTo("r");
        assertThat(a.getVeterinarian()).isEqualTo("v");
    }

    @Test
    void careAndMedication() {
        Care c = new Care();
        c.setId(1L);
        c.setName("c");
        c.setCost(1.5);
        assertThat(c.getCost()).isEqualTo(1.5);
        Medication m = new Medication();
        m.setId(2L);
        m.setName("m");
        m.setCost(2.5);
        assertThat(m.getName()).isEqualTo("m");
    }

    @Test
    void invoiceAndAdditionalCharge() {
        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setTotalCost(9.0);
        Appointment ap = new Appointment();
        ap.setId(3L);
        inv.setAppointment(ap);
        inv.setCares(List.of());
        inv.setMedications(List.of());
        inv.setAdditionalCharges(List.of(new AdditionalChargeItem("d", 1.0)));
        assertThat(inv.getTotalCost()).isEqualTo(9.0);
        assertThat(inv.getAppointment().getId()).isEqualTo(3L);

        AdditionalChargeItem a = new AdditionalChargeItem("x", 2.0);
        AdditionalChargeItem b = new AdditionalChargeItem("x", 2.0);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(new AdditionalChargeItem("y", 2.0));
    }
}
