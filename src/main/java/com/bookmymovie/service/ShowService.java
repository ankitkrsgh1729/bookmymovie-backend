package com.bookmymovie.service;

import com.bookmymovie.dto.request.*;
import com.bookmymovie.dto.response.*;
import com.bookmymovie.entity.Movie;
import com.bookmymovie.entity.Screen;
import com.bookmymovie.entity.Show;
import com.bookmymovie.entity.Theater;
import com.bookmymovie.exception.BaseException;
import com.bookmymovie.repository.MovieRepository;
import com.bookmymovie.repository.ScreenRepository;
import com.bookmymovie.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    // ==================== CREATE OPERATIONS ====================

    public ShowResponseDto createShow(ShowCreateRequestDto request) {
        log.info("Creating show for movie {} at screen {} on {} at {}",
                request.getMovieId(), request.getScreenId(), request.getShowDate(), request.getShowTime());

        // Validate entities exist
        Movie movie = findMovieById(request.getMovieId());
        Screen screen = findScreenById(request.getScreenId());

        // Check for conflicts
        validateShowScheduling(request, null);

        // Create show entity
        Show show = buildShowFromRequest(request, movie, screen);
        show = showRepository.save(show);

        log.info("Successfully created show with ID: {}", show.getShowId());
        return mapToShowResponseDto(show);
    }

    public BulkShowResponseDto createBulkShows(BulkShowCreateRequestDto request) {
        log.info("Creating bulk shows for movie {} at screen {} from {} to {}",
                request.getMovieId(), request.getScreenId(), request.getStartDate(), request.getEndDate());

        Movie movie = findMovieById(request.getMovieId());
        Screen screen = findScreenById(request.getScreenId());

        BulkShowResponseDto.BulkShowResponseDtoBuilder responseBuilder = BulkShowResponseDto.builder()
                .totalRequested(0)
                .successfullyCreated(0)
                .failed(0)
                .createdShowIds(new java.util.ArrayList<>())
                .errors(new java.util.ArrayList<>())
                .conflicts(new java.util.ArrayList<>());

        LocalDate currentDate = request.getStartDate();

        while (!currentDate.isAfter(request.getEndDate())) {
            // Skip weekends if requested
            if (request.getSkipWeekends() && isWeekend(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            // Skip excluded dates
            if (request.getExcludeDates() != null && request.getExcludeDates().contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            for (LocalTime showTime : request.getShowTimes()) {
                responseBuilder.totalRequested(responseBuilder.build().getTotalRequested() + 1);

                try {
                    ShowCreateRequestDto showRequest = ShowCreateRequestDto.builder()
                            .movieId(request.getMovieId())
                            .screenId(request.getScreenId())
                            .showDate(currentDate)
                            .showTime(showTime)
                            .basePrice(request.getBasePrice())
                            .weekendMultiplier(request.getWeekendMultiplier())
                            .primeTimeMultiplier(request.getPrimeTimeMultiplier())
                            .holidayMultiplier(request.getHolidayMultiplier())
                            .hasIntermission(request.getHasIntermission())
                            .intermissionDurationMinutes(request.getIntermissionDurationMinutes())
                            .cleaningTimeMinutes(request.getCleaningTimeMinutes())
                            .isSpecialScreening(request.getIsSpecialScreening())
                            .specialNotes(request.getSpecialNotes())
                            .build();

                    validateShowScheduling(showRequest, null);
                    Show show = buildShowFromRequest(showRequest, movie, screen);
                    show = showRepository.save(show);

                    responseBuilder.successfullyCreated(responseBuilder.build().getSuccessfullyCreated() + 1);
                    responseBuilder.build().getCreatedShowIds().add(show.getShowId());

                } catch (Exception e) {
                    responseBuilder.failed(responseBuilder.build().getFailed() + 1);
                    responseBuilder.build().getErrors().add(
                            String.format("Failed to create show on %s at %s: %s", currentDate, showTime, e.getMessage()));
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        BulkShowResponseDto response = responseBuilder.build();
        log.info("Bulk show creation completed: {} created, {} failed out of {} requested",
                response.getSuccessfullyCreated(), response.getFailed(), response.getTotalRequested());

        return response;
    }

    // ==================== READ OPERATIONS ====================

    @Transactional(readOnly = true)
    public ShowResponseDto getShowById(Long showId) {
        Show show = findShowById(showId);
        return mapToShowResponseDto(show);
    }

    @Transactional(readOnly = true)
    public List<ShowSummaryDto> getShowsByMovie(Long movieId, LocalDate showDate) {
        List<Show> shows = showRepository.findByMovieMovieIdAndShowDateOrderByShowTime(movieId, showDate);
        return shows.stream()
                .map(this::mapToShowSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShowListingDto getMovieShowListing(Long movieId, String city, LocalDate showDate) {
        List<Show> shows = showRepository.findShowsByMovieAndCityAndDate(movieId, city, showDate);
        return buildShowListingDto(shows);
    }

    @Transactional(readOnly = true)
    public Page<ShowResponseDto> searchShows(ShowSearchRequestDto request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy())
        );

        // This would need a custom implementation with Criteria API or Specifications
        // For now, returning a basic implementation
        Page<Show> shows = showRepository.findAll(pageable);
        return shows.map(this::mapToShowResponseDto);
    }

    // ==================== UPDATE OPERATIONS ====================

    public ShowResponseDto updateShow(Long showId, ShowUpdateRequestDto request) {
        log.info("Updating show with ID: {}", showId);

        Show show = findShowById(showId);

        // Validate update is allowed
        if (!show.canBeCancelled() && request.getStatus() != null) {
            throw new ShowNotModifiableException("Show cannot be modified at this time");
        }

        // If updating time/date, check for conflicts
        if (request.getShowDate() != null || request.getShowTime() != null) {
            ShowCreateRequestDto conflictRequest = ShowCreateRequestDto.builder()
                    .movieId(show.getMovie().getMovieId())
                    .screenId(show.getScreen().getScreenId())
                    .showDate(request.getShowDate() != null ? request.getShowDate() : show.getShowDate())
                    .showTime(request.getShowTime() != null ? request.getShowTime() : show.getShowTime())
                    .build();

            validateShowScheduling(conflictRequest, showId);
        }

        // Apply updates
        updateShowFromRequest(show, request);
        show = showRepository.save(show);

        log.info("Successfully updated show with ID: {}", showId);
        return mapToShowResponseDto(show);
    }

    public void cancelShow(Long showId, String reason) {
        log.info("Cancelling show with ID: {} for reason: {}", showId, reason);

        Show show = findShowById(showId);
        show.cancelShow(reason);
        showRepository.save(show);

        log.info("Successfully cancelled show with ID: {}", showId);
    }

    // ==================== CONFLICT DETECTION ====================

    @Transactional(readOnly = true)
    public ShowConflictCheckResponseDto checkShowConflicts(ShowConflictCheckRequestDto request) {
        LocalDateTime startTime = LocalDateTime.of(request.getShowDate(), request.getShowTime());

        // Calculate end time including intermission and cleaning
        int totalDuration = request.getMovieDurationMinutes();
        if (request.getHasIntermission() != null && request.getHasIntermission()) {
            totalDuration += request.getIntermissionDurationMinutes() != null ?
                    request.getIntermissionDurationMinutes() : 15;
        }

        int cleaningTime = request.getCleaningTimeMinutes() != null ?
                request.getCleaningTimeMinutes() : 20;

        LocalDateTime endTime = startTime.plusMinutes(totalDuration + cleaningTime);

        List<Show> conflictingShows;
        if (request.getExcludeShowId() != null) {
            conflictingShows = showRepository.findConflictingShowsExcluding(
                    request.getScreenId(), request.getShowDate(), startTime, endTime, request.getExcludeShowId());
        } else {
            conflictingShows = showRepository.findConflictingShows(
                    request.getScreenId(), request.getShowDate(), startTime, endTime);
        }

        return ShowConflictCheckResponseDto.builder()
                .hasConflicts(!conflictingShows.isEmpty())
                .message(conflictingShows.isEmpty() ? "No conflicts found" :
                        "Found " + conflictingShows.size() + " conflicting shows")
                .conflictingShows(conflictingShows.stream()
                        .map(this::mapToConflictingShowDto)
                        .collect(Collectors.toList()))
                .build();
    }

    // ==================== PRICING OPERATIONS ====================

    @Transactional(readOnly = true)
    public ShowPricingResponseDto calculateShowPricing(ShowPricingRequestDto request) {
        Show show = findShowById(request.getShowId());

        // Apply custom date/time if provided
        if (request.getCustomDate() != null) {
            show.setShowDate(request.getCustomDate());
        }
        if (request.getCustomTime() != null) {
            show.setShowTime(request.getCustomTime());
        }
        if (request.getIsHoliday() != null) {
            show.setIsHoliday(request.getIsHoliday());
        }

        BigDecimal actualPrice = show.calculateActualPrice();

        return ShowPricingResponseDto.builder()
                .showId(show.getShowId())
                .basePrice(show.getBasePrice())
                .actualPrice(actualPrice)
                .pricingFactors(buildPricingFactors(show))
                .finalPrice(actualPrice)
                .build();
    }

    // ==================== ANALYTICS OPERATIONS ====================

    @Transactional(readOnly = true)
    public ShowAnalyticsDto getShowAnalytics(LocalDate startDate, LocalDate endDate) {
        log.info("Generating show analytics from {} to {}", startDate, endDate);

        // Get basic statistics
        List<Object[]> popularMovies = showRepository.getPopularMoviesByShowCount(startDate, endDate);
        List<Object[]> theaterRevenue = showRepository.getTheaterRevenueAnalysis(startDate, endDate);
        List<Object[]> hourlyAnalysis = showRepository.getHourlyOccupancyAnalysis(startDate, endDate);
        List<Object[]> dailyRevenue = showRepository.getDailyRevenue(startDate, endDate);
        List<Object[]> weekdayWeekend = showRepository.getWeekdayVsWeekendAnalysis(startDate, endDate);

        return ShowAnalyticsDto.builder()
                .fromDate(startDate)
                .toDate(endDate)
                .popularMovies(buildPopularMoviesList(popularMovies))
                .theaterPerformance(buildTheaterPerformanceList(theaterRevenue))
                .hourlyAnalysis(buildHourlyAnalysisList(hourlyAnalysis))
                .dailyRevenue(buildDailyRevenueList(dailyRevenue))
                .weekdayWeekendAnalysis(buildWeekdayWeekendAnalysis(weekdayWeekend))
                .build();
    }

    // ==================== SEAT MANAGEMENT ====================

    @Transactional
    public void bookSeats(Long showId, Integer seatsToBook) {
        Show show = findShowById(showId);

        if (!show.canBeBooked()) {
            throw new ShowNotBookableException("Show cannot be booked at this time");
        }

        show.bookSeats(seatsToBook);
        showRepository.save(show);

        log.info("Booked {} seats for show ID: {}", seatsToBook, showId);
    }

    @Transactional
    public void releaseSeats(Long showId, Integer seatsToRelease) {
        Show show = findShowById(showId);
        show.releaseSeats(seatsToRelease);
        showRepository.save(show);

        log.info("Released {} seats for show ID: {}", seatsToRelease, showId);
    }

    @Transactional(readOnly = true)
    public SeatAvailabilityResponseDto checkSeatAvailability(SeatAvailabilityRequestDto request) {
        Show show = findShowById(request.getShowId());

        return SeatAvailabilityResponseDto.builder()
                .showId(show.getShowId())
                .totalSeats(show.getTotalSeats())
                .bookedSeats(show.getBookedSeats())
                .availableSeats(show.getAvailableSeats())
                .canAccommodateRequest(show.getAvailableSeats() >= request.getRequiredSeats())
                .availabilityStatus(show.isFullyBooked() ? "HOUSEFULL" :
                        show.getAvailableSeats() < 10 ? "FILLING_FAST" : "AVAILABLE")
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void validateShowScheduling(ShowCreateRequestDto request, Long excludeShowId) {
        Movie movie = findMovieById(request.getMovieId());

        ShowConflictCheckRequestDto conflictRequest = ShowConflictCheckRequestDto.builder()
                .screenId(request.getScreenId())
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .movieDurationMinutes(movie.getDurationMinutes())
                .hasIntermission(request.getHasIntermission())
                .intermissionDurationMinutes(request.getIntermissionDurationMinutes())
                .cleaningTimeMinutes(request.getCleaningTimeMinutes())
                .excludeShowId(excludeShowId)
                .build();

        ShowConflictCheckResponseDto conflictResponse = checkShowConflicts(conflictRequest);

        if (conflictResponse.getHasConflicts()) {
            throw new ShowSchedulingConflictException(
                    "Show scheduling conflict detected: " + conflictResponse.getMessage());
        }
    }

    private Show buildShowFromRequest(ShowCreateRequestDto request, Movie movie, Screen screen) {
        return Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .basePrice(request.getBasePrice())
                .weekendMultiplier(request.getWeekendMultiplier() != null ?
                        request.getWeekendMultiplier() : BigDecimal.valueOf(1.5))
                .primeTimeMultiplier(request.getPrimeTimeMultiplier() != null ?
                        request.getPrimeTimeMultiplier() : BigDecimal.valueOf(1.8))
                .holidayMultiplier(request.getHolidayMultiplier() != null ?
                        request.getHolidayMultiplier() : BigDecimal.valueOf(2.0))
                .hasIntermission(request.getHasIntermission() != null ?
                        request.getHasIntermission() : true)
                .intermissionDurationMinutes(request.getIntermissionDurationMinutes() != null ?
                        request.getIntermissionDurationMinutes() : 15)
                .cleaningTimeMinutes(request.getCleaningTimeMinutes() != null ?
                        request.getCleaningTimeMinutes() : 20)
                .isSpecialScreening(request.getIsSpecialScreening() != null ?
                        request.getIsSpecialScreening() : false)
                .isPremiere(request.getIsPremiere() != null ?
                        request.getIsPremiere() : false)
                .isHoliday(request.getIsHoliday() != null ?
                        request.getIsHoliday() : false)
                .specialNotes(request.getSpecialNotes())
                .totalSeats(screen.getTotalSeats())
                .availableSeats(screen.getTotalSeats())
                .bookedSeats(0)
                .status(Show.ShowStatus.SCHEDULED)
                .build();
    }

    private void updateShowFromRequest(Show show, ShowUpdateRequestDto request) {
        if (request.getShowDate() != null) show.setShowDate(request.getShowDate());
        if (request.getShowTime() != null) show.setShowTime(request.getShowTime());
        if (request.getBasePrice() != null) show.setBasePrice(request.getBasePrice());
        if (request.getWeekendMultiplier() != null) show.setWeekendMultiplier(request.getWeekendMultiplier());
        if (request.getPrimeTimeMultiplier() != null) show.setPrimeTimeMultiplier(request.getPrimeTimeMultiplier());
        if (request.getHolidayMultiplier() != null) show.setHolidayMultiplier(request.getHolidayMultiplier());
        if (request.getHasIntermission() != null) show.setHasIntermission(request.getHasIntermission());
        if (request.getIntermissionDurationMinutes() != null) show.setIntermissionDurationMinutes(request.getIntermissionDurationMinutes());
        if (request.getCleaningTimeMinutes() != null) show.setCleaningTimeMinutes(request.getCleaningTimeMinutes());
        if (request.getIsSpecialScreening() != null) show.setIsSpecialScreening(request.getIsSpecialScreening());
        if (request.getIsPremiere() != null) show.setIsPremiere(request.getIsPremiere());
        if (request.getIsHoliday() != null) show.setIsHoliday(request.getIsHoliday());
        if (request.getStatus() != null) show.setStatus(request.getStatus());
        if (request.getSpecialNotes() != null) show.setSpecialNotes(request.getSpecialNotes());
    }

    private ShowResponseDto mapToShowResponseDto(Show show) {
        return ShowResponseDto.builder()
                .showId(show.getShowId())
                .showDate(show.getShowDate())
                .showTime(show.getShowTime())
                .showDateTime(show.getShowDateTime())
                .endTime(show.getEndTime())
                .basePrice(show.getBasePrice())
                .actualPrice(show.calculateActualPrice())
                .status(show.getStatus())
                .totalSeats(show.getTotalSeats())
                .bookedSeats(show.getBookedSeats())
                .availableSeats(show.getAvailableSeats())
                .occupancyPercentage(show.getOccupancyPercentage())
                .hasIntermission(show.getHasIntermission())
                .intermissionDurationMinutes(show.getIntermissionDurationMinutes())
                .isSpecialScreening(show.getIsSpecialScreening())
                .isPremiere(show.getIsPremiere())
                .isHoliday(show.getIsHoliday())
                .isWeekend(show.isWeekend())
                .isPrimeTime(show.isPrimeTime())
                .isMatinee(show.isMatineeShow())
                .isLateNight(show.isLateNightShow())
                .specialNotes(show.getSpecialNotes())
                .movie(mapToMovieDetailsDto(show.getMovie()))
                .theater(mapToTheaterDetailsDto(show.getScreen().getTheater()))
                .screen(mapToScreenDetailsDto(show.getScreen()))
                .build();
    }

    private ShowSummaryDto mapToShowSummaryDto(Show show) {
        return ShowSummaryDto.builder()
                .showId(show.getShowId())
                .movieTitle(show.getMovie().getTitle())
                .theaterName(show.getScreen().getTheater().getName())
                .screenName(show.getScreen().getName())
                .showDate(show.getShowDate())
                .showTime(show.getShowTime())
                .actualPrice(show.calculateActualPrice())
                .availableSeats(show.getAvailableSeats())
                .occupancyPercentage(show.getOccupancyPercentage())
                .status(show.getStatus())
                .canBeBooked(show.canBeBooked())
                .isSpecialScreening(show.getIsSpecialScreening())
                .isPremiere(show.getIsPremiere())
                .build();
    }

    private ShowResponseDto.MovieDetailsDto mapToMovieDetailsDto(Movie movie) {
        return ShowResponseDto.MovieDetailsDto.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .language(movie.getLanguage().toString())
                .rating(String.valueOf(movie.getRating()))
                .durationMinutes(movie.getDurationMinutes())
                .genres(movie.getGenres().stream().map(Enum::toString).collect(Collectors.toList()))
                .posterUrl(movie.getPosterUrl())
                .build();
    }

    private ShowResponseDto.TheaterDetailsDto mapToTheaterDetailsDto(Theater theater) {
        return ShowResponseDto.TheaterDetailsDto.builder()
                .theaterId(theater.getTheaterId())
                .name(theater.getName())
                .address(theater.getAddress())
                .city(theater.getCity())
                .state(theater.getState())
                .latitude(theater.getLatitude())
                .longitude(theater.getLongitude())
                .phoneNumber(theater.getPhoneNumber())
                .build();
    }

    private ShowResponseDto.ScreenDetailsDto mapToScreenDetailsDto(Screen screen) {
        return ShowResponseDto.ScreenDetailsDto.builder()
                .screenId(screen.getScreenId())
                .name(screen.getName())
                .screenType(screen.getScreenType().toString())
                .soundSystem(screen.getSoundSystem().toString())
                .totalSeats(screen.getTotalSeats())
                .features(screen.getFeatures().stream().map(Enum::toString).collect(Collectors.toList()))
                .build();
    }

    private ShowConflictCheckResponseDto.ConflictingShowDto mapToConflictingShowDto(Show show) {
        return ShowConflictCheckResponseDto.ConflictingShowDto.builder()
                .showId(show.getShowId())
                .movieTitle(show.getMovie().getTitle())
                .startTime(show.getShowDateTime())
                .endTime(show.getActualEndTime())
                .conflictReason("Time overlap detected")
                .build();
    }

    private ShowListingDto buildShowListingDto(List<Show> shows) {
        if (shows.isEmpty()) {
            return ShowListingDto.builder().build();
        }

        Show firstShow = shows.get(0);
        Movie movie = firstShow.getMovie();

        // Group shows by theater
        var showsByTheater = shows.stream()
                .collect(Collectors.groupingBy(show -> show.getScreen().getTheater()));

        List<ShowListingDto.TheaterShowDto> theaterShows = showsByTheater.entrySet().stream()
                .map(entry -> {
                    var theater = entry.getKey();
                    var theaterShowList = entry.getValue();

                    List<ShowListingDto.ShowTimeDto> showTimes = theaterShowList.stream()
                            .map(show -> ShowListingDto.ShowTimeDto.builder()
                                    .showId(show.getShowId())
                                    .showTime(show.getShowTime())
                                    .screenName(show.getScreen().getName())
                                    .screenType(show.getScreen().getScreenType().toString())
                                    .actualPrice(show.calculateActualPrice())
                                    .availableSeats(show.getAvailableSeats())
                                    .isSpecialScreening(show.getIsSpecialScreening())
                                    .isPremiere(show.getIsPremiere())
                                    .canBeBooked(show.canBeBooked())
                                    .build())
                            .sorted((s1, s2) -> s1.getShowTime().compareTo(s2.getShowTime()))
                            .collect(Collectors.toList());

                    return ShowListingDto.TheaterShowDto.builder()
                            .theaterId(theater.getTheaterId())
                            .theaterName(theater.getName())
                            .theaterAddress(theater.getAddress())
                            .city(theater.getCity())
                            .latitude(theater.getLatitude())
                            .longitude(theater.getLongitude())
                            .showTimes(showTimes)
                            .build();
                })
                .collect(Collectors.toList());

        return ShowListingDto.builder()
                .movieId(movie.getMovieId())
                .movieTitle(movie.getTitle())
                .movieLanguage(movie.getLanguage().toString())
                .movieRating(String.valueOf(movie.getRating()))
                .movieDuration(movie.getDurationMinutes())
                .movieGenres(movie.getGenres().stream().map(Enum::toString).collect(Collectors.toList()))
                .posterUrl(movie.getPosterUrl())
                .theaters(theaterShows)
                .build();
    }

    private List<ShowPricingResponseDto.PricingFactorDto> buildPricingFactors(Show show) {
        List<ShowPricingResponseDto.PricingFactorDto> factors = new java.util.ArrayList<>();

        // Weekend factor
        factors.add(ShowPricingResponseDto.PricingFactorDto.builder()
                .factorName("Weekend Pricing")
                .multiplier(show.getWeekendMultiplier())
                .description("Additional charge for weekend shows")
                .applied(show.isWeekend())
                .build());

        // Prime time factor
        factors.add(ShowPricingResponseDto.PricingFactorDto.builder()
                .factorName("Prime Time Pricing")
                .multiplier(show.getPrimeTimeMultiplier())
                .description("Additional charge for prime time shows (6 PM - 10 PM)")
                .applied(show.isPrimeTime())
                .build());

        // Holiday factor
        factors.add(ShowPricingResponseDto.PricingFactorDto.builder()
                .factorName("Holiday Pricing")
                .multiplier(show.getHolidayMultiplier())
                .description("Additional charge for holiday shows")
                .applied(show.getIsHoliday())
                .build());

        // Special screening factor
        if (show.getIsSpecialScreening()) {
            factors.add(ShowPricingResponseDto.PricingFactorDto.builder()
                    .factorName("Special Screening")
                    .multiplier(BigDecimal.valueOf(1.3))
                    .description("Additional charge for special screenings")
                    .applied(true)
                    .build());
        }

        // Premiere factor
        if (show.getIsPremiere()) {
            factors.add(ShowPricingResponseDto.PricingFactorDto.builder()
                    .factorName("Premiere Show")
                    .multiplier(BigDecimal.valueOf(2.5))
                    .description("Premium pricing for premiere shows")
                    .applied(true)
                    .build());
        }

        return factors;
    }

    private List<ShowAnalyticsDto.PopularMovieDto> buildPopularMoviesList(List<Object[]> popularMovies) {
        return popularMovies.stream()
                .map(row -> ShowAnalyticsDto.PopularMovieDto.builder()
                        .movieTitle((String) row[0])
                        .showCount(((Number) row[1]).longValue())
                        .averageOccupancy(((Number) row[2]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ShowAnalyticsDto.TheaterPerformanceDto> buildTheaterPerformanceList(List<Object[]> theaterRevenue) {
        return theaterRevenue.stream()
                .map(row -> ShowAnalyticsDto.TheaterPerformanceDto.builder()
                        .theaterName((String) row[0])
                        .showCount(((Number) row[1]).longValue())
                        .totalRevenue(BigDecimal.valueOf(((Number) row[2]).doubleValue()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ShowAnalyticsDto.HourlyAnalysisDto> buildHourlyAnalysisList(List<Object[]> hourlyAnalysis) {
        return hourlyAnalysis.stream()
                .map(row -> ShowAnalyticsDto.HourlyAnalysisDto.builder()
                        .hour(((Number) row[0]).intValue())
                        .showCount(((Number) row[1]).longValue())
                        .averageOccupancy(((Number) row[2]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ShowAnalyticsDto.DailyRevenueDto> buildDailyRevenueList(List<Object[]> dailyRevenue) {
        return dailyRevenue.stream()
                .map(row -> ShowAnalyticsDto.DailyRevenueDto.builder()
                        .date((LocalDate) row[0])
                        .revenue(BigDecimal.valueOf(((Number) row[1]).doubleValue()))
                        .build())
                .collect(Collectors.toList());
    }

    private ShowAnalyticsDto.WeekdayWeekendAnalysisDto buildWeekdayWeekendAnalysis(List<Object[]> weekdayWeekend) {
        if (weekdayWeekend.size() < 2) {
            return ShowAnalyticsDto.WeekdayWeekendAnalysisDto.builder().build();
        }

        // Assuming first row is weekday, second is weekend
        Object[] weekdayRow = weekdayWeekend.get(0);
        Object[] weekendRow = weekdayWeekend.get(1);

        return ShowAnalyticsDto.WeekdayWeekendAnalysisDto.builder()
                .weekdayOccupancy(((Number) weekdayRow[2]).doubleValue())
                .weekdayShows(((Number) weekdayRow[1]).longValue())
                .weekendOccupancy(((Number) weekendRow[2]).doubleValue())
                .weekendShows(((Number) weekendRow[1]).longValue())
                .build();
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() >= 6; // Saturday = 6, Sunday = 7
    }

    private Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));
    }

    private Screen findScreenById(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with ID: " + screenId));
    }

    private Show findShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with ID: " + showId));
    }

    // ==================== CUSTOM EXCEPTIONS ====================

    public static class ShowNotFoundException extends BaseException {
        public ShowNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class MovieNotFoundException extends BaseException {
        public MovieNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class ScreenNotFoundException extends BaseException {
        public ScreenNotFoundException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 404;
        }
    }

    public static class ShowSchedulingConflictException extends BaseException {
        public ShowSchedulingConflictException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 409; // Conflict
        }
    }

    public static class ShowNotModifiableException extends BaseException {
        public ShowNotModifiableException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 422; // Unprocessable Entity
        }
    }

    public static class ShowNotBookableException extends BaseException {
        public ShowNotBookableException(String message) {
            super(message);
        }

        @Override
        public int getHttpStatusCode() {
            return 422; // Unprocessable Entity
        }
    }
}