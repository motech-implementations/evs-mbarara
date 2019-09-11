package org.motechproject.evsmbarara.web.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * DTO for request coming from Zetes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SubjectZetesDto {

    @JsonProperty("PID")
    @Getter
    @Setter
    private String subjectId;

    @Getter
    @Setter
    private String siteName;

    @Getter
    @Setter
    private String siteId;

    @Getter
    @Setter
    private String district;

    @Getter
    @Setter
    private String chiefdom;

    @Getter
    @Setter
    private String section;

    @Getter
    @Setter
    private String community;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer age;

    @Getter
    @Setter
    private String gender;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private String language;

    @Getter
    @Setter
    private String guardianName;

    @Getter
    @Setter
    private String guardianType;
}
