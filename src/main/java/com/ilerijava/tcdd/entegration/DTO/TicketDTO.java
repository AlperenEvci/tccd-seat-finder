package com.ilerijava.tcdd.entegration.DTO;

import lombok.Data;

@Data
public class TicketDTO {
    private String name;
    private String cabinName;
    private int availabilityCount;
}