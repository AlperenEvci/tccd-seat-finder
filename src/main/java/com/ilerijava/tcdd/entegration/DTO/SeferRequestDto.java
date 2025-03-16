package com.ilerijava.tcdd.entegration.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;
import java.time.LocalDateTime;
import lombok.Data;

@Data

public class SeferRequestDto {

	private String searchType = "DOMESTIC";

	private boolean searchReservation = false;

	List<PassengerTypeCount> passengerTypeCounts;

	List<SearchRoutes> searchRoutes;

	public static record PassengerTypeCount(int id, int count) {
	}

	public static record SearchRoutes(int departureStationId, String departureStationName, int arrivalStationId,
			String arrivalStationName,
			@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime departureDate) {
	}
}
