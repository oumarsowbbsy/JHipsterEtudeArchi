package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Personne;
import com.mycompany.myapp.service.dto.PersonneDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Personne} and its DTO {@link PersonneDTO}.
 */
@Mapper(componentModel = "spring")
public interface PersonneMapper extends EntityMapper<PersonneDTO, Personne> {}
