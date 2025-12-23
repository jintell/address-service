package org.meldtech.platform.shared.integration.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: Josiah Adetayo
 * @Email: josleke@gmail.com, josiah.adetayo@meld-tech.com
 */
@Data
public class ClientError {
    @JsonProperty("error_description")
    private String errorDescription;
    private String error;
    @JsonProperty("error_uri")
    private String errorUri;
}
