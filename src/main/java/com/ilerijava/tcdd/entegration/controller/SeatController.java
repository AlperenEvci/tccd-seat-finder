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
import java.util.stream.Collectors;

import com.ilerijava.tcdd.entegration.enums.SeatType;

@Data
@RestController
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

		String seatTypeCode = SeatType.getCodeFromType(seatType);

		LocalDateTime startDateTime = LocalDateTime.parse(startDate.replace(" ", "T"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));
		LocalDateTime endDateTime = LocalDateTime.parse(endDate.replace(" ", "T"),
				DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss"));

		TrainSeatsResponseDTO result = new TrainSeatsResponseDTO();
		List<TrainAvailableSeatsDTO> allTrainsList = new ArrayList<>();

		// Başlangıç tarihinden bitiş tarihine kadar her gün için döngü
		LocalDateTime currentDate = startDateTime;
		while (!currentDate.isAfter(endDateTime)) {
			SeferResponseDto response = seatService.getSefer(fromStationId, fromStationName, toStationId, toStationName,
					currentDate);

			if (response != null && response.getTrainLegs() != null) {
				List<TrainAvailableSeatsDTO> dailyTrains = response.getTrainLegs().stream()
						.flatMap(leg -> leg.trainAvailabilities().stream())
						.flatMap(availability -> availability.trains().stream())
						.filter(train -> train.trainSegments().stream().anyMatch(segment -> {
							LocalDateTime segmentTime = segment.departureTime();
							return !segmentTime.isBefore(startDateTime) &&
									!segmentTime.isAfter(endDateTime) &&
									segment.departureStationId() == fromStationId;
						}))
						.filter(train -> train.availableFareInfo().stream()
								.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
								.anyMatch(cabinClass -> cabinClass.cabinClass().code().equals(seatTypeCode) &&
										cabinClass.availabilityCount() > 0))
						.map(train -> {
							TrainAvailableSeatsDTO trainInfo = new TrainAvailableSeatsDTO();
							trainInfo.setTrainName(train.commercialName());

							train.trainSegments().stream()
									.filter(segment -> segment.departureStationId() == fromStationId)
									.findFirst()
									.ifPresent(segment -> {
										DateTimeFormatter formatter = DateTimeFormatter
												.ofPattern("dd-MM-yyyy HH:mm:ss");
										trainInfo.setDepartureTime(segment.departureTime().format(formatter));
									});

							AvailableSeatsDTO seatsInfo = new AvailableSeatsDTO();
							train.availableFareInfo().stream()
									.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
									.filter(cabinClass -> cabinClass.cabinClass().code().equals(seatTypeCode))
									.forEach(cabinClass -> {
										int availableCount = cabinClass.availabilityCount();
										switch (seatTypeCode) {
											case "Y1":
												seatsInfo.setEkonomiSeats(availableCount);
												seatsInfo.setTotalSeats(availableCount);
												break;
											case "B":
												seatsInfo.setYatakliSeats(availableCount);
												seatsInfo.setTotalSeats(availableCount);
												break;
											case "DSB":
												seatsInfo.setWheelchairSeats(availableCount);
												seatsInfo.setTotalSeats(availableCount);
												break;
										}
									});

							trainInfo.setSeatInfo(seatsInfo);
							return trainInfo;
						})
						.collect(Collectors.toList());

				allTrainsList.addAll(dailyTrains);
			}

			// Bir sonraki güne geç
			currentDate = currentDate.plusDays(1);
		}

		result.setTrains(allTrainsList);
		return result;
	}

}
