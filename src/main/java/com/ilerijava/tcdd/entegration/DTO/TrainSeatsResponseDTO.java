package com.ilerijava.tcdd.entegration.DTO;

import lombok.Data;
import java.util.List;

@Data
public class TrainSeatsResponseDTO {
    private List<TrainAvailableSeatsDTO> trains;
}