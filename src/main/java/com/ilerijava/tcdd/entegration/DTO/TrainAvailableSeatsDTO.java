package com.ilerijava.tcdd.entegration.DTO;

import lombok.Data;

@Data
public class TrainAvailableSeatsDTO {
    private String trainName; // Tren adı (örn: "İZMİR MAVİ EKSPRESİ")
    private String departureTime; // String olarak değiştirdik
    private AvailableSeatsDTO seatInfo;
}