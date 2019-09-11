package org.motechproject.evsmbarara.dto;

import lombok.Getter;
import lombok.Setter;

public class ScreeningDto {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String volunteerId;

    @Getter
    @Setter
    private String clinicId;

    @Getter
    @Setter
    private String date;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String contactNumber;

    @Getter
    @Setter
    private String additionalContact;

    @Getter
    @Setter
    private String clinicLocation;
}
