package com.bookmymovie.constants;

public class MovieConstant {

    public enum Genre {
        ACTION("Action"),
        ADVENTURE("Adventure"),
        COMEDY("Comedy"),
        DRAMA("Drama"),
        FANTASY("Fantasy"),
        HORROR("Horror"),
        MYSTERY("Mystery"),
        ROMANCE("Romance"),
        THRILLER("Thriller"),
        WESTERN("Western"),
        SCIENCE_FICTION("Science Fiction"),
        CRIME("Crime"),
        DOCUMENTARY("Documentary"),
        ANIMATION("Animation"),
        FAMILY("Family"),
        MUSIC("Music"),
        WAR("War"),
        BIOGRAPHY("Biography"),
        HISTORICAL("Historical"),
        SPORTS("Sports");

        private final String displayName;

        Genre(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Language {
        ENGLISH("English"),
        HINDI("Hindi"),
        TAMIL("Tamil"),
        TELUGU("Telugu"),
        KANNADA("Kannada"),
        MALAYALAM("Malayalam"),
        BENGALI("Bengali"),
        MARATHI("Marathi"),
        GUJARATI("Gujarati"),
        PUNJABI("Punjabi"),
        SPANISH("Spanish"),
        FRENCH("French"),
        GERMAN("German"),
        JAPANESE("Japanese"),
        KOREAN("Korean"),
        CHINESE("Chinese");

        private final String displayName;

        Language(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Rating {
        U("U", "Universal - suitable for all ages"),
        UA("UA", "Parental guidance for children under 12"),
        A("A", "Restricted to adults (18+)"),
        S("S", "Restricted to specialized audiences");

        private final String code;
        private final String description;

        Rating(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum MovieStatus {
        COMING_SOON("Coming Soon"),
        NOW_SHOWING("Now Showing"),
        ENDED("Ended"),
        CANCELLED("Cancelled");

        private final String displayName;

        MovieStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}