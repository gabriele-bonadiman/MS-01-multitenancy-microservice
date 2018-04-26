package com.gabrielebonadiman.multitenancy.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gabrielebonadiman.multitenancy.service.EventService;
import com.gabrielebonadiman.multitenancy.service.dto.EventDTO;
import com.gabrielebonadiman.multitenancy.web.rest.errors.BadRequestAlertException;
import com.gabrielebonadiman.multitenancy.web.rest.util.HeaderUtil;
import com.gabrielebonadiman.multitenancy.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing Event.
 */
@RestController
@RequestMapping("/api")
public class EventResource {

    private final Logger log = LoggerFactory.getLogger(EventResource.class);

    private static final String ENTITY_NAME = "event";

    private final EventService eventService;
    private final static String EXCEPTION_MESSAGE = "An error occurred with Event";
    private final static String EXCEPTION_ENTITY = "Event";
    private final static String EXCEPTION_HTTP_LABEL = "isBadRequest";

    public EventResource(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * POST  /events : Create a new event.
     *
     * @param eventDTO the eventDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new eventDTO, or with status 400 (Bad Request) if the event has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/events")
    @Timed
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) throws URISyntaxException {
        log.debug("REST request to save Event : {}", eventDTO);
        if (eventDTO.getId() != null) {
            throw new BadRequestAlertException("A new event cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (eventDTO.getCompanyId() != null){
            log.debug("Company ID has to be set by the system and not by the user");
            throw new BadRequestAlertException(EXCEPTION_MESSAGE,EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
        }
        EventDTO result = eventService.save(eventDTO);
        return ResponseEntity.created(new URI("/api/events/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /events : Updates an existing event.
     *
     * @param eventDTO the eventDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated eventDTO,
     * or with status 400 (Bad Request) if the eventDTO is not valid,
     * or with status 500 (Internal Server Error) if the eventDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/events")
    @Timed
    public ResponseEntity<EventDTO> updateEvent(@Valid @RequestBody EventDTO eventDTO) throws URISyntaxException {
        log.debug("REST request to update Event : {}", eventDTO);
        if (eventDTO.getId() == null) {
            return createEvent(eventDTO);
        }
        EventDTO result = eventService.save(eventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, eventDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /events : get all the events.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of events in body
     */
    @GetMapping("/events")
    @Timed
    public ResponseEntity<List<EventDTO>> getAllEvents(Pageable pageable) {
        log.debug("REST request to get a page of Events");
        Page<EventDTO> page = eventService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /events/:id : get the "id" event.
     *
     * @param id the id of the eventDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the eventDTO, or with status 404 (Not Found)
     */
    @GetMapping("/events/{id}")
    @Timed
    public ResponseEntity<EventDTO> getEvent(@PathVariable UUID id) {
        log.debug("REST request to get Event : {}", id);
        EventDTO eventDTO = eventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(eventDTO));
    }

    /**
     * DELETE  /events/:id : delete the "id" event.
     *
     * @param id the id of the eventDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/events/{id}")
    @Timed
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        log.debug("REST request to delete Event : {}", id);
        eventService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
