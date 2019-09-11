package org.motechproject.evsmbarara.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.evsmbarara.client.HttpResponse;
import org.motechproject.evsmbarara.client.EvsMbararaHttpClient;
import org.motechproject.evsmbarara.domain.Subject;
import org.motechproject.evsmbarara.service.SubjectService;
import org.motechproject.evsmbarara.service.ZetesService;
import org.motechproject.evsmbarara.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("zetesService")
public class ZetesServiceImpl implements ZetesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZetesServiceImpl.class);

    private SubjectService subjectService;

    private EvsMbararaHttpClient evsMbararaHttpClient;

    @Override
    public void sendUpdatedSubjects(String zetesUrl, String username, String password) {
        LOGGER.info("Sending updated subjects to zetes. Job started at {}", DateTime.now());

        List<Subject> modifiedSubjects = subjectService.findModifiedSubjects();
        for (Subject s : modifiedSubjects) {
            String json = JsonUtils.convertSubjectForZetes(s);
            if (json != null) {
                HttpResponse response = evsMbararaHttpClient
                    .sendJson(zetesUrl, json, username, password);
                if (response == null) {
                    LOGGER.error("Skipping subject due to HttpClient failure. Subject id: {}", s.getSubjectId());
                } else if (response.getStatus() != HttpStatus.SC_NO_CONTENT) {
                    LOGGER.error("Failed to update the subject with id: {}. Response from Zetes (status {}):\n{}",
                            s.getSubjectId(), response.getStatus(), parseZetesResponse(response));
                } else {
                    // the subject has been updated successfully
                    s.setChanged(false);
                    subjectService.update(s);
                    LOGGER.debug("Update to Zetes was successful. Subject id: {}: \n", s.getSubjectId(), s.toString());
                }
            } else {
                LOGGER.error("Skipping subject due to json processing exception. Subject id: {}", s.getSubjectId());
            }
        }
        LOGGER.info("Zetes update job finished at {}", DateTime.now());
    }

    private String parseZetesResponse(HttpResponse httpResponse) {
        int status = httpResponse.getStatus();
        if (status == HttpStatus.SC_NOT_FOUND) {
            return "Invalid reverse proxy url";
        } else if (status == HttpStatus.SC_UNAUTHORIZED) {
            return "Bad authentication";
        } else {
            String response = httpResponse.getResponseBody();
            if (StringUtils.isEmpty(response)) {
                return "Empty response body with status different than 204";
            }
            if ("application/json".equals(httpResponse.getContentType())) {
                try {
                    JsonElement jsonElement = new JsonParser().parse(response);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (jsonObject.has("message")) {
                        return jsonObject.get("message").getAsString();
                    } else {
                        return jsonObject.getAsString();
                    }
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Could not parse JSON response from Zetes");
                }
            } else if (response.contains("JsonParseException")) {
                return "Invalid JSON syntax";
            }
            return response;
        }
    }

    @Autowired
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Autowired
    public void setEvsMbararaHttpClient(EvsMbararaHttpClient evsMbararaHttpClient) {
        this.evsMbararaHttpClient = evsMbararaHttpClient;
    }
}
