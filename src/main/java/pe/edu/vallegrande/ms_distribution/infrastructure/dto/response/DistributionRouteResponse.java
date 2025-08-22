package pe.edu.vallegrande.ms_distribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionRouteResponse {

    private String id;
    private String organizationId;
    private String routeCode;
    private String routeName;
    private List<ZoneDetail> zones;
    private Integer totalEstimatedDuration;
    private String responsibleUserId;
    private String status;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneDetail {
        private String zoneId;
        private Integer order;
        private Integer estimatedDuration;
    }
}
