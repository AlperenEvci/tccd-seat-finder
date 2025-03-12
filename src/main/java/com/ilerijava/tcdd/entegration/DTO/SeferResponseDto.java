
package com.ilerijava.tcdd.entegration.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class SeferResponseDto {

	private List<TrainLeg> trainLegs;
	private int legCount;
	private int roundTripDiscount;
	private long maxRegionalTrainsRoundTripDays;

	public record TrainLeg(List<TrainAvailability> trainAvailabilities, int resultCount) {
	}

	public record TrainAvailability(List<Train> trains, int totalTripTime, double minPrice, boolean connection,
			boolean dayChanged) {
	}

	public record Train(int id, String number, String name, String commercialName, String type, String line,
			boolean reversed, int scheduleId, int departureStationId, int arrivalStationId,
			List<AvailableFareInfo> availableFareInfo, List<TrainSegment> trainSegments) {
	}

	public record AvailableFareInfo(List<CabinClass> cabinClasses) {
	}

	public record TrainSegment(int departureStationId, int arrivalStationId, LocalDateTime departureTime,
			LocalDateTime arrivalTime) {
	}

	public record CabinClass(CabinClassDetail cabinClass, int availabilityCount) {
	}

	public record CabinClassDetail(int id, String name, String code) {
	}
}
