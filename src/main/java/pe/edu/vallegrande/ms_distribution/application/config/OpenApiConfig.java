package pe.edu.vallegrande.ms_distribution.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para el microservicio de usuarios y
 * autenticación
 */
@Configuration
public class OpenApiConfig {

     @Bean
     public OpenAPI customOpenAPI() {
          return new OpenAPI()
                    .info(new Info()
                              .title("VG Microservicio de Distribucion de Agua")
                              .description("API para gestión de Distribucion de Agua del sistema JASS Digital. " +
                                        "Disponible en: https://lab.vallegrande.edu.pe/jass/ms-inventory/")
                              .version("2.0.0")
                              .contact(new Contact()
                                        .name("Valle Grande")
                                        .email("soporte@vallegrande.edu.pe")
                                        .url("https://vallegrande.edu.pe"))
                              .license(new License()
                                        .name("MIT License")
                                        .url("https://opensource.org/licenses/MIT")));
     }
}