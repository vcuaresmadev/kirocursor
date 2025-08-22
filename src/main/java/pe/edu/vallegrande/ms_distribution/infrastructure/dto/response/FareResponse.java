package pe.edu.vallegrande.ms_distribution.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareResponse {
    private String id;
    private String organizationId;

    private String fareCode;
    private String fareName;
    private String fareType;

    private BigDecimal fareAmount;

    private String status;
    private Instant createdAt;
}
