package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.PersonneRepository;
import com.mycompany.myapp.service.PersonneService;
import com.mycompany.myapp.service.dto.PersonneDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Personne}.
 */
@RestController
@RequestMapping("/api")
public class PersonneResource {

    private final Logger log = LoggerFactory.getLogger(PersonneResource.class);

    private static final String ENTITY_NAME = "personne";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PersonneService personneService;

    private final PersonneRepository personneRepository;

    public PersonneResource(PersonneService personneService, PersonneRepository personneRepository) {
        this.personneService = personneService;
        this.personneRepository = personneRepository;
    }

    /**
     * {@code POST  /personnes} : Create a new personne.
     *
     * @param personneDTO the personneDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new personneDTO, or with status {@code 400 (Bad Request)} if the personne has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/personnes")
    public Mono<ResponseEntity<PersonneDTO>> createPersonne(@Valid @RequestBody PersonneDTO personneDTO) throws URISyntaxException {
        log.debug("REST request to save Personne : {}", personneDTO);
        if (personneDTO.getId() != null) {
            throw new BadRequestAlertException("A new personne cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return personneService
            .save(personneDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/personnes/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /personnes/:id} : Updates an existing personne.
     *
     * @param id the id of the personneDTO to save.
     * @param personneDTO the personneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated personneDTO,
     * or with status {@code 400 (Bad Request)} if the personneDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the personneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/personnes/{id}")
    public Mono<ResponseEntity<PersonneDTO>> updatePersonne(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody PersonneDTO personneDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Personne : {}, {}", id, personneDTO);
        if (personneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, personneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return personneRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return personneService
                    .update(personneDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /personnes/:id} : Partial updates given fields of an existing personne, field will ignore if it is null
     *
     * @param id the id of the personneDTO to save.
     * @param personneDTO the personneDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated personneDTO,
     * or with status {@code 400 (Bad Request)} if the personneDTO is not valid,
     * or with status {@code 404 (Not Found)} if the personneDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the personneDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/personnes/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<PersonneDTO>> partialUpdatePersonne(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody PersonneDTO personneDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Personne partially : {}, {}", id, personneDTO);
        if (personneDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, personneDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return personneRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<PersonneDTO> result = personneService.partialUpdate(personneDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /personnes} : get all the personnes.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of personnes in body.
     */
    @GetMapping("/personnes")
    public Mono<ResponseEntity<List<PersonneDTO>>> getAllPersonnes(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Personnes");
        return personneService
            .countAll()
            .zipWith(personneService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /personnes/:id} : get the "id" personne.
     *
     * @param id the id of the personneDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the personneDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/personnes/{id}")
    public Mono<ResponseEntity<PersonneDTO>> getPersonne(@PathVariable String id) {
        log.debug("REST request to get Personne : {}", id);
        Mono<PersonneDTO> personneDTO = personneService.findOne(id);
        return ResponseUtil.wrapOrNotFound(personneDTO);
    }

    /**
     * {@code DELETE  /personnes/:id} : delete the "id" personne.
     *
     * @param id the id of the personneDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/personnes/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deletePersonne(@PathVariable String id) {
        log.debug("REST request to delete Personne : {}", id);
        return personneService
            .delete(id)
            .map(result ->
                ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
            );
    }

    /**
     * {@code SEARCH  /_search/personnes?query=:query} : search for the personne corresponding
     * to the query.
     *
     * @param query the query of the personne search.
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the result of the search.
     */
    @GetMapping("/_search/personnes")
    public Mono<ResponseEntity<Flux<PersonneDTO>>> searchPersonnes(
        @RequestParam String query,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to search for a page of Personnes for query {}", query);
        return personneService
            .searchCount()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
            .map(headers -> ResponseEntity.ok().headers(headers).body(personneService.search(query, pageable)));
    }
}
