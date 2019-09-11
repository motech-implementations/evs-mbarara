package org.motechproject.evsmbarara.web;

import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.service.ConfigService;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.evsmbarara.service.ZetesService;
import org.motechproject.evsmbarara.validation.SubjectValidator;
import org.motechproject.evsmbarara.validation.ValidationError;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jdo.JDOException;
import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;

/**
 * Web API for Subject Registration
 */
@Controller
public class ZetesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZetesController.class);

    private SubjectService subjectService;

    private ConfigService configService;

    private ZetesService zetesService;

    @PreAuthorize("hasAnyRole('mdsDataAccess', 'participantRegistration')")
    @RequestMapping(value = "/registration/submit", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> submitSubjectRequest(@RequestBody SubjectZetesDto subjectZetesDto) {

        List<ValidationError> errorList =  SubjectValidator.validate(subjectZetesDto);

        if (!errorList.isEmpty()) {
            List<String> validationMessages = extract(errorList, on(ValidationError.class).getMessage());
            LOGGER.error("Subject : {} - {}", subjectZetesDto.getSubjectId(), validationMessages.toString());
            return new ResponseEntity<>(validationMessages.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            subjectService.createOrUpdateForZetes(subjectZetesDto);
        } catch (JDOException ex) {
            LOGGER.warn("Error raised during creating subject: " + ex.getMessage(), ex);
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            LOGGER.error("Fatal error raised during creating subject: " + ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/runJob", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> runZetesJob() {
        Config config = configService.getConfig();

        try {
            zetesService.sendUpdatedSubjects(config.getZetesUrl(), config.getZetesUsername(), config.getZetesPassword());
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Autowired
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Autowired
    public void setZetesService(ZetesService zetesService) {
        this.zetesService = zetesService;
    }
}
