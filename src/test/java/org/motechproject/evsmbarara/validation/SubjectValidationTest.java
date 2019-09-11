package org.motechproject.evsmbarara.validation;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.motechproject.evsmbarara.web.domain.SubjectZetesDto;

import java.util.List;

import static com.google.common.collect.Iterables.any;

/**
 * Tests SubjectZetesDto validations
 */
public class SubjectValidationTest {

    private Predicate<ValidationError> hasErrorPredicate(final String errorMessage) {
        return new Predicate<ValidationError>() {
            @Override
            public boolean apply(ValidationError err) {
                return err.getMessage().equals(errorMessage);
            }
        };
    }

    @Test
    public void testSubjectIdValidation() {

        SubjectZetesDto subjectZetesDtoCorrectId = createBasicSubjectZetesDto();
        SubjectZetesDto subjectZetesDtoWrongId = createBasicSubjectZetesDto();

        subjectZetesDtoCorrectId.setSubjectId("20300026");
        subjectZetesDtoWrongId.setSubjectId("10100032");

        List<ValidationError> request1Errors = SubjectValidator.validate(subjectZetesDtoCorrectId);
        List<ValidationError> request2Errors = SubjectValidator.validate(subjectZetesDtoWrongId);

        Assert.assertTrue(request1Errors.isEmpty());
        Assert.assertTrue(any(request2Errors, hasErrorPredicate(ValidationError.SUBJECT_ID_NOT_VERIFIED)));
    }

    @Test
    public void testSubmitRequestNullValuesValidation() {
        SubjectZetesDto request = createBasicSubjectZetesDto();
        request.setLanguage(null);
        request.setSubjectId(null);
        request.setName(null);
        request.setSiteId(null);
        request.setPhoneNumber("123a435666");

        List<ValidationError> requestErrors = SubjectValidator.validate(request);

        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.LANGUAGE_NULL)));
        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.SUBJECT_ID_NULL)));
        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.NAME_NULL)));
        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.SITE_ID_NULL)));
        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.PHONE_NUMBER_HAS_NON_DIGITS)));
    }

    @Test
    public void testLanguageCodeValidation() {
        SubjectZetesDto request = createBasicSubjectZetesDto();
        SubjectZetesDto request2 = createBasicSubjectZetesDto();

        request.setLanguage("not-en");
        request2.setLanguage("kri");

        List<ValidationError> requestErrors = SubjectValidator.validate(request);
        List<ValidationError> request2Errors = SubjectValidator.validate(request2);

        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.LANGUAGE_NOT_CORRECT)));
        Assert.assertFalse(any(request2Errors, hasErrorPredicate(ValidationError.LANGUAGE_NOT_CORRECT)));
    }

    @Test
    public void testValuesWithNumbers() {
        SubjectZetesDto request = createBasicSubjectZetesDto();

        request.setSubjectId("1231QWE3463asd45");
        request.setName("King Lion 3rd");

        List<ValidationError> requestErrors = SubjectValidator.validate(request);

        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.SUBJECT_ID_NOT_VERIFIED)));
        Assert.assertTrue(any(requestErrors, hasErrorPredicate(ValidationError.NAME_HAS_DIGITS)));
    }

    @Test
    public void testEmptyPhoneNumber() {
        SubjectZetesDto request1 = createBasicSubjectZetesDto();
        request1.setPhoneNumber("");

        SubjectZetesDto request2 = createBasicSubjectZetesDto();
        request2.setPhoneNumber(null);

        List<ValidationError> request1Errors = SubjectValidator.validate(request1);
        List<ValidationError> request2Errors = SubjectValidator.validate(request2);

        Assert.assertTrue(request1Errors.isEmpty());
        Assert.assertTrue(request2Errors.isEmpty());
    }

    private SubjectZetesDto createBasicSubjectZetesDto() {
        SubjectZetesDto subjectZetesDto = new SubjectZetesDto();
        subjectZetesDto.setSubjectId("10100014");
        subjectZetesDto.setPhoneNumber("123456789");
        subjectZetesDto.setName("Kasia");
        subjectZetesDto.setAddress("Warszawa 19");
        subjectZetesDto.setLanguage("eng");
        subjectZetesDto.setCommunity("community");
        subjectZetesDto.setSiteId("newSiteId");
        subjectZetesDto.setSiteName("siteName");
        subjectZetesDto.setChiefdom("chiefdom");
        subjectZetesDto.setSection("section");
        subjectZetesDto.setDistrict("district");
        subjectZetesDto.setAge(18);
        subjectZetesDto.setGender("male");
        return subjectZetesDto;
    }
}
