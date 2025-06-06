package com.bookmymovie.service;


import com.bookmymovie.constants.TheaterConstant;
import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.ScreenResponseDto;
import com.bookmymovie.dto.response.SeatMapResponseDto;
import com.bookmymovie.dto.response.TheaterResponseDto;
import com.bookmymovie.entity.Screen;
import com.bookmymovie.entity.Seat;
import com.bookmymovie.entity.Theater;
import com.bookmymovie.exception.*;
import com.bookmymovie.repository.ScreenRepository;
import com.bookmymovie.repository.SeatRepository;
import com.bookmymovie.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    // ==================== THEATER MANAGEMENT ====================

    @Transactional
    public TheaterResponseDto createTheater(TheaterCreateRequestDto request) {
        log.info("Creating theater: {}", request.getName());

        // Check for duplicate theater in same city
        if (theaterRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndDeletedFalse(
                request.getName(), request.getCity())) {
            throw new TheaterAlreadyExistsException(
                    "Theater with name '" + request.getName() + "' already exists in " + request.getCity());
        }

        // Build theater entity
        Theater theater = Theater.builder()
                .name(request.getName().trim())
                .address(request.getAddress().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .theaterType(request.getTheaterType())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .facilities(request.getFacilities() != null ? request.getFacilities() : new ArrayList<>())
                .build();

        // Validate business rules
        validateTheaterBusinessRules(theater);

        Theater savedTheater = theaterRepository.save(theater);
        log.info("Theater created successfully with ID: {}", savedTheater.getTheaterId());

        return mapToTheaterResponseDto(savedTheater);
    }

    public Page<TheaterResponseDto> searchTheaters(TheaterSearchRequestDto request) {
        log.info("Searching theaters with criteria: {}", request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.Direction.fromString(request.getSortDirection()),
                request.getSortBy()
        );

        Page<Theater> theaters;

        // Geographic search takes priority
        if (request.getLatitude() != null && request.getLongitude() != null) {
            return searchTheatersByLocation(request, pageable);
        }

        // City-based search
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            theaters = theaterRepository.findByCityIgnoreCaseAndStatusAndDeletedFalse(
                    request.getCity().trim(), request.getStatus(), pageable);
        }
        // Name-based search
        else if (request.getName() != null && !request.getName().trim().isEmpty()) {
            theaters = theaterRepository.findByNameContainingIgnoreCaseAndDeletedFalse(
                    request.getName().trim(), pageable);
        }
        // All active theaters
        else {
            theaters = theaterRepository.findByStatusAndDeletedFalse(request.getStatus(), pageable);
        }

        return theaters.map(this::mapToTheaterResponseDto);
    }

    private Page<TheaterResponseDto> searchTheatersByLocation(TheaterSearchRequestDto request, Pageable pageable) {
        log.info("Performing geographic search around lat: {}, lng: {}, radius: {}km",
                request.getLatitude(), request.getLongitude(), request.getRadiusKm());

        // Validate radius
        int radius = Math.min(request.getRadiusKm(), TheaterConstant.MAX_SEARCH_RADIUS_KM);

        List<Object[]> results = theaterRepository.findTheatersWithinRadius(
                request.getLatitude(), request.getLongitude(), radius);

        List<TheaterResponseDto> theaterDtos = results.stream()
                .map(result -> {
                    Theater theater = (Theater) result[0];
                    Double distance = (Double) result[1];
                    TheaterResponseDto dto = mapToTheaterResponseDto(theater);
                    dto.setDistanceKm(distance);
                    return dto;
                })
                .collect(Collectors.toList());

        // Apply additional filters if provided
        if (request.getTheaterType() != null) {
            theaterDtos = theaterDtos.stream()
                    .filter(dto -> dto.getTheaterType() == request.getTheaterType())
                    .collect(Collectors.toList());
        }

        // Manual pagination for geographic results
        int start = request.getPage() * request.getSize();
        int end = Math.min(start + request.getSize(), theaterDtos.size());
        List<TheaterResponseDto> pageContent = theaterDtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, theaterDtos.size());
    }

    public TheaterResponseDto getTheaterById(Long theaterId) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterNotFoundException("Theater not found with ID: " + theaterId));

        return mapToTheaterResponseDto(theater);
    }

    @Transactional
    public TheaterResponseDto updateTheater(Long theaterId, TheaterUpdateRequestDto request) {
        log.info("Updating theater with ID: {}", theaterId);

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterNotFoundException("Theater not found with ID: " + theaterId));

        // Update fields if provided
        if (request.getName() != null) {
            theater.setName(request.getName().trim());
        }
        if (request.getAddress() != null) {
            theater.setAddress(request.getAddress().trim());
        }
        if (request.getPhoneNumber() != null) {
            theater.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            theater.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            theater.setStatus(request.getStatus());
        }
        if (request.getOpeningTime() != null) {
            theater.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            theater.setClosingTime(request.getClosingTime());
        }
        if (request.getFacilities() != null) {
            theater.setFacilities(request.getFacilities());
        }

        // Validate business rules
        validateTheaterBusinessRules(theater);

        Theater updatedTheater = theaterRepository.save(theater);
        log.info("Theater updated successfully: {}", theaterId);

        return mapToTheaterResponseDto(updatedTheater);
    }

    @Transactional
    public void deleteTheater(Long theaterId) {
        log.info("Deleting theater with ID: {}", theaterId);

        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterNotFoundException("Theater not found with ID: " + theaterId));

        // Business rule: Cannot delete theater with active shows
        // This would require checking show repository (implement later)

        theaterRepository.delete(theater); // Soft delete due to @SQLDelete
        log.info("Theater deleted successfully: {}", theaterId);
    }

    // ==================== SCREEN MANAGEMENT ====================

    @Transactional
    public ScreenResponseDto createScreen(ScreenCreateRequestDto request) {
        log.info("Creating screen: {} for theater ID: {}", request.getName(), request.getTheaterId());

        // Validate theater exists
        Theater theater = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new TheaterNotFoundException("Theater not found with ID: " + request.getTheaterId()));

        // Check for duplicate screen name in theater
        if (screenRepository.existsByTheaterTheaterIdAndNameIgnoreCaseAndDeletedFalse(
                request.getTheaterId(), request.getName())) {
            throw new ScreenAlreadyExistsException(
                    "Screen with name '" + request.getName() + "' already exists in this theater");
        }

        // Build screen entity
        Screen screen = Screen.builder()
                .name(request.getName().trim())
                .theater(theater)
                .screenType(request.getScreenType())
                .soundSystem(request.getSoundSystem())
                .totalRows(request.getTotalRows())
                .totalSeats(request.getTotalSeats())
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .build();

        // Validate business rules
        validateScreenBusinessRules(screen);

        Screen savedScreen = screenRepository.save(screen);
        log.info("Screen created successfully with ID: {}", savedScreen.getScreenId());

        return mapToScreenResponseDto(savedScreen);
    }

    public List<ScreenResponseDto> getScreensByTheater(Long theaterId) {
        List<Screen> screens = screenRepository.findByTheaterTheaterIdAndDeletedFalse(theaterId);
        return screens.stream()
                .map(this::mapToScreenResponseDto)
                .collect(Collectors.toList());
    }

    public ScreenResponseDto getScreenById(Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with ID: " + screenId));

        return mapToScreenResponseDto(screen);
    }

    // ==================== SEAT LAYOUT MANAGEMENT ====================

    @Transactional
    public SeatMapResponseDto createSeatLayout(SeatLayoutRequestDto request) {
        log.info("Creating seat layout for screen ID: {}", request.getScreenId());

        // Validate screen exists
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with ID: " + request.getScreenId()));

        // Check if seats already exist
        List<Seat> existingSeats = seatRepository.findByScreenScreenIdAndDeletedFalse(request.getScreenId());
        if (!existingSeats.isEmpty()) {
            throw new SeatLayoutAlreadyExistsException("Seat layout already exists for this screen");
        }

        List<Seat> seats = new ArrayList<>();
        int totalSeatsCreated = 0;

        // Create seats based on configuration
        for (SeatLayoutRequestDto.SeatConfigurationDto config : request.getSeatConfigurations()) {
            for (int seatNum = 1; seatNum <= config.getSeatsInRow(); seatNum++) {
                Seat seat = Seat.builder()
                        .screen(screen)
                        .rowLabel(config.getRowLabel().toUpperCase())
                        .rowNumber(config.getRowNumber())
                        .seatNumber(seatNum)
                        .category(config.getCategory())
                        .basePrice(config.getBasePrice())
                        .seatType(config.getSeatType())
                        .isWheelchairAccessible(config.getIsWheelchairAccessible())
                        .isCoupleSeAT(config.getIsCoupleSeAT())
                        .isPremium(config.getIsPremium())
                        .features(config.getFeatures() != null ? config.getFeatures() : new ArrayList<>())
                        .build();

                // Validate seat configuration
                seat.validateSeatConfiguration();

                seats.add(seat);
                totalSeatsCreated++;
            }
        }

        // Validate total seat count matches screen capacity
        if (totalSeatsCreated != screen.getTotalSeats()) {
            throw new SeatLayoutValidationException(
                    "Total seats created (" + totalSeatsCreated +
                            ") doesn't match screen capacity (" + screen.getTotalSeats() + ")");
        }

        // Save all seats
        List<Seat> savedSeats = seatRepository.saveAll(seats);
        log.info("Created {} seats for screen ID: {}", savedSeats.size(), request.getScreenId());

        return mapToSeatMapResponseDto(screen, savedSeats);
    }

    public SeatMapResponseDto getSeatMap(Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with ID: " + screenId));

        List<Seat> seats = seatRepository.findSeatMapByScreen(screenId);
        return mapToSeatMapResponseDto(screen, seats);
    }

    @Transactional
    public void updateSeatStatus(SeatStatusUpdateRequestDto request) {
        log.info("Updating status for {} seats to: {}", request.getSeatIds().size(), request.getStatus());

        List<Seat> seats = seatRepository.findBySeatIds(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new SeatNotFoundException("Some seats not found");
        }

        // Update status for all seats
        seats.forEach(seat -> seat.setStatus(request.getStatus()));
        seatRepository.saveAll(seats);

        log.info("Updated status for {} seats", seats.size());
    }

    // ==================== VALIDATION METHODS ====================

    private void validateTheaterBusinessRules(Theater theater) {
        // Validate operating hours
        if (theater.getOpeningTime() != null && theater.getClosingTime() != null) {
            if (theater.getOpeningTime().isAfter(theater.getClosingTime())) {
                throw new TheaterValidationException("Opening time cannot be after closing time");
            }
        }

        // Validate geographic coordinates
        if (theater.getLatitude().abs().compareTo(new BigDecimal("90")) > 0) {
            throw new TheaterValidationException("Invalid latitude value");
        }
        if (theater.getLongitude().abs().compareTo(new BigDecimal("180")) > 0) {
            throw new TheaterValidationException("Invalid longitude value");
        }
    }

    private void validateScreenBusinessRules(Screen screen) {
        // Validate capacity
        if (screen.getTotalSeats() <= 0) {
            throw new ScreenValidationException("Total seats must be positive");
        }
        if (screen.getTotalRows() <= 0) {
            throw new ScreenValidationException("Total rows must be positive");
        }

        // Business rule: Average seats per row should be reasonable
        double avgSeatsPerRow = (double) screen.getTotalSeats() / screen.getTotalRows();
        if (avgSeatsPerRow < 5 || avgSeatsPerRow > 50) {
            throw new ScreenValidationException("Average seats per row should be between 5 and 50");
        }
    }

    // ==================== MAPPING METHODS ====================

    private TheaterResponseDto mapToTheaterResponseDto(Theater theater) {
        List<TheaterResponseDto.ScreenSummaryDto> screenSummaries = theater.getScreens().stream()
                .map(screen -> TheaterResponseDto.ScreenSummaryDto.builder()
                        .screenId(screen.getScreenId())
                        .name(screen.getName())
                        .screenType(screen.getScreenType())
                        .soundSystem(screen.getSoundSystem())
                        .totalSeats(screen.getTotalSeats())
                        .status(screen.getStatus())
                        .build())
                .collect(Collectors.toList());

        return TheaterResponseDto.builder()
                .theaterId(theater.getTheaterId())
                .name(theater.getName())
                .address(theater.getAddress())
                .city(theater.getCity())
                .state(theater.getState())
                .pincode(theater.getPincode())
                .latitude(theater.getLatitude())
                .longitude(theater.getLongitude())
                .phoneNumber(theater.getPhoneNumber())
                .email(theater.getEmail())
                .theaterType(theater.getTheaterType())
                .status(theater.getStatus())
                .openingTime(theater.getOpeningTime())
                .closingTime(theater.getClosingTime())
                .facilities(theater.getFacilities())
                .totalScreens(theater.getTotalScreens())
                .totalSeats(theater.getTotalSeats())
                .screens(screenSummaries)
                .build();
    }

    private ScreenResponseDto mapToScreenResponseDto(Screen screen) {
        return ScreenResponseDto.builder()
                .screenId(screen.getScreenId())
                .name(screen.getName())
                .theaterId(screen.getTheater().getTheaterId())
                .theaterName(screen.getTheater().getName())
                .screenType(screen.getScreenType())
                .soundSystem(screen.getSoundSystem())
                .status(screen.getStatus())
                .totalRows(screen.getTotalRows())
                .totalSeats(screen.getTotalSeats())
                .features(screen.getFeatures())
                .availableSeats(screen.getAvailableSeats())
                .build();
    }

    private SeatMapResponseDto mapToSeatMapResponseDto(Screen screen, List<Seat> seats) {
        // Group seats by row
        Map<Integer, List<Seat>> seatsByRow = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowNumber));

        List<SeatMapResponseDto.RowDto> rows = seatsByRow.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<SeatMapResponseDto.RowDto.SeatDto> seatDtos = entry.getValue().stream()
                            .sorted((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()))
                            .map(seat -> SeatMapResponseDto.RowDto.SeatDto.builder()
                                    .seatId(seat.getSeatId())
                                    .seatNumber(seat.getSeatNumber())
                                    .seatIdentifier(seat.getSeatIdentifier())
                                    .category(seat.getCategory())
                                    .status(seat.getStatus())
                                    .price(seat.getEffectivePrice())
                                    .isAvailable(seat.isAvailable())
                                    .isCoupleSeAT(seat.getIsCoupleSeAT())
                                    .isPremium(seat.getIsPremium())
                                    .build())
                            .collect(Collectors.toList());

                    return SeatMapResponseDto.RowDto.builder()
                            .rowLabel(entry.getValue().get(0).getRowLabel())
                            .rowNumber(entry.getKey())
                            .seats(seatDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return SeatMapResponseDto.builder()
                .screenId(screen.getScreenId())
                .screenName(screen.getName())
                .totalRows(screen.getTotalRows())
                .totalSeats(screen.getTotalSeats())
                .rows(rows)
                .build();
    }

    public boolean isTheaterNameTaken(String name, String city) {
        return theaterRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndDeletedFalse(name, city);
    }

}