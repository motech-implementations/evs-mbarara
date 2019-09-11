package org.motechproject.evsmbarara.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;

import javax.jdo.annotations.Unique;

@Entity(recordHistory = true)
@NoArgsConstructor
public class Clinic {

    @Field
    @Getter
    @Setter
    private Long id;

    @Unique
    @UIDisplayable(position = 0)
    @Field(required = true)
    @Getter
    @Setter
    private String siteId;

    @UIDisplayable(position = 1)
    @Field(required = true)
    @Getter
    @Setter
    private String location;

    @UIDisplayable(position = 6)
    @Field(displayName = "Amount of Rooms")
    @Getter
    @Setter
    private Integer numberOfRooms;

    @UIDisplayable(position = 2)
    @Field(required = true, defaultValue = "80")
    @Getter
    @Setter
    private Integer maxCapacityByDay;

    @UIDisplayable(position = 3)
    @Field(required = true, defaultValue = "30")
    @Getter
    @Setter
    private Integer maxScreeningVisits;

    @UIDisplayable(position = 4)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxPrimeVisits;

    @UIDisplayable(position = 7)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxPrimeFirstFollowUpVisits;

    @UIDisplayable(position = 8)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxPrimeSecondFollowUpVisits;

    @UIDisplayable(position = 9)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxPrimeThirdFollowUpVisits;

    @UIDisplayable(position = 5)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxBoosterVisits;

    @UIDisplayable(position = 10)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxBoosterFirstFollowUpVisits;

    @UIDisplayable(position = 11)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxThreeMonthsPostPrimeVisits;

    @UIDisplayable(position = 12)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxSixMonthsPostPrimeVisits;

    @UIDisplayable(position = 13)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxTwelveMonthsPostPrimeVisits;

    @UIDisplayable(position = 14)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxTwentyFourMonthsPostPrimeVisits;

    @UIDisplayable(position = 15)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxThirtySixMonthsPostPrimeVisits;

    @UIDisplayable(position = 16)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxFortyEightMonthsPostPrimeVisits;

    @UIDisplayable(position = 17)
    @Field(required = true, defaultValue = "20")
    @Getter
    @Setter
    private Integer maxSixtyMonthsPostPrimeVisits;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    @Override
    public String toString() {
        return location;
    }
}

