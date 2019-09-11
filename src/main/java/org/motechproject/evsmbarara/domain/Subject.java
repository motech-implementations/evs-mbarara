package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.evsmbarara.domain.enums.Gender;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.util.serializer.CustomDateDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateTimeDeserializer;
import org.motechproject.evsmbarara.util.serializer.CustomDateTimeSerializer;
import org.motechproject.evsmbarara.util.serializer.CustomVisitListDeserializer;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Models data for registration of Subject in EVS Mbarara
 */
@Entity(recordHistory = true, name = "Participant", maxFetchDepth = 3)
@NoArgsConstructor
public class Subject {
    public static final String SUBJECT_ID_FIELD_NAME = "subjectId";
    public static final String SUBJECT_ID_FIELD_DISPLAY_NAME = "Participant Id";

    /**
     * Fields captured in ZETES
     */

    @Unique
    @NonEditable
    @UIDisplayable(position = 0)
    @Field(required = true, displayName = SUBJECT_ID_FIELD_DISPLAY_NAME)
    @Getter
    @Setter
    private String subjectId;

    @UIDisplayable(position = 1)
    @Field(required = true)
    @Getter
    @Setter
    private String name;

    @UIDisplayable(position = 2)
    @Column(length = 20)
    @Pattern(regexp = "^[0-9\\s]*$")
    @Field
    @Getter
    @Setter
    private String phoneNumber;

    @UIDisplayable(position = 3)
    @Field
    @Getter
    @Setter
    private String address;

    @NonEditable
    @UIDisplayable(position = 4)
    @Field(required = true)
    @Getter
    @Setter
    private Gender gender;

    @UIDisplayable(position = 5)
    @Column(length = 20)
    @Field(required = true)
    @Getter
    @Setter
    private Language language;

    @NonEditable
    @UIDisplayable(position = 6)
    @Field(required = true)
    @Getter
    @Setter
    private String siteId;

    @NonEditable
    @UIDisplayable(position = 7)
    @Field(required = true)
    @Getter
    @Setter
    private String siteName;

    @UIDisplayable(position = 8)
    @Field
    @Getter
    @Setter
    private String community;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String chiefdom;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String section;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String district;

    @UIDisplayable(position = 9)
    @Field(required = true)
    @Getter
    @Setter
    private Integer age;

    @UIDisplayable(position = 10)
    @Field
    @Getter
    @Setter
    private String guardianName;

    @UIDisplayable(position = 11)
    @Field
    @Getter
    @Setter
    private String guardianType;

    /**
     * Other fields
     */

    @UIDisplayable(position = 12)
    @JsonDeserialize(using = CustomVisitListDeserializer.class)
    @Field
    @Persistent(mappedBy = "subject")
    @Cascade(delete = true)
    @Getter
    @Setter
    private List<Visit> visits = new ArrayList<>();

    @UIDisplayable(position = 13)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate primerVaccinationDate;

    @UIDisplayable(position = 14)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate boosterVaccinationDate;

    @Field(defaultValue = "false")
    @Getter
    @Setter
    private boolean changed;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private Boolean femaleChildBearingAge;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private Integer yearOfBirth;

    /**
     * Motech internal fields
     */
    @Field
    @Getter
    @Setter
    private Long id;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime creationDate;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime modificationDate;

    public void setPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            this.phoneNumber = null;
        } else {
            this.phoneNumber = phoneNumber;
        }
    }

    public void setAddress(String address) {
        if (StringUtils.isBlank(address)) {
            this.address = null;
        } else {
            this.address = address;
        }
    }

    @Ignore
    public String getLanguageCode() {
        if (language != null) {
            return language.getCode();
        } else {
            return null;
        }

    }

    public void setLanguageCode(String languageCode) {
        //this setter is needed, because json deserialization doesn't work properly without it
    }

    @Override
    public String toString() {
        return subjectId;
    }
}
