package com.ilerijava.tcdd.entegration.enums;

public enum SeatType {
    EKONOMI("Y1"),
    YATAKLI("B"),
    TEKERLEKLI_SANDALYE("DSB");

    private final String code;

    SeatType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static String getCodeFromType(String type) {
        try {
            return valueOf(type.toUpperCase()).getCode();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Geçersiz koltuk tipi. Geçerli tipler: EKONOMI, YATAKLI, TEKERLEKLI_SANDALYE");
        }
    }
}