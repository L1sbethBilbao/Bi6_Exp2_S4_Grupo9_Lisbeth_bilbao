package com.duoc.backend.invoice;

import com.duoc.backend.appointment.Appointment;
import com.duoc.backend.care.Care;
import com.duoc.backend.medication.Medication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Visita (cita) a la que pertenece la factura — obligatorio según actividad. */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Appointment appointment;

    @ManyToMany
    @JoinTable(
            name = "invoice_cares",
            joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "care_id")
    )
    private List<Care> cares = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "invoice_medications",
            joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id")
    )
    private List<Medication> medications = new ArrayList<>();

    /** Cargos adicionales (material, estacionamiento, etc.). */
    @ElementCollection
    @CollectionTable(name = "invoice_additional_charges", joinColumns = @JoinColumn(name = "invoice_id"))
    private List<AdditionalChargeItem> additionalCharges = new ArrayList<>();

    private Double totalCost;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public List<Care> getCares() {
        return cares;
    }

    public void setCares(List<Care> cares) {
        this.cares = cares != null ? cares : new ArrayList<>();
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications != null ? medications : new ArrayList<>();
    }

    public List<AdditionalChargeItem> getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(List<AdditionalChargeItem> additionalCharges) {
        this.additionalCharges = additionalCharges != null ? additionalCharges : new ArrayList<>();
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}
