package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.PersonneDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Personne}.
 */
public interface PersonneService {
    /**
     * Save a personne.
     *
     * @param personneDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<PersonneDTO> save(PersonneDTO personneDTO);

    /**
     * Updates a personne.
     *
     * @param personneDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<PersonneDTO> update(PersonneDTO personneDTO);

    /**
     * Partially updates a personne.
     *
     * @param personneDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<PersonneDTO> partialUpdate(PersonneDTO personneDTO);

    /**
     * Get all the personnes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<PersonneDTO> findAll(Pageable pageable);

    /**
     * Returns the number of personnes available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Returns the number of personnes available in search repository.
     *
     */
    Mono<Long> searchCount();

    /**
     * Get the "id" personne.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<PersonneDTO> findOne(String id);

    /**
     * Delete the "id" personne.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(String id);

    /**
     * Search for the personne corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<PersonneDTO> search(String query, Pageable pageable);
}
