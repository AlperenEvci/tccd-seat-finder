
package com.ilerijava.tcdd.entegration.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SearchRoute {
    private int departureStationId;
    private String departureStationName;
    private int arrivalStationId;
    private String arrivalStationName;
    private LocalDateTime departureDate;

    public SearchRoute(int departureStationId, String departureStationName, int arrivalStationId, String arrivalStationName, LocalDateTime departureDate) {
        this.departureStationId = departureStationId;
        this.departureStationName = departureStationName;
        this.arrivalStationId = arrivalStationId;
        this.arrivalStationName = arrivalStationName;
        this.departureDate = departureDate;
    }

    // Getter ve Setter metotları
}
