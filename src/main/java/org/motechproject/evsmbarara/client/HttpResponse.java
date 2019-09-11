package org.motechproject.evsmbarara.client;

import lombok.Getter;
import lombok.Setter;

public class HttpResponse {

    @Getter
    @Setter
    private int status;

    @Getter
    @Setter
    private String responseBody;

    @Getter
    @Setter
    private String contentType;
}
