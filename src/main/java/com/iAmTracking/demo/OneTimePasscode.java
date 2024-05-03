package com.iAmTracking.demo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class OneTimePasscode {
    private final String code;
    private final LocalDateTime expirationTime;

    /**
     * Constructor for OneTimePasscode.
     * @param code the OTP code
     * @param validityDuration the duration in minutes for which the code is valid
     */
    public OneTimePasscode(String code, long validityDuration) {
        this.code = code;
        this.expirationTime = LocalDateTime.now().plus(validityDuration, ChronoUnit.MINUTES);
    }

    /**
     * Returns the OTP code.
     * @return the OTP code
     */
    public String getCode() {
        return code;
    }

    /**
     * Checks if the OTP has expired.
     * @return true if the current time is after the expiration time, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    public static String generateCode() {
        Random random = new Random();
        int partOne = random.nextInt(10000);
        int partTwo = random.nextInt(10000);
        return String.format("%04d-%04d", partOne, partTwo);
    }


}
