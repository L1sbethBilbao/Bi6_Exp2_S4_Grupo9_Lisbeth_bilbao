package com.duoc.backend.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AdditionalChargeItem implements Serializable {

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Double amount;

    public AdditionalChargeItem() {
    }

    public AdditionalChargeItem(String description, Double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdditionalChargeItem that = (AdditionalChargeItem) o;
        return Objects.equals(description, that.description) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount);
    }
}
