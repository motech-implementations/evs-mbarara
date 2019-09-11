package org.motechproject.evsmbarara.utils;

import org.motechproject.evsmbarara.domain.Clinic;

public final class ClinicUtil {

    private ClinicUtil() {
    }

    public static Clinic createClinic(String siteId, String location) {
        Clinic clinic = new Clinic();
        clinic.setSiteId(siteId);
        clinic.setLocation(location);
        clinic.setMaxPrimeVisits(0);
        clinic.setMaxScreeningVisits(0);
        clinic.setMaxCapacityByDay(0);
        clinic.setMaxSixtyMonthsPostPrimeVisits(0);
        clinic.setMaxFortyEightMonthsPostPrimeVisits(0);
        clinic.setMaxThirtySixMonthsPostPrimeVisits(0);
        clinic.setMaxTwentyFourMonthsPostPrimeVisits(0);
        clinic.setMaxTwelveMonthsPostPrimeVisits(0);
        clinic.setMaxSixMonthsPostPrimeVisits(0);
        clinic.setMaxThreeMonthsPostPrimeVisits(0);
        clinic.setMaxBoosterFirstFollowUpVisits(0);
        clinic.setMaxBoosterVisits(0);
        clinic.setMaxPrimeThirdFollowUpVisits(0);
        clinic.setMaxPrimeSecondFollowUpVisits(0);
        clinic.setMaxPrimeFirstFollowUpVisits(0);
        return clinic;
    }
}
