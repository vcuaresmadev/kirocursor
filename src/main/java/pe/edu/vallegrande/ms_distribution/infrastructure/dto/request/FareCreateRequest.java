package pe.edu.vallegrande.ms_distribution.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareCreateRequest {

    private String organizationId;
    private String fareCode;
    private String fareName;
    private String fareType; // DIARIA, SEMANAL, MENSUAL
    private BigDecimal fareAmount;
}
