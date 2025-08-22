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
public class DistributionScheduleResponse {

    private String id;
    private String organizationId;

    private String scheduleCode;
    private String zoneId;
    private String scheduleName;

    private List<String> daysOfWeek;
    private String startTime;   // Formato HH:mm
    private String endTime;     // Formato HH:mm
    private int durationHours;

    private String status;
    private Instant createdAt;
}
