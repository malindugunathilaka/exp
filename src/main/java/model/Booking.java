package model;

import java.util.Date;

public class Booking {
    private int id;
    private int guestId;
    private int roomId;
    private Date checkInDate;
    private Date checkOutDate;
    private double totalPrice;
    private String status;

    // For joined queries
    private String roomNumber;
    private String guestName;

    // Default constructor
    public Booking() {}

    // Constructor without id
    public Booking(int guestId, int roomId, Date checkInDate, Date checkOutDate, double totalPrice) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = "Booked";
    }

    // Constructor with all fields
    public Booking(int id, int guestId, int roomId, Date checkInDate, Date checkOutDate,
                   double totalPrice, String status) {
        this.id = id;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    // Utility methods
    public boolean isActive() {
        return "Booked".equals(status) || "Checked In".equals(status);
    }

    public void checkIn() {
        this.status = "Checked In";
    }

    public void checkOut() {
        this.status = "Checked Out";
    }

    public void cancel() {
        this.status = "Cancelled";
    }

    public long getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            long diff = checkOutDate.getTime() - checkInDate.getTime();
            return diff / (1000 * 60 * 60 * 24);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", guestId=" + guestId +
                ", roomId=" + roomId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}