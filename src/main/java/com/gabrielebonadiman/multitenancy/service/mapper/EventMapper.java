package com.gabrielebonadiman.multitenancy.service.mapper;

import com.gabrielebonadiman.multitenancy.domain.Event;
import com.gabrielebonadiman.multitenancy.service.dto.EventDTO;
import org.mapstruct.Mapper;

import java.util.UUID;

/**
 * Mapper for the entity Event and its DTO EventDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EventMapper extends EntityMapper<EventDTO, Event> {



    default Event fromId(UUID id) {
        if (id == null) {
            return null;
        }
        Event event = new Event();
        event.setId(id);
        return event;
    }
}
