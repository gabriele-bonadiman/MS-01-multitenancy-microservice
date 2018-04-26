package com.gabrielebonadiman.multitenancy.service;

import com.gabrielebonadiman.multitenancy.service.dto.EventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service Interface for managing Event.
 */
public interface EventService {

    /**
     * Save a event.
     *
     * @param eventDTO the entity to save
     * @return the persisted entity
     */
    EventDTO save(EventDTO eventDTO);

    /**
     * Get all the events.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    Page<EventDTO> findAll(Pageable pageable);

    /**
     * Get the "id" event.
     *
     * @param id the id of the entity
     * @return the entity
     */
    EventDTO findOne(UUID id);

    /**
     * Delete the "id" event.
     *
     * @param id the id of the entity
     */
    void delete(UUID id);
}
