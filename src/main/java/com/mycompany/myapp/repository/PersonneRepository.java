package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Personne;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data MongoDB reactive repository for the Personne entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PersonneRepository extends ReactiveMongoRepository<Personne, String> {
    Flux<Personne> findAllBy(Pageable pageable);
}
