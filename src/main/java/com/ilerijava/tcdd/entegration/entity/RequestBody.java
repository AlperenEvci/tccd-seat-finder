package com.ilerijava.tcdd.entegration.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RequestBody {

    // take the data from the user
    private int departureStationId;
    private String departureStationName;
    private int arrivalStationId;
    private String arrivalStationName;
    private LocalDateTime departureDate;

}
