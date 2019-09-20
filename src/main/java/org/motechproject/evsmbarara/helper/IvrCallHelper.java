package org.motechproject.evsmbarara.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.motechproject.evsmbarara.constants.EvsMbararaConstants;
import org.motechproject.evsmbarara.domain.Config;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.domain.VotoLanguage;
import org.motechproject.evsmbarara.domain.VotoMessage;
import org.motechproject.evsmbarara.domain.enums.Language;
import org.motechproject.evsmbarara.exception.EvsInitiateCallException;
import org.motechproject.evsmbarara.repository.VotoLanguageDataService;
import org.motechproject.evsmbarara.repository.VotoMessageDataService;
import org.motechproject.evsmbarara.service.ConfigService;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.ivr.service.OutboundCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IvrCallHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IvrCallHelper.class);

    @Autowired
    private ConfigService configService;

    @Autowired
    private VotoMessageDataService votoMessageDataService;

    @Autowired
    private VotoLanguageDataService votoLanguageDataService;

    @Autowired
    private SubjectService subjectService;

    private OutboundCallService outboundCallService;

    public void initiateIvrCall(String messageKey, String externalId) {
        Config config = configService.getConfig();

        Subject subject = getSubject(externalId);

        if (config.getSendIvrCalls() != null && config.getSendIvrCalls()
            && subject.getLanguage() != null && StringUtils.isNotBlank(subject.getPhoneNumber())) {

            String votoLanguageId = getVotoLanguageId(subject.getLanguage(), externalId);
            String votoMessageId = getVotoMessageId(messageKey, externalId);

            JsonObject subscriber = new JsonObject();
            subscriber.addProperty(EvsMbararaConstants.PHONE, subject.getPhoneNumber());
            subscriber.addProperty(EvsMbararaConstants.LANGUAGE, votoLanguageId);

            JsonArray subscriberArray = new JsonArray();
            subscriberArray.add(subscriber);

            Gson gson = new GsonBuilder().serializeNulls().create();
            String subscribers = gson.toJson(subscriberArray);

            Map<String, String> callParams = new HashMap<>();
            callParams.put(EvsMbararaConstants.API_KEY, config.getApiKey());
            callParams.put(EvsMbararaConstants.MESSAGE_ID, votoMessageId);
            callParams.put(EvsMbararaConstants.STATUS_CALLBACK_URL, config.getStatusCallbackUrl());
            callParams.put(EvsMbararaConstants.SUBSCRIBERS, subscribers);
            callParams.put(EvsMbararaConstants.SEND_SMS_IF_VOICE_FAILS, config.getSendSmsIfVoiceFails() ? "1" : "0");
            callParams.put(EvsMbararaConstants.DETECT_VOICEMAIL, config.getDetectVoiceMail() ? "1" : "0");
            callParams.put(EvsMbararaConstants.RETRY_ATTEMPTS_SHORT, config.getRetryAttempts().toString());
            callParams.put(EvsMbararaConstants.RETRY_DELAY_SHORT, config.getRetryDelay().toString());
            callParams.put(EvsMbararaConstants.RETRY_ATTEMPTS_LONG, EvsMbararaConstants.RETRY_ATTEMPTS_LONG_DEFAULT);
            callParams.put(EvsMbararaConstants.SUBJECT_ID, externalId);
            callParams.put(EvsMbararaConstants.SUBJECT_PHONE_NUMBER, subject.getPhoneNumber());

            LOGGER.info("Initiating call: {}", callParams.toString());

            outboundCallService.initiateCall(config.getIvrSettingsName(), callParams);
        }
    }

    private Subject getSubject(String subjectId) {
        Subject subject = subjectService.findSubjectBySubjectId(subjectId);

        if (subject == null) {
            throw new EvsInitiateCallException("Cannot initiate call, because Provider with id: %s not found", subjectId);
        }

        return subject;
    }

    private String getVotoLanguageId(Language language, String subjectId) {
        VotoLanguage votoLanguage = votoLanguageDataService.findByLanguage(language);

        if (votoLanguage == null) {
            throw new EvsInitiateCallException("Cannot initiate call for Provider with id: %s, because Voto Language for language: %s not found",
                    subjectId, language.toString());
        }

        return votoLanguage.getVotoId();
    }

    private String getVotoMessageId(String messageKey, String subjectId) {
        VotoMessage votoMessage = votoMessageDataService.findByMessageKey(messageKey);

        if (votoMessage == null) {
            throw new EvsInitiateCallException("Cannot initiate call for Provider with id: %s, because Voto Message with key: %s not found",
                    subjectId, messageKey);
        }

        return votoMessage.getVotoIvrId();
    }

    @Autowired
    public void setOutboundCallService(OutboundCallService outboundCallService) {
        this.outboundCallService = outboundCallService;
    }
}
