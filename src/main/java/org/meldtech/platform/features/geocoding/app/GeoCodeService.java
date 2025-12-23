package org.meldtech.platform.features.geocoding.app;

import org.meldtech.platform.shared.config.GeoCodingProperties;
import org.meldtech.platform.shared.integration.client.HttpConnectorService;
import org.springframework.stereotype.Service;

import org.meldtech.platform.features.geocoding.api.GeoCodingDto.GeoCodingResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GeoCodeService {
    private final HttpConnectorService connectorService;
    private final GeoCodingProperties props;

    private static final double EARTH_RADIUS_KM = 6371.0;

    public GeoCodeService(HttpConnectorService connectorService,
                          GeoCodingProperties props) {
        this.connectorService = connectorService;
        this.props = props;
    }

    public Mono<GeoCodingResponse> geoCode(String address) {
        if (address == null || address.isBlank()) return Mono.empty();
        return connectorService.get(String.format(props.getUrl(), address), headers(), GeoCodingResponse[].class)
                .map(response -> response[0])
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Map> getReverseGeoCode(double lat, double lon) {
        if (lat == 0 && lon == 0) return Mono.empty();
        return connectorService.get(String.format(props.getReverse(), lat, lon), headers(), Map.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Double> getDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 && lon1 == 0 || lat2 == 0 && lon2 == 0) return Mono.empty();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return Mono.just(EARTH_RADIUS_KM * c);
    }

    public Mono<Double> getDistanceBetween(String address1, String address2) {
        if (address1 == null || address1.isBlank() || address2 == null || address2.isBlank()) return Mono.empty();
        Mono<GeoCodingResponse> location1 = geoCode(address1);
        Mono<GeoCodingResponse> location2 = geoCode(address2);

        return Mono.zip(location1, location2)
                .flatMap(tuple -> {
                    GeoCodingResponse loc1 = tuple.getT1();
                    GeoCodingResponse loc2 = tuple.getT2();

                    return getDistance(
                            Double.parseDouble(loc1.latitude()),
                            Double.parseDouble(loc1.longitude()),
                            Double.parseDouble(loc2.latitude()),
                            Double.parseDouble(loc2.longitude())
                    );
                });
    }

    private Map<String, String> headers() {
        return Map.of("Accept", "application/json", "User-Agent", "User-Onboarding-1.0");
    }
}
