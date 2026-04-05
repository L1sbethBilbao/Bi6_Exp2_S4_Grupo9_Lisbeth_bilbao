package com.duoc.backend.Invoice;

import com.duoc.backend.Appointment.Appointment;
import com.duoc.backend.Appointment.AppointmentRepository;
import com.duoc.backend.Care.Care;
import com.duoc.backend.Care.CareRepository;
import com.duoc.backend.Medication.Medication;
import com.duoc.backend.Medication.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MedicationRepository medicationRepository;
    private final CareRepository careRepository;
    private final AppointmentRepository appointmentRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            MedicationRepository medicationRepository,
            CareRepository careRepository,
            AppointmentRepository appointmentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.medicationRepository = medicationRepository;
        this.careRepository = careRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Iterable<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    public List<Invoice> getInvoicesByAppointmentId(Long appointmentId) {
        return invoiceRepository.findByAppointment_Id(appointmentId);
    }

    public Invoice saveInvoice(Invoice invoice) {
        if (invoice.getAppointment() == null || invoice.getAppointment().getId() == null) {
            throw new IllegalArgumentException("La factura debe incluir una visita (appointment) con id válido.");
        }

        Appointment visit = appointmentRepository.findById(invoice.getAppointment().getId())
                .orElseThrow(() -> new IllegalArgumentException("La visita indicada no existe."));

        List<Medication> medications = invoice.getMedications() != null ? invoice.getMedications() : new ArrayList<>();
        List<Care> cares = invoice.getCares() != null ? invoice.getCares() : new ArrayList<>();
        List<AdditionalChargeItem> extras = invoice.getAdditionalCharges() != null
                ? invoice.getAdditionalCharges()
                : new ArrayList<>();

        if (!medications.isEmpty()) {
            List<Long> medIds = medications.stream().map(Medication::getId).filter(id -> id != null).collect(Collectors.toList());
            if (medIds.size() != medications.size()) {
                throw new IllegalArgumentException("Cada medicamento debe tener id.");
            }
            List<Medication> validMedications = StreamSupport.stream(
                    medicationRepository.findAllById(medIds).spliterator(), false
            ).collect(Collectors.toList());
            if (validMedications.size() != medications.size()) {
                throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
            }
            invoice.setMedications(validMedications);
        } else {
            invoice.setMedications(new ArrayList<>());
        }

        if (!cares.isEmpty()) {
            List<Long> careIds = cares.stream().map(Care::getId).filter(id -> id != null).collect(Collectors.toList());
            if (careIds.size() != cares.size()) {
                throw new IllegalArgumentException("Cada servicio debe tener id.");
            }
            List<Care> validCares = StreamSupport.stream(
                    careRepository.findAllById(careIds).spliterator(), false
            ).collect(Collectors.toList());
            if (validCares.size() != cares.size()) {
                throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
            }
            invoice.setCares(validCares);
        } else {
            invoice.setCares(new ArrayList<>());
        }

        for (AdditionalChargeItem item : extras) {
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new IllegalArgumentException("Cada cargo adicional debe tener descripción.");
            }
            if (item.getAmount() == null || item.getAmount() < 0) {
                throw new IllegalArgumentException("Cada cargo adicional debe tener monto mayor o igual a 0.");
            }
        }
        invoice.setAdditionalCharges(extras);
        invoice.setAppointment(visit);

        double totalCare = invoice.getCares().stream().mapToDouble(Care::getCost).sum();
        double totalMed = invoice.getMedications().stream().mapToDouble(Medication::getCost).sum();
        double totalExtra = invoice.getAdditionalCharges().stream().mapToDouble(AdditionalChargeItem::getAmount).sum();

        invoice.setTotalCost(totalCare + totalMed + totalExtra);
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new NoSuchElementException("Factura no encontrada.");
        }
        invoiceRepository.deleteById(id);
    }
}
