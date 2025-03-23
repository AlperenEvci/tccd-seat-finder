package com.ilerijava.tcdd.entegration.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilerijava.tcdd.entegration.service.SeatService;

import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.TrainSeatsResponseDTO;

import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.ilerijava.tcdd.entegration.util.TrainFunctions;

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

		LocalDateTime dateTime = TrainFunctions.parseDateTime(departureDate);

		return seatService.getSefer(fromStationId, fromStationName, toStationId, toStationName, dateTime);
	}

	@PostMapping("/available-seats-detail/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{departureDate}")
	public TrainSeatsResponseDTO getDetailedAvailableSeats(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable String departureDate) {

		LocalDateTime dateTime = TrainFunctions.parseDateTime(departureDate);

		return seatService.getDetailedAvailableSeats(
				fromStationId,
				fromStationName,
				toStationId,
				toStationName,
				dateTime);
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

		LocalDateTime startDateTime = TrainFunctions.parseDateTime(startDate);
		LocalDateTime endDateTime = TrainFunctions.parseDateTime(endDate);

		return seatService.getAvailableSeatsBetweenDates(
				fromStationId,
				fromStationName,
				toStationId,
				toStationName,
				startDateTime,
				endDateTime,
				seatType);
	}

	@PostMapping("/available-seats/between-dates-paginated/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{startDate}/{endDate}/{seatType}/{page}/{size}")
	public Page<TrainSeatsResponseDTO> getAvailableSeatsBetweenDatesPaginated(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable String startDate,
			@PathVariable String endDate,
			@PathVariable String seatType,
			@PathVariable int page,
			@PathVariable int size) {

		LocalDateTime startDateTime = TrainFunctions.parseDateTime(startDate);
		LocalDateTime endDateTime = TrainFunctions.parseDateTime(endDate);

		return seatService.getAvailableSeatsBetweenDatesPaginated(
				fromStationId,
				fromStationName,
				toStationId,
				toStationName,
				startDateTime,
				endDateTime,
				seatType,
				PageRequest.of(page, size));
	}

}
