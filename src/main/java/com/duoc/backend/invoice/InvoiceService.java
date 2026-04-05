package com.duoc.backend.invoice;

import com.duoc.backend.appointment.Appointment;
import com.duoc.backend.appointment.AppointmentRepository;
import com.duoc.backend.care.Care;
import com.duoc.backend.care.CareRepository;
import com.duoc.backend.medication.Medication;
import com.duoc.backend.medication.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
        requireAppointmentWithId(invoice);
        Appointment visit = loadAppointment(invoice.getAppointment().getId());
        invoice.setMedications(resolveMedications(invoice.getMedications()));
        invoice.setCares(resolveCares(invoice.getCares()));
        List<AdditionalChargeItem> extras = nullToEmpty(invoice.getAdditionalCharges());
        validateAdditionalCharges(extras);
        invoice.setAdditionalCharges(extras);
        invoice.setAppointment(visit);
        invoice.setTotalCost(computeTotal(invoice));
        return invoiceRepository.save(invoice);
    }

    private static void requireAppointmentWithId(Invoice invoice) {
        if (invoice.getAppointment() == null || invoice.getAppointment().getId() == null) {
            throw new IllegalArgumentException("La factura debe incluir una visita (appointment) con id válido.");
        }
    }

    private Appointment loadAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("La visita indicada no existe."));
    }

    private List<Medication> resolveMedications(List<Medication> medications) {
        List<Medication> list = nullToEmpty(medications);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> medIds = list.stream().map(Medication::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (medIds.size() != list.size()) {
            throw new IllegalArgumentException("Cada medicamento debe tener id.");
        }
        List<Medication> valid = StreamSupport.stream(
                medicationRepository.findAllById(medIds).spliterator(), false
        ).collect(Collectors.toList());
        if (valid.size() != list.size()) {
            throw new IllegalArgumentException("Algunos medicamentos no existen en la base de datos.");
        }
        return valid;
    }

    private List<Care> resolveCares(List<Care> cares) {
        List<Care> list = nullToEmpty(cares);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> careIds = list.stream().map(Care::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (careIds.size() != list.size()) {
            throw new IllegalArgumentException("Cada servicio debe tener id.");
        }
        List<Care> valid = StreamSupport.stream(
                careRepository.findAllById(careIds).spliterator(), false
        ).collect(Collectors.toList());
        if (valid.size() != list.size()) {
            throw new IllegalArgumentException("Algunos servicios no existen en la base de datos.");
        }
        return valid;
    }

    private static void validateAdditionalCharges(List<AdditionalChargeItem> extras) {
        for (AdditionalChargeItem item : extras) {
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new IllegalArgumentException("Cada cargo adicional debe tener descripción.");
            }
            if (item.getAmount() == null || item.getAmount() < 0) {
                throw new IllegalArgumentException("Cada cargo adicional debe tener monto mayor o igual a 0.");
            }
        }
    }

    private static double computeTotal(Invoice invoice) {
        double totalCare = invoice.getCares().stream().mapToDouble(Care::getCost).sum();
        double totalMed = invoice.getMedications().stream().mapToDouble(Medication::getCost).sum();
        double totalExtra = invoice.getAdditionalCharges().stream().mapToDouble(AdditionalChargeItem::getAmount).sum();
        return totalCare + totalMed + totalExtra;
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }

    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new NoSuchElementException("Factura no encontrada.");
        }
        invoiceRepository.deleteById(id);
    }
}
