package com.bookmymovie.constants;

import lombok.Getter;

public class UserConstant {

    @Getter
    public enum UserRole {
        USER, ADMIN, THEATER_OWNER
    }
}
