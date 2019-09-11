package org.motechproject.evsmbarara.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CapacityReportDto {

    @Getter
    @Setter
    private String date;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private Integer maxCapacity;

    @Getter
    @Setter
    private Integer availableCapacity;

    @Getter
    @Setter
    private Integer screeningSlotRemaining;

    @Getter
    @Setter
    private Integer vaccineSlotRemaining;

    public CapacityReportDto(String date, String location, Integer maxCapacity, Integer availableCapacity, Integer screeningSlotRemaining, Integer vaccineSlotRemaining) {
        this.date = date;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.availableCapacity = availableCapacity;
        this.screeningSlotRemaining = screeningSlotRemaining;
        this.vaccineSlotRemaining = vaccineSlotRemaining;
    }
}
