package org.motechproject.evsmbarara.service.impl;

import org.motechproject.evsmbarara.domain.Clinic;
import org.motechproject.evsmbarara.repository.ClinicDataService;
import org.motechproject.evsmbarara.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("clinicService")
public class ClinicServiceImpl implements ClinicService {

    @Autowired
    private ClinicDataService clinicDataService;

    @Override
    public List<Clinic> getClinics() {
        return clinicDataService.retrieveAll();
    }
}
