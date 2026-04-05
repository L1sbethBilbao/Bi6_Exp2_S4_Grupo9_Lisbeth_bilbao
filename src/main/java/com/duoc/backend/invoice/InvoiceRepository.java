package com.duoc.backend.invoice;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    List<Invoice> findByAppointment_Id(Long appointmentId);
}
