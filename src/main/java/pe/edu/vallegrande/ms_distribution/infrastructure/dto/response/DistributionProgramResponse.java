package pe.edu.vallegrande.ms_distribution.infrastructure.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionProgramResponse {

    private String id;
    private String organizationId;
    private String zoneId;          
    private String streetId; 

    private String programCode;
    private String scheduleId;
    private String routeId;

    private String programDate;         // Formato: yyyy-MM-dd
    private String plannedStartTime;    // Formato: HH:mm
    private String plannedEndTime;
    private String actualStartTime;
    private String actualEndTime;

    private String status;
    private String responsibleUserId;
    private String observations;
    private String createdAt;           // Formato ISO
}
