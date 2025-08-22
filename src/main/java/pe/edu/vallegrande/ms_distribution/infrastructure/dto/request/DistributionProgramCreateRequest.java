package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionProgramCreateRequest {

    private String organizationId;
    private String programCode;
    private String scheduleId;
    private String routeId; 
    private String zoneId;              
    private String streetId;     
    private String programDate; // formato: yyyy-MM-dd
    private String plannedStartTime; // HH:mm
    private String plannedEndTime;   // HH:mm
    private String actualStartTime;  // HH:mm
    private String actualEndTime;    // HH:mm
    private String status;
    private String responsibleUserId;
    private String observations;
}
