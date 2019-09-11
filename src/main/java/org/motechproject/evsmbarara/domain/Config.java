package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static final String TIME_PICKER_FORMAT = "HH:mm";

    @Getter
    @Setter
    private List<String> clinicMainFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> clinicExtendedFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> boosterRelatedVisits = new ArrayList<>();

    @Getter
    @Setter
    private Boolean showWarnings = true;

    @Getter
    @Setter
    private String zetesUrl;

    @Getter
    @Setter
    private String zetesUsername;

    @Getter
    @Setter
    private String zetesPassword;

    @Getter
    @Setter
    private String startTime;

    @Getter
    @Setter
    private Boolean enableZetesJob = false;
}
