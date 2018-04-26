package com.gabrielebonadiman.multitenancy.service.impl;

import com.gabrielebonadiman.multitenancy.client.UaaClient;
import com.gabrielebonadiman.multitenancy.domain.Event;
import com.gabrielebonadiman.multitenancy.repository.EventRepository;
import com.gabrielebonadiman.multitenancy.service.EventService;
import com.gabrielebonadiman.multitenancy.service.dto.EventDTO;
import com.gabrielebonadiman.multitenancy.service.mapper.EventMapper;
import com.gabrielebonadiman.multitenancy.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


/**
 * Service Implementation for managing Event.
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    private final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    private final UaaClient uaaClient;


    private final static String EXCEPTION_MESSAGE = "An error occurred with Event";
    private final static String EXCEPTION_ENTITY = "Event";
    private final static String EXCEPTION_HTTP_LABEL = "isBadRequest";

/*
    @GetMapping("/aa")
    public String asd(){
        return uaaClient.getUserCompanyId().toString();
    }
*/

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper, UaaClient uaaClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.uaaClient = uaaClient;
    }

    /**
     * Save a event.
     *
     * @param eventDTO the entity to save
     * @return the persisted entity
     */
    @Override
    public EventDTO save(EventDTO eventDTO) {
        UUID companyId =
            uaaClient.getUserCompanyId();
        if (companyId != null) {
            log.debug("Request to save Event : {}", eventDTO);
            eventDTO.setCompanyId(companyId);
            Event event = eventMapper.toEntity(eventDTO);
            event = eventRepository.save(event);
            return eventMapper.toDto(event);
        }
        else{
            log.debug("Unable to get the company ID");
            throw new BadRequestAlertException(EXCEPTION_MESSAGE,EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
        }
    }

    /**
     * Get all the events.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EventDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Events");
        UUID companyId =
            uaaClient.getUserCompanyId();
        if (companyId != null) {
            return eventRepository.findAllByCompanyId(pageable, companyId)
                .map(eventMapper::toDto);
        } else {
            log.debug("Unable to get the company ID");
            throw new BadRequestAlertException(EXCEPTION_MESSAGE, EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
        }
    }

    /**
     * Get one event by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public EventDTO findOne(UUID id) {
        log.debug("Request to get Event : {}", id);
        UUID companyId =
            uaaClient.getUserCompanyId();
        if (companyId != null) {
            Event event = eventRepository.findOne(id);
            if (event != null) {
                if (event.getCompanyId() != companyId) {
                    log.error("The system has been compromised. Request to fetch from database an event" +
                        " that does not belong to the user: {} using companyID", id, companyId);
                    throw new SecurityException();
                }else{
                    log.debug("Request to delete Event : {}", id);
                    return eventMapper.toDto(event);
                }
            }
            else{
                log.debug("Unable to find an event with UUID: {}", id);
                throw new BadRequestAlertException(EXCEPTION_MESSAGE, EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
            }
        }
        else {
            log.debug("Unable to get the company ID");
            throw new BadRequestAlertException(EXCEPTION_MESSAGE, EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
        }
    }

    /**
     * Delete the event by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(UUID id) {
        UUID companyId =
            uaaClient.getUserCompanyId();
        if (companyId != null) {
            Event event = eventRepository.findOne(id);
            if (event != null) {
                if (event.getCompanyId() != companyId){
                    log.error("The system has been compromised. Request to fetch from database an event" +
                        " that does not belong to the user: {} using companyID", id, companyId);
                    throw new SecurityException();
                }
                else{
                    log.debug("Request to delete Event : {}", id);
                    eventRepository.delete(id);
                }
            }
            else{
                log.debug("Unable to find an event with UUID: {}", id);
                throw new BadRequestAlertException(EXCEPTION_MESSAGE, EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
            }
        }
        else{
            log.debug("Unable to get the company ID");
            throw new BadRequestAlertException(EXCEPTION_MESSAGE, EXCEPTION_ENTITY, EXCEPTION_HTTP_LABEL);
        }
    }
}











