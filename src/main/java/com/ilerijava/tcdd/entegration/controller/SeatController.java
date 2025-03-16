package com.ilerijava.tcdd.entegration.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilerijava.tcdd.entegration.service.SeatService;

import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.AvailableSeatsDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainAvailableSeatsDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainSeatsResponseDTO;

import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;

@Data
@RestController
@RequiredArgsConstructor
public class SeatController {

	private final SeatService seatService;

	@PostMapping("/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{departureDate}")
	public SeferResponseDto getSefer(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable String departureDate) {

		// String formatındaki tarihi LocalDateTime'a çevirme
		LocalDateTime dateTime = LocalDateTime.parse(departureDate.replace(" ", "T"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));

		return seatService.getSefer(fromStationId, fromStationName, toStationId, toStationName, dateTime);
	}

	@PostMapping("/available-seats-detail/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{departureDate}")
	public TrainSeatsResponseDTO getDetailedAvailableSeats(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable String departureDate) {

		LocalDateTime dateTime = LocalDateTime.parse(departureDate.replace(" ", "T"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));

		SeferResponseDto response = seatService.getSefer(fromStationId, fromStationName, toStationId, toStationName,
				dateTime);
		TrainSeatsResponseDTO result = new TrainSeatsResponseDTO();
		List<TrainAvailableSeatsDTO> trainsList = new ArrayList<>();

		if (response != null && response.getTrainLegs() != null) {
			response.getTrainLegs().stream()
					.flatMap(leg -> leg.trainAvailabilities().stream())
					.flatMap(availability -> availability.trains().stream())
					.forEach(train -> {
						TrainAvailableSeatsDTO trainInfo = new TrainAvailableSeatsDTO();
						trainInfo.setTrainName(train.commercialName());

						// Tarihi istenen formatta set et
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
						trainInfo.setDepartureTime(train.trainSegments().get(0).departureTime().format(formatter));

						AvailableSeatsDTO seatsInfo = new AvailableSeatsDTO();

						train.availableFareInfo().stream()
								.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
								.forEach(cabinClass -> {
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
								});

						seatsInfo.setTotalSeats(seatsInfo.getEkonomiSeats() +
								seatsInfo.getYatakliSeats() +
								seatsInfo.getWheelchairSeats());

						trainInfo.setSeatInfo(seatsInfo);
						trainsList.add(trainInfo);
					});
		}

		result.setTrains(trainsList);
		return result;
	}

	@PostMapping("/available-seats/between-dates/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{startDate}/{endDate}/{seatType}")
	public TrainSeatsResponseDTO getAvailableSeatsBetweenDates(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable String startDate,
			@PathVariable String endDate,
			@PathVariable String seatType) {

		LocalDateTime startDateTime = parseDateTime(startDate);
		LocalDateTime endDateTime = parseDateTime(endDate);

		return seatService.getAvailableSeatsBetweenDates(
				fromStationId,
				fromStationName,
				toStationId,
				toStationName,
				startDateTime,
				endDateTime,
				seatType);
	}

	private LocalDateTime parseDateTime(String dateStr) {
		return LocalDateTime.parse(dateStr.replace(" ", "T"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));
	}

}
