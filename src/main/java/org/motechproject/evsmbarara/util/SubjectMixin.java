package org.motechproject.evsmbarara.util;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonView;

// View definitions:
class Views {
    static class Zetes {
    }
}

abstract class SubjectMixin {

    @Getter
    @Setter
    @JsonProperty("PID")
    @JsonView(Views.Zetes.class)
    private String subjectId;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String siteName;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String siteId;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String community;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String name;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private Integer age;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String gender;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String address;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String phoneNumber;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String guardianName;

    @Getter
    @Setter
    @JsonView(Views.Zetes.class)
    private String guardianType;

    @JsonProperty("language")
    @JsonView(Views.Zetes.class)
    abstract String getLanguageCode();
}
