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
@Document(collection = "schedules")
public class DistributionSchedule {

    @Id
    private String id;

    private String organizationId;
    private String scheduleCode;
    private String zoneId;
    private String scheduleName;

    private List<String> daysOfWeek;
    private String startTime;     // formato: "HH:mm"
    private String endTime;       // formato: "HH:mm"
    private int durationHours;

    private String status;
    private Instant createdAt;
}
