package com.bookmymovie.constants;

public class TheaterConstant {

    // Theater Types
    public enum TheaterType {
        MULTIPLEX("Multiplex - Multiple screens with modern amenities"),
        SINGLE_SCREEN("Single Screen - Traditional theater with one screen"),
        IMAX("IMAX - Large format premium experience"),
        DRIVE_IN("Drive-in - Outdoor screening for cars"),
        PREMIUM("Premium - Luxury theater with high-end facilities");

        private final String description;

        TheaterType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Theater Status
    public enum TheaterStatus {
        ACTIVE("Theater is operational and accepting bookings"),
        INACTIVE("Theater is temporarily closed"),
        UNDER_RENOVATION("Theater is under renovation"),
        PERMANENTLY_CLOSED("Theater has been permanently shut down");

        private final String description;

        TheaterStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Theater Facilities
    public enum Facility {
        PARKING("Car parking available"),
        FOOD_COURT("Food court and restaurants"),
        ATM("ATM facility available"),
        WHEELCHAIR_ACCESS("Wheelchair accessible"),
        AIR_CONDITIONING("Air conditioned premises"),
        RECLINER_SEATS("Recliner seating available"),
        DOLBY_ATMOS("Dolby Atmos sound system"),
        VALET_PARKING("Valet parking service"),
        CAFE("Cafe and snacks"),
        GIFT_SHOP("Gift shop available"),
        CLOAKROOM("Cloakroom facility"),
        ELEVATOR("Elevator access"),
        BABY_CARE_ROOM("Baby care room available"),
        FOUR_DX("4DX experience with motion seats and effects");

        private final String description;

        Facility(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Screen Types
    public enum ScreenType {
        REGULAR_2D("Regular 2D screen"),
        IMAX_2D("IMAX 2D large format screen"),
        IMAX_3D("IMAX 3D large format screen"),
        DOLBY_CINEMA("Dolby Cinema with enhanced audio-visual"),
        FOUR_DX("4DX with motion seats and environmental effects"),
        SCREEN_X("ScreenX with 270-degree viewing"),
        VR("Virtual Reality experience"),
        PREMIUM("Premium screen with luxury seating");

        private final String description;

        ScreenType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Sound Systems
    public enum SoundSystem {
        STEREO("Basic stereo sound"),
        SURROUND_5_1("5.1 surround sound"),
        SURROUND_7_1("7.1 surround sound"),
        DOLBY_ATMOS("Dolby Atmos 3D audio"),
        DTS_X("DTS:X immersive audio"),
        IMAX_ENHANCED("IMAX Enhanced audio system");

        private final String description;

        SoundSystem(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Screen Status
    public enum ScreenStatus {
        ACTIVE("Screen is operational"),
        INACTIVE("Screen is temporarily closed"),
        UNDER_MAINTENANCE("Screen is under maintenance"),
        RENOVATION("Screen is being renovated");

        private final String description;

        ScreenStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Screen Features
    public enum ScreenFeature {
        DIGITAL_PROJECTION("Digital projection system"),
        LASER_PROJECTION("Laser projection technology"),
        CURVED_SCREEN("Curved screen design"),
        EXTRA_LARGE("Extra large screen size"),
        PREMIUM_SOUND("Premium sound system"),
        MOTION_SEATS("Motion synchronized seats"),
        ENVIRONMENTAL_EFFECTS("Environmental effects (wind, fog, etc.)"),
        RECLINER_SEATING("Recliner seating throughout"),
        IN_SEAT_SERVICE("In-seat food and beverage service"),
        PRIVATE_BOXES("Private viewing boxes available");

        private final String description;

        ScreenFeature(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Seat Categories
    public enum SeatCategory {
        REGULAR("Regular seating"),
        PREMIUM("Premium seating with better location"),
        VIP("VIP seating with luxury amenities"),
        EXECUTIVE("Executive seating with extra space"),
        BALCONY("Balcony seating"),
        BOX("Private box seating"),
        WHEELCHAIR("Wheelchair accessible seating");

        private final String description;

        SeatCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Seat Status
    public enum SeatStatus {
        AVAILABLE("Seat is available for booking"),
        BLOCKED("Seat is blocked and cannot be booked"),
        UNDER_MAINTENANCE("Seat is under maintenance"),
        PERMANENTLY_BLOCKED("Seat is permanently out of service");

        private final String description;

        SeatStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Seat Types
    public enum SeatType {
        REGULAR("Regular theater seat"),
        RECLINER("Reclining seat"),
        LOVE_SEAT("Couple/love seat"),
        WHEELCHAIR("Wheelchair accessible space"),
        BEAN_BAG("Bean bag seating"),
        SOFA("Sofa-style seating"),
        DIRECTOR_CHAIR("Director-style chair");

        private final String description;

        SeatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Seat Features
    public enum SeatFeature {
        POWER_OUTLET("Power outlet available"),
        USB_CHARGING("USB charging port"),
        CUP_HOLDER("Cup holder"),
        ARMREST("Adjustable armrest"),
        FOOTREST("Footrest"),
        MASSAGE("Massage function"),
        HEATING("Heated seat"),
        COOLING("Cooled seat"),
        TABLET_HOLDER("Tablet/phone holder"),
        READING_LIGHT("Personal reading light"),
        WHEELCHAIR_ACCESSIBLE("Wheelchair accessible space");

        private final String description;

        SeatFeature(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Distance calculation constants
    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final int DEFAULT_SEARCH_RADIUS_KM = 25;
    public static final int MAX_SEARCH_RADIUS_KM = 100;

    // Business rules
    public static final int MIN_SHOW_DURATION_MINUTES = 60;
    public static final int MAX_SHOW_DURATION_MINUTES = 300;
    public static final int CLEANING_TIME_MINUTES = 30;
    public static final int INTERMISSION_TIME_MINUTES = 15;

    // Validation patterns
    public static final String SEAT_LABEL_PATTERN = "^[A-Z]{1,2}$";
    public static final String THEATER_NAME_PATTERN = "^[a-zA-Z0-9\\s\\-\\.]{2,100}$";
}