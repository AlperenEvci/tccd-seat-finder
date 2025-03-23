package com.ilerijava.tcdd.entegration.util;

import com.ilerijava.tcdd.entegration.DTO.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrainFunctions {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * String formatındaki tarihi LocalDateTime'a çevirir
     * Format: dd-MM-yyyy HH:mm:ss
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr.replace(" ", "T"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));
    }

    /**
     * Tren segmentinin geçerli olup olmadığını kontrol eder
     * 
     * @param segment       Kontrol edilecek tren segmenti
     * @param fromStationId Başlangıç istasyonu ID'si
     * @param startDateTime Başlangıç tarihi
     * @param endDateTime   Bitiş tarihi
     */
    public static boolean isValidSegment(SeferResponseDto.TrainSegment segment,
            Integer fromStationId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        LocalDateTime segmentTime = segment.departureTime();
        return !segmentTime.isBefore(startDateTime) &&
                !segmentTime.isAfter(endDateTime) &&
                segment.departureStationId() == fromStationId;
    }

    /**
     * Belirli bir tren için müsait koltuk olup olmadığını kontrol eder
     */
    public static boolean hasAvailableSeats(SeferResponseDto.Train train, String seatTypeCode) {
        return train.availableFareInfo().stream()
                .flatMap(fareInfo -> fareInfo.cabinClasses().stream())
                .anyMatch(cabinClass -> cabinClass.cabinClass().code().equals(seatTypeCode) &&
                        cabinClass.availabilityCount() > 0);
    }

    /**
     * Tren için koltuk bilgilerini oluşturur
     */
    public static AvailableSeatsDTO createSeatInfo(SeferResponseDto.Train train, String seatTypeCode) {
        AvailableSeatsDTO seatsInfo = new AvailableSeatsDTO();
        train.availableFareInfo().stream()
                .flatMap(fareInfo -> fareInfo.cabinClasses().stream())
                .filter(cabinClass -> seatTypeCode.equals("ALL") || cabinClass.cabinClass().code().equals(seatTypeCode))
                .forEach(cabinClass -> updateSeatCount(seatsInfo, cabinClass));

        seatsInfo.setTotalSeats(seatsInfo.getEkonomiSeats() +
                seatsInfo.getYatakliSeats() +
                seatsInfo.getWheelchairSeats());

        return seatsInfo;
    }

    /**
     * Koltuk tipine göre koltuk sayısını günceller
     */
    private static void updateSeatCount(AvailableSeatsDTO seatsInfo,
            SeferResponseDto.CabinClass cabinClass) {
        switch (cabinClass.cabinClass().code()) {
            case "Y1":
                seatsInfo.setEkonomiSeats(seatsInfo.getEkonomiSeats() +
                        cabinClass.availabilityCount());
                break;
            case "B":
                seatsInfo.setYatakliSeats(seatsInfo.getYatakliSeats() +
                        cabinClass.availabilityCount());
                break;
            case "DSB":
                seatsInfo.setWheelchairSeats(seatsInfo.getWheelchairSeats() +
                        cabinClass.availabilityCount());
                break;
        }
    }

    /**
     * Tren için kalkış zamanını formatlar
     */
    public static String formatDepartureTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }
}