package com.ilerijava.tcdd.entegration.controller;

import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilerijava.tcdd.entegration.service.SeatService;

import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.TicketDTO;

import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@RestController
public class SeatController {

	private final SeatService seatService;

	@PostMapping("/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{travelDate}")
	public SeferResponseDto getSefer(@PathVariable Integer fromStationId, @PathVariable String fromStationName,
			@PathVariable Integer toStationId, @PathVariable String toStationName,
			@PathVariable LocalDateTime travelDate) {
		return seatService.getSefer(fromStationId, fromStationName, toStationId, toStationName, travelDate);
	}

	@PostMapping("/tickets/{fromStationId}/{fromStationName}/{toStationId}/{toStationName}/{travelDate}")
	public List<TicketDTO> getAvailableTickets(
			@PathVariable Integer fromStationId,
			@PathVariable String fromStationName,
			@PathVariable Integer toStationId,
			@PathVariable String toStationName,
			@PathVariable LocalDateTime travelDate) {

		return seatService.getAvailableTickets(fromStationId, fromStationName, toStationId, toStationName, travelDate);

	}
}
