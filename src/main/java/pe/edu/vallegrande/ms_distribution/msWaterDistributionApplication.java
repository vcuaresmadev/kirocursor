package pe.edu.vallegrande.ms_distribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@SpringBootApplication
@OpenAPIDefinition
public class msWaterDistributionApplication {

    public static void main(String[] args) {
        SpringApplication.run(msWaterDistributionApplication.class, args);
    }
}