package org.meldtech.platform.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.geocoding")
public class GeoCodingProperties {
    String url;
    String reverse;
}
