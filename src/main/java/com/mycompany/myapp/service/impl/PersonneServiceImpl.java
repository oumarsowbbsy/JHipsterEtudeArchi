package com.mycompany.myapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.Personne;
import com.mycompany.myapp.repository.PersonneRepository;
import com.mycompany.myapp.repository.search.PersonneSearchRepository;
import com.mycompany.myapp.service.PersonneService;
import com.mycompany.myapp.service.dto.PersonneDTO;
import com.mycompany.myapp.service.mapper.PersonneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Personne}.
 */
@Service
public class PersonneServiceImpl implements PersonneService {

    private final Logger log = LoggerFactory.getLogger(PersonneServiceImpl.class);

    private final PersonneRepository personneRepository;

    private final PersonneMapper personneMapper;

    private final PersonneSearchRepository personneSearchRepository;

    public PersonneServiceImpl(
        PersonneRepository personneRepository,
        PersonneMapper personneMapper,
        PersonneSearchRepository personneSearchRepository
    ) {
        this.personneRepository = personneRepository;
        this.personneMapper = personneMapper;
        this.personneSearchRepository = personneSearchRepository;
    }

    @Override
    public Mono<PersonneDTO> save(PersonneDTO personneDTO) {
        log.debug("Request to save Personne : {}", personneDTO);
        return personneRepository
            .save(personneMapper.toEntity(personneDTO))
            .flatMap(personneSearchRepository::save)
            .map(personneMapper::toDto);
    }

    @Override
    public Mono<PersonneDTO> update(PersonneDTO personneDTO) {
        log.debug("Request to save Personne : {}", personneDTO);
        return personneRepository
            .save(personneMapper.toEntity(personneDTO))
            .flatMap(personneSearchRepository::save)
            .map(personneMapper::toDto);
    }

    @Override
    public Mono<PersonneDTO> partialUpdate(PersonneDTO personneDTO) {
        log.debug("Request to partially update Personne : {}", personneDTO);

        return personneRepository
            .findById(personneDTO.getId())
            .map(existingPersonne -> {
                personneMapper.partialUpdate(existingPersonne, personneDTO);

                return existingPersonne;
            })
            .flatMap(personneRepository::save)
            .flatMap(savedPersonne -> {
                personneSearchRepository.save(savedPersonne);

                return Mono.just(savedPersonne);
            })
            .map(personneMapper::toDto);
    }

    @Override
    public Flux<PersonneDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Personnes");
        return personneRepository.findAllBy(pageable).map(personneMapper::toDto);
    }

    public Mono<Long> countAll() {
        return personneRepository.count();
    }

    public Mono<Long> searchCount() {
        return personneSearchRepository.count();
    }

    @Override
    public Mono<PersonneDTO> findOne(String id) {
        log.debug("Request to get Personne : {}", id);
        return personneRepository.findById(id).map(personneMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Personne : {}", id);
        return personneRepository.deleteById(id).then(personneSearchRepository.deleteById(id));
    }

    @Override
    public Flux<PersonneDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Personnes for query {}", query);
        return personneSearchRepository.search(query, pageable).map(personneMapper::toDto);
    }
}
