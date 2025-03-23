package com.ilerijava.tcdd.entegration.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.ilerijava.tcdd.entegration.DTO.SeferRequestDto;
import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.TicketDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainSeatsResponseDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainAvailableSeatsDTO;
import com.ilerijava.tcdd.entegration.enums.SeatType;

import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import com.ilerijava.tcdd.entegration.config.RestClientConfig;
import com.ilerijava.tcdd.entegration.util.TrainFunctions;

@Service
@RequiredArgsConstructor
public class SeatService {

	private final RestClient restClient;
	private final RestClientConfig restClientConfig;
	private final EmailService emailService;

	/**
	 * Belirtilen istasyonlar arasındaki tren seferlerini getirir
	 */
	public SeferResponseDto getSefer(Integer fromStationId, String fromStationName, Integer toStationId,
			String toStationName, LocalDateTime departureDate) {

		SeferRequestDto requestBody = new SeferRequestDto();
		requestBody.setSearchRoutes(
				List.of(new SeferRequestDto.SearchRoutes(fromStationId, fromStationName, toStationId, toStationName,
						departureDate)));
		requestBody.setPassengerTypeCounts(List.of(new SeferRequestDto.PassengerTypeCount(0, 1)));
		requestBody.setSearchReservation(false);
		requestBody.setSearchType("DOMESTIC");

		return restClient.post()
				.uri(restClientConfig.getTrainAvailabilityUrl())
				.body(requestBody)
				.retrieve()
				.body(SeferResponseDto.class);
	}

	public List<TicketDTO> getAvailableTickets(Integer fromStationId, String fromStationName, Integer toStationId,
			String toStationName, LocalDateTime travelDate) {

		try {
			SeferResponseDto response = getSefer(fromStationId, fromStationName, toStationId, toStationName,
					travelDate);
			List<TicketDTO> availableTickets = new ArrayList<>();

			if (response != null && response.getTrainLegs() != null) {
				response.getTrainLegs().stream()
						.flatMap(leg -> leg.trainAvailabilities().stream())
						.flatMap(availability -> availability.trains().stream())
						.forEach(train -> {
							train.availableFareInfo().stream()
									.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
									.forEach(cabinClass -> {
										TicketDTO ticketDTO = new TicketDTO();
										ticketDTO.setName(train.commercialName());
										ticketDTO.setCabinName(cabinClass.cabinClass().name());
										ticketDTO.setAvailabilityCount(cabinClass.availabilityCount());
										availableTickets.add(ticketDTO);
									});
						});
			}
			return availableTickets;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error processing tickets: " + e.getMessage(), e);
		}
	}

	/**
	 * Belirli tarih aralığındaki müsait koltukları listeler
	 */
	public TrainSeatsResponseDTO getAvailableSeatsBetweenDates(
			Integer fromStationId,
			String fromStationName,
			Integer toStationId,
			String toStationName,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String seatType) {

		String seatTypeCode = SeatType.getCodeFromType(seatType);
		List<TrainAvailableSeatsDTO> allTrainsList = collectTrainsForDateRange(
				fromStationId, fromStationName, toStationId, toStationName,
				startDateTime, endDateTime, seatTypeCode);

		TrainSeatsResponseDTO result = new TrainSeatsResponseDTO();
		result.setTrains(allTrainsList);

		if (allTrainsList.stream().anyMatch(train -> train.getSeatInfo().getTotalSeats() > 0)) {
			emailService.sendAvailableSeatsEmail(allTrainsList, fromStationName, toStationName);
		}

		return result;
	}

	/**
	 * Tren bilgilerini işler ve müsait koltukları filtreler
	 */
	private List<TrainAvailableSeatsDTO> collectTrainsForDateRange(
			Integer fromStationId,
			String fromStationName,
			Integer toStationId,
			String toStationName,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String seatTypeCode) {

		List<TrainAvailableSeatsDTO> allTrains = new ArrayList<>();
		LocalDateTime currentDate = startDateTime;

		while (!currentDate.isAfter(endDateTime)) {
			SeferResponseDto response = getSefer(fromStationId, fromStationName, toStationId, toStationName,
					currentDate);
			allTrains.addAll(processTrainsFromResponse(response, fromStationId, seatTypeCode));
			currentDate = currentDate.plusDays(1);
		}

		return allTrains;
	}

	private List<TrainAvailableSeatsDTO> processTrainsFromResponse(
			SeferResponseDto response,
			Integer fromStationId,
			String seatTypeCode) {

		if (response == null || response.getTrainLegs() == null) {
			return new ArrayList<>();
		}

		return response.getTrainLegs().stream()
				.flatMap(leg -> leg.trainAvailabilities().stream())
				.flatMap(availability -> availability.trains().stream())
				.map(train -> {
					TrainAvailableSeatsDTO trainInfo = new TrainAvailableSeatsDTO();
					trainInfo.setTrainName(train.commercialName());

					// TrainFunctions kullanarak kalkış zamanını ayarla
					train.trainSegments().stream()
							.filter(segment -> segment.departureStationId() == fromStationId)
							.findFirst()
							.ifPresent(segment -> trainInfo.setDepartureTime(
									TrainFunctions.formatDepartureTime(segment.departureTime())));

					// TrainFunctions kullanarak koltuk bilgilerini ayarla
					trainInfo.setSeatInfo(TrainFunctions.createSeatInfo(train, seatTypeCode));
					return trainInfo;
				})
				.collect(Collectors.toList());
	}

	public Page<TrainSeatsResponseDTO> getAvailableSeatsBetweenDatesPaginated(
			Integer fromStationId,
			String fromStationName,
			Integer toStationId,
			String toStationName,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String seatType,
			Pageable pageable) {

		String seatTypeCode = SeatType.getCodeFromType(seatType);
		List<TrainAvailableSeatsDTO> allTrains = collectTrainsForDateRange(
				fromStationId, fromStationName, toStationId, toStationName,
				startDateTime, endDateTime, seatTypeCode);

		return createPaginatedResponse(allTrains, pageable);
	}

	private Page<TrainSeatsResponseDTO> createPaginatedResponse(
			List<TrainAvailableSeatsDTO> allTrains,
			Pageable pageable) {

		int start = (int) pageable.getOffset();
		start = Math.min(start, allTrains.size());
		int end = Math.min((start + pageable.getPageSize()), allTrains.size());

		List<TrainSeatsResponseDTO> pageContent = new ArrayList<>();
		if (!allTrains.isEmpty()) {
			List<TrainAvailableSeatsDTO> paginatedTrains = allTrains.subList(start, end);
			TrainSeatsResponseDTO responseDTO = new TrainSeatsResponseDTO();
			responseDTO.setTrains(paginatedTrains);
			pageContent.add(responseDTO);
		}

		return new PageImpl<>(pageContent, pageable, allTrains.size());
	}

	public TrainSeatsResponseDTO getDetailedAvailableSeats(
			Integer fromStationId,
			String fromStationName,
			Integer toStationId,
			String toStationName,
			LocalDateTime dateTime) {

		SeferResponseDto response = getSefer(fromStationId, fromStationName, toStationId, toStationName, dateTime);
		List<TrainAvailableSeatsDTO> trainsList = processTrainsFromResponse(response, fromStationId, "ALL");

		TrainSeatsResponseDTO result = new TrainSeatsResponseDTO();
		result.setTrains(trainsList);
		return result;
	}
}
