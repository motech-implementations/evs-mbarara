package org.motechproject.evsmbarara.domain.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Gender {
    Male("male"),
    Female("female"),
    Unknown("unknown"),
    Undifferentiated("undifferentiated");

    @Getter
    private String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender getByValue(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.getValue().equals(value)) {
                return gender;
            }
        }
        return null;
    }

    public static Set<String> getListOfValues() {
        Set<String> codes = new HashSet<>();

        for (Gender gender : values()) {
            codes.add(gender.getValue());
        }

        return Collections.unmodifiableSet(codes);
    }
}
