package com.ilerijava.tcdd.entegration.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.ilerijava.tcdd.entegration.DTO.SeferRequestDto;
import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto.Train;
import com.ilerijava.tcdd.entegration.DTO.TicketDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainSeatsResponseDTO;
import com.ilerijava.tcdd.entegration.DTO.TrainAvailableSeatsDTO;
import com.ilerijava.tcdd.entegration.DTO.AvailableSeatsDTO;
import com.ilerijava.tcdd.entegration.enums.SeatType;

import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import com.ilerijava.tcdd.entegration.config.RestClientConfig;

@Service
@RequiredArgsConstructor
public class SeatService {

	private final RestClient restClient;
	private final RestClientConfig restClientConfig;

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
			// Önce sefer bilgilerini al
			SeferResponseDto response = getSefer(fromStationId, fromStationName, toStationId, toStationName,
					travelDate);

			// Bilet listesini oluştur
			List<TicketDTO> availableTickets = new ArrayList<>();

			if (response != null && response.getTrainLegs() != null) {
				response.getTrainLegs()
						.forEach(trainLeg -> {
							if (trainLeg.trainAvailabilities() != null) {
								trainLeg.trainAvailabilities()
										.forEach(trainAvailability -> {
											if (trainAvailability.trains() != null) {
												trainAvailability.trains().forEach(train -> {
													if (train.availableFareInfo() != null) {
														train.availableFareInfo().forEach(fareInfo -> {
															if (fareInfo.cabinClasses() != null) {
																fareInfo.cabinClasses().forEach(cabinClass -> {
																	TicketDTO ticketDTO = new TicketDTO();
																	ticketDTO.setName(train.commercialName());
																	ticketDTO.setCabinName(
																			cabinClass.cabinClass().name());
																	ticketDTO.setAvailabilityCount(
																			cabinClass.availabilityCount());
																	availableTickets.add(ticketDTO);
																});
															}
														});
													}
												});
											}
										});
							}
						});
			}

			return availableTickets;
		} catch (Exception e) {
			// Log the exception for debugging
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
		TrainSeatsResponseDTO result = new TrainSeatsResponseDTO();
		List<TrainAvailableSeatsDTO> allTrainsList = new ArrayList<>();

		LocalDateTime currentDate = startDateTime;
		while (!currentDate.isAfter(endDateTime)) {
			SeferResponseDto response = getSefer(fromStationId, fromStationName, toStationId, toStationName,
					currentDate);
			processTrainsForDate(response, fromStationId, startDateTime, endDateTime, seatTypeCode, allTrainsList);
			currentDate = currentDate.plusDays(1);
		}

		result.setTrains(allTrainsList);
		return result;
	}

	/**
	 * Tren bilgilerini işler ve müsait koltukları filtreler
	 */
	private void processTrainsForDate(
			SeferResponseDto response,
			Integer fromStationId,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String seatTypeCode,
			List<TrainAvailableSeatsDTO> allTrainsList) {

		if (response != null && response.getTrainLegs() != null) {
			List<TrainAvailableSeatsDTO> dailyTrains = response.getTrainLegs().stream()
					.flatMap(leg -> leg.trainAvailabilities().stream())
					.flatMap(availability -> availability.trains().stream())
					.filter(train -> isValidTrain(train, fromStationId, startDateTime, endDateTime, seatTypeCode))
					.map(train -> mapToTrainInfo(train, fromStationId, seatTypeCode))
					.collect(Collectors.toList());

			allTrainsList.addAll(dailyTrains);
		}
	}

	/**
	 * Trenin geçerli bir sefer ve müsait koltuk olup olmadığını kontrol eder
	 */
	private boolean isValidTrain(Train train, Integer fromStationId,
			LocalDateTime startDateTime, LocalDateTime endDateTime, String seatTypeCode) {
		return hasValidSegment(train, fromStationId, startDateTime, endDateTime)
				&& hasAvailableSeats(train, seatTypeCode);
	}

	private boolean hasValidSegment(Train train, Integer fromStationId,
			LocalDateTime startDateTime, LocalDateTime endDateTime) {
		return train.trainSegments().stream().anyMatch(segment -> {
			LocalDateTime segmentTime = segment.departureTime();
			return !segmentTime.isBefore(startDateTime) &&
					!segmentTime.isAfter(endDateTime) &&
					segment.departureStationId() == fromStationId;
		});
	}

	private boolean hasAvailableSeats(Train train, String seatTypeCode) {
		return train.availableFareInfo().stream()
				.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
				.anyMatch(cabinClass -> cabinClass.cabinClass().code().equals(seatTypeCode) &&
						cabinClass.availabilityCount() > 0);
	}

	private TrainAvailableSeatsDTO mapToTrainInfo(Train train, Integer fromStationId, String seatTypeCode) {
		TrainAvailableSeatsDTO trainInfo = new TrainAvailableSeatsDTO();
		trainInfo.setTrainName(train.commercialName());
		setDepartureTime(train, fromStationId, trainInfo);
		trainInfo.setSeatInfo(createSeatInfo(train, seatTypeCode));
		return trainInfo;
	}

	private void setDepartureTime(Train train, Integer fromStationId, TrainAvailableSeatsDTO trainInfo) {
		train.trainSegments().stream()
				.filter(segment -> segment.departureStationId() == fromStationId)
				.findFirst()
				.ifPresent(segment -> {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
					trainInfo.setDepartureTime(segment.departureTime().format(formatter));
				});
	}

	private AvailableSeatsDTO createSeatInfo(Train train, String seatTypeCode) {
		AvailableSeatsDTO seatsInfo = new AvailableSeatsDTO();
		train.availableFareInfo().stream()
				.flatMap(fareInfo -> fareInfo.cabinClasses().stream())
				.filter(cabinClass -> cabinClass.cabinClass().code().equals(seatTypeCode))
				.forEach(cabinClass -> setSeatCount(seatsInfo, seatTypeCode, cabinClass.availabilityCount()));
		return seatsInfo;
	}

	/**
	 * Belirtilen koltuk tipine göre müsait koltuk sayısını ayarlar
	 */
	private void setSeatCount(AvailableSeatsDTO seatsInfo, String seatTypeCode, int availableCount) {
		seatsInfo.setTotalSeats(availableCount);
	}
}
