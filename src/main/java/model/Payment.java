package model;

import java.util.Date;

public class Payment {
    private int id;
    private int bookingId;
    private double amount;
    private Date paymentDate;
    private String method;

    // Default constructor
    public Payment() {}

    // Constructor without id and date (date will be set automatically)
    public Payment(int bookingId, double amount, String method) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.paymentDate = new Date(); // Current date/time
    }

    // Constructor with all fields
    public Payment(int id, int bookingId, double amount, Date paymentDate, String method) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    // Utility methods
    public boolean isCreditCardPayment() {
        return "Credit Card".equals(method);
    }

    public boolean isCashPayment() {
        return "Cash".equals(method);
    }

    public boolean isBankTransfer() {
        return "Bank Transfer".equals(method);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", method='" + method + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Payment payment = (Payment) obj;
        return id == payment.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

