package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;

import javax.validation.constraints.Pattern;

@Entity(recordHistory = true)
public class Volunteer {

    @Field
    @Getter
    @Setter
    private Long id;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    @Field(required = true)
    @Getter
    @Setter
    private String name;

    @Pattern(regexp = "^[0-9\\s]*$")
    @Field
    @Getter
    @Setter
    private String contactNumber;

    @Field
    @Getter
    @Setter
    private String additionalContact;
}
