package pe.edu.vallegrande.ms_distribution.domain.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "routes")
public class DistributionRoute {

    @Id
    private String id;

    private String organizationId;

    private String routeCode;
    private String routeName;

    private List<ZoneOrder> zones;

    private int totalEstimatedDuration; // en horas

    private String responsibleUserId;

    private String status;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneOrder {
        private String zoneId;
        private int order;
        private int estimatedDuration; // en horas
    }
}
