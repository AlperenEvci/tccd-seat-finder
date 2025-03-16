package com.ilerijava.tcdd.entegration.DTO;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class AvailableSeatsDTO {
    @JsonIgnore
    private int ekonomiSeats;
    @JsonIgnore
    private int yatakliSeats;
    @JsonIgnore
    private int wheelchairSeats;
    private int totalSeats;
}