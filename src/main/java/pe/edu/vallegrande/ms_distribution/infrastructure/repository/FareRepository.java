package pe.edu.vallegrande.ms_distribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.ms_distribution.domain.models.Fare;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FareRepository extends ReactiveMongoRepository<Fare, String> {

    Flux<Fare> findAllByStatus(String status);

    Mono<Boolean> existsByFareCode(String fareCode);

    Mono<Fare> findTopByOrderByFareCodeDesc(); // <- CORRECTO
}
