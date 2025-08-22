package pe.edu.vallegrande.ms_distribution.application.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.edu.vallegrande.ms_distribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.ms_distribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.ms_distribution.infrastructure.repository.DistributionProgramRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DistributionProgramServiceImplTest {

    @Mock
    private DistributionProgramRepository programRepository;

    @InjectMocks
    private DistributionProgramServiceImpl distributionProgramService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Escenario de Validación:
     * Debe fallar cuando la fecha del programa no tiene un formato válido.
     */
    @Test
    void saveDistributionProgram_shouldReturnError_whenProgramDateIsInvalid() {
        System.out.println("➡️ Iniciando prueba: Fecha inválida en DistributionProgram");
        
        // Arrange - Construimos la solicitud
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("sch-1");
        request.setRouteId("route-1");
        request.setZoneId("zone-1");
        request.setOrganizationId("org-1");
        request.setStreetId("street-1");
        request.setProgramDate("invalid-date"); // Invalid date format
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("10:00");
        request.setActualStartTime(null);
        request.setActualEndTime(null);
        request.setStatus("PENDING");
        request.setResponsibleUserId("user-1");
        request.setObservations("Test observation");

        // Mock: generateNextProgramCode returns "PROG001"
        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(throwable -> {
                    System.out.println("❌ Error capturado: " + throwable.getMessage());
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().contains("Text 'invalid-date' could not be parsed");
                })
                .verify();

        // Verifica que los métodos del repositorio fueron llamados correctamente
        verify(programRepository, never()).save(any(DistributionProgram.class));
        System.out.println("✔️ Prueba completada: No se guardó el programa por fecha inválida\n");
    }

    /**
     * Escenario de Excepción:
     * Debe lanzar error cuando el repositorio falla al guardar el programa.
     */
    @Test
    void saveDistributionProgram_shouldReturnError_whenRepositoryFails() {
         System.out.println("➡️ Iniciando prueba: Falla del repositorio al guardar DistributionProgram");

        // Arrange - Construimos la solicitud
        DistributionProgramCreateRequest request = new DistributionProgramCreateRequest();
        request.setScheduleId("sch-1");
        request.setRouteId("route-1");
        request.setZoneId("zone-1");
        request.setOrganizationId("org-1");
        request.setStreetId("street-1");
        request.setProgramDate(LocalDate.now().toString());
        request.setPlannedStartTime("08:00");
        request.setPlannedEndTime("10:00");
        request.setActualStartTime(null);
        request.setActualEndTime(null);
        request.setStatus("PENDING");
        request.setResponsibleUserId("user-1");
        request.setObservations("Test observation");

        when(programRepository.findTopByOrderByProgramCodeDesc()).thenReturn(Mono.empty());
        when(programRepository.save(ArgumentMatchers.any(DistributionProgram.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        // Act & Assert - Ejecutamos el método y validamos la respuesta
        StepVerifier.create(distributionProgramService.save(request))
                .expectErrorSatisfies(throwable -> {
                    System.out.println("❌ Error capturado: " + throwable.getMessage());
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().contains("Database error");
                })
                .verify();

        System.out.println("✔️ Prueba completada: Se detectó correctamente el error del repositorio\n");
    }
}

    /**
     * mvn -Dtest=DistributionProgramServiceImplTest test
     */