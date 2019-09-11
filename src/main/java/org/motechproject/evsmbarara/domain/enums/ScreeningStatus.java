package org.motechproject.evsmbarara.domain.enums;

import lombok.Getter;

public enum ScreeningStatus {
    ACTIVE("Active"),
    CANCELED("Canceled"),
    COMPLETED("Completed");

    @Getter
    private String value;

    ScreeningStatus(String value) {
        this.value = value;
    }
}
