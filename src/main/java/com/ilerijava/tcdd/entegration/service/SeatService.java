package com.ilerijava.tcdd.entegration.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.ilerijava.tcdd.entegration.DTO.SeferRequestDto;
import com.ilerijava.tcdd.entegration.DTO.SeferResponseDto;
import com.ilerijava.tcdd.entegration.DTO.TicketDTO;

import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SeatService {

	private final RestClient restClient;

	public SeferResponseDto getSefer(Integer fromStationId, String fromStationName, Integer toStationId,
			String toStationName, LocalDateTime departureDate) {

		SeferRequestDto requestBody = new SeferRequestDto();

		requestBody.setSearchRoutes(
				List.of(new SeferRequestDto.SearchRoutes(fromStationId, fromStationName, toStationId, toStationName,
						departureDate)));
		requestBody.setPassengerTypeCounts(List.of(new SeferRequestDto.PassengerTypeCount(0, 1)));
		requestBody.setSearchReservation(false);
		requestBody.setSearchType("DOMESTIC");

		String url = "https://web-api-prod-ytp.tcddtasimacilik.gov.tr/tms/train/train-availability?environment=dev&userId=1";

		SeferResponseDto seferResponseDto = restClient.post().uri(url)
				.body(requestBody)
				.header("Authorization",
						"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlVFFicDhDMmpiakp1cnUzQVk2a0ZnV196U29MQXZIMmJ5bTJ2OUg5THhRIn0."
								+ "eyJleHAiOjE3MjEzODQ0NzAsImlhdCI6MTcyMTM4NDQxMCwianRpIjoiYWFlNjVkNzgtNmRkZS00ZGY4LWEwZWYtYjRkNzZiYjZlODNjIiwiaXNzIjoiaHR0cDovL"
								+ "3l0cC1wcm9kLW1hc3RlcjEudGNkZHRhc2ltYWNpbGlrLmdvdi50cjo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDAzNDI3MmMtNTc"
								+ "2Yi00OTBlLWJhOTgtNTFkMzc1NWNhYjA3IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidG1zIiwic2Vzc2lvbl9zdGF0ZSI6IjAwYzM4NTJiLTg1YjEtNDMxNS04OGIwLW"
								+ "Q0MWMxMTcyYzA0MSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1tYXN0ZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXR"
								+ "ob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1"
								+ "wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsInNpZCI6IjAwYzM4NTJiLTg1YjEtNDMxNS04OGIwLWQ0MWMxMTcyYzA0MSIsImVtYWlsX3ZlcmlmaW"
								+ "VkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoid2ViIiwiZ2l2ZW5fbmFtZSI6IiIsImZhbWlseV9uYW1lIjoiIn0.AIW_4Qws2wfwxyVg8dgHRT9jB3qNavob2C4mEQIQGl3"
								+ "urzW2jALPx-e51ZwHUb-TXB-X2RPHakonxKnWG6tDIP5aKhiidzXDcr6pDDoYU5DnQhMg1kywyOaMXsjLFjuYN5PAyGUMh6YSOVsg1PzNh-5GrJF44pS47JnB9zk03Pr08napjsZPo"
								+ "RB-5N4GQ49cnx7ePC82Y7YIc-gTew2baqKQPz9_v381Gbm2V38PZDH9KldlcWut7kqQYJFMJ7dkM_entPJn9lFk7R5h5j_06OlQEpWRMQTn9SQ1AYxxmZxBu5XYMKDkn4rzIIVCkdTP"
								+ "JNCt5PvjENjClKFeUA1DOg")
				.header("Unit-Id", "3895").retrieve().body(SeferResponseDto.class);

		return seferResponseDto;
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

}
