package com.ilerijava.tcdd.entegration.integration;

import com.ilerijava.tcdd.entegration.service.SeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import com.ilerijava.tcdd.entegration.DTO.TrainSeatsResponseDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainAvailableSeatsDTO;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.format.DateTimeFormatter;

@SpringBootTest
public class SeatControllerIntegrationTest {

    @Autowired
    private SeatService seatService;

    @Test
    public void getAvailableSeatsBetweenDates_ShouldReturnTrainSeats() {
        // Given

        LocalDateTime gidisTarih = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);
        LocalDateTime gidisTarihSon = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0);
        String binisIstasyonu = "ESKİŞEHİR";
        String inisIstasyonu = "BALIKESİR";
        int binisIstasyonuId = 93;
        int inisIstasyonuId = 1159;
        String koltukTipi = "EKONOMI";

        TrainSeatsResponseDTO trainSeatsResponseDTO = seatService.getAvailableSeatsBetweenDates(binisIstasyonuId,
                binisIstasyonu, inisIstasyonuId,
                inisIstasyonu, gidisTarih, gidisTarihSon, koltukTipi);

        assertThat(trainSeatsResponseDTO).isNotNull();
        assertThat(trainSeatsResponseDTO.getTrains()).isNotEmpty();

        // Her seferin detaylarını kontrol et
        for (TrainAvailableSeatsDTO train : trainSeatsResponseDTO.getTrains()) {
            assertThat(train.getTrainName()).isNotEmpty();
            assertThat(train.getDepartureTime()).isNotNull();
            assertThat(train.getSeatInfo()).isNotNull();
            assertThat(train.getSeatInfo().getTotalSeats()).isGreaterThan(0);
        }

        // tarih aralığı kontrolü
        LocalDateTime departureTime = LocalDateTime.parse(trainSeatsResponseDTO.getTrains().get(0).getDepartureTime(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        assertTrue(departureTime.isAfter(gidisTarih.minusMinutes(1)));
        assertTrue(departureTime.isBefore(gidisTarihSon.plusMinutes(1)));

        // Sonuçları yazdır
        System.out.println("\nBulunan Seferler:");
        System.out.println("------------------");
        for (TrainAvailableSeatsDTO train : trainSeatsResponseDTO.getTrains()) {
            System.out.printf("Tren: %s\n", train.getTrainName());
            System.out.printf("Kalkış: %s\n", train.getDepartureTime());
        }
    }
}