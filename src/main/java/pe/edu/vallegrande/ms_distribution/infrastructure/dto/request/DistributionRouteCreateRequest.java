package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributionRouteCreateRequest {

    private String organizationId;
        private String routeCode;
            private String routeName;
                private List<ZoneEntry> zones;
                    private Integer totalEstimatedDuration; // en horas
                        private String responsibleUserId;

                            @Data
                                @NoArgsConstructor
                                    @AllArgsConstructor
                                        public static class ZoneEntry {
                                                private String zoneId;
                                                        private Integer order;
                                                                private Integer estimatedDuration; // en horas
                                                                    }
                                                                    }
                                                                    