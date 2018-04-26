package com.gabrielebonadiman.multitenancy.web.rest;

import com.gabrielebonadiman.multitenancy.MultitenancyApp;
import com.gabrielebonadiman.multitenancy.client.UaaClient;
import com.gabrielebonadiman.multitenancy.config.SecurityBeanOverrideConfiguration;
import com.gabrielebonadiman.multitenancy.domain.Event;
import com.gabrielebonadiman.multitenancy.repository.EventRepository;
import com.gabrielebonadiman.multitenancy.service.EventService;
import com.gabrielebonadiman.multitenancy.service.dto.EventDTO;
import com.gabrielebonadiman.multitenancy.service.mapper.EventMapper;
import com.gabrielebonadiman.multitenancy.web.rest.errors.BadRequestAlertException;
import com.gabrielebonadiman.multitenancy.web.rest.errors.ExceptionTranslator;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.gabrielebonadiman.multitenancy.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the EventResource REST controller.
 *
 * @see EventResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MultitenancyApp.class, SecurityBeanOverrideConfiguration.class})
public class EventResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Date DEFAULT_DATE = new Date();
    private static final Date UPDATED_DATE = new Date();

    private static final String DEFAULT_HOST = "AAAAAAAAAA";
    private static final String UPDATED_HOST = "BBBBBBBBBB";

    private static final UUID DEFAULT_EVENT_ID = UUID.randomUUID();
    private static final UUID UPDATED_EVENT_ID= UUID.randomUUID();

    private static final UUID DEFAULT_COMPANY_ID = UUID.randomUUID();
    private static final UUID UPDATED_COMPANY_ID = UUID.randomUUID();

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @MockBean
    private UaaClient uaaClient;

    private MockMvc restEventMockMvc;

    private Event event;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final EventResource eventResource = new EventResource(eventService);
        given(uaaClient.getUserCompanyId()).willReturn(DEFAULT_COMPANY_ID);
        this.restEventMockMvc = MockMvcBuilders.standaloneSetup(eventResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Event createEntity(EntityManager em) {
        Event event = new Event()
            .name(DEFAULT_NAME)
            .date(DEFAULT_DATE)
            .host(DEFAULT_HOST);
        return event;
    }

    @Before
    public void initTest() {
        event = createEntity(em);
    }

    @Test
    @Transactional
    public void createEvent() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);
        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isCreated());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate + 1);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testEvent.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testEvent.getHost()).isEqualTo(DEFAULT_HOST);
        assertThat(testEvent.getCompanyId()).isEqualTo(DEFAULT_COMPANY_ID);
    }

    @Test
    @Transactional
    public void createEventWithUaaReturnNullCompanyId() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        given(uaaClient.getUserCompanyId()).willReturn(null);
        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);
        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().is4xxClientError());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createEventWithCompanyIdNotNull() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();
        event.setCompanyId(DEFAULT_COMPANY_ID);
        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);
        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().is4xxClientError());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setName(null);

        // Create the Event, which fails.
        EventDTO eventDTO = eventMapper.toDto(event);

        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isBadRequest());

        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = eventRepository.findAll().size();
        // set the field null
        event.setDate(null);

        // Create the Event, which fails.
        EventDTO eventDTO = eventMapper.toDto(event);

        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isBadRequest());

        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void updateNonExistingEvent() throws Exception {
        int databaseSizeBeforeUpdate = eventRepository.findAll().size();

        // Create the Event
        EventDTO eventDTO = eventMapper.toDto(event);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restEventMockMvc.perform(put("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isCreated());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Event.class);
        Event event1 = new Event();
        event1.setId(DEFAULT_EVENT_ID);
        Event event2 = new Event();
        event2.setId(event1.getId());
        assertThat(event1).isEqualTo(event2);
        event2.setId(UPDATED_EVENT_ID);
        assertThat(event1).isNotEqualTo(event2);
        event1.setId(null);
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventDTO.class);
        EventDTO eventDTO1 = new EventDTO();
        eventDTO1.setId(DEFAULT_EVENT_ID);
        EventDTO eventDTO2 = new EventDTO();
        assertThat(eventDTO1).isNotEqualTo(eventDTO2);
        eventDTO2.setId(eventDTO1.getId());
        assertThat(eventDTO1).isEqualTo(eventDTO2);
        eventDTO2.setId(UPDATED_EVENT_ID);
        assertThat(eventDTO1).isNotEqualTo(eventDTO2);
        eventDTO1.setId(null);
        assertThat(eventDTO1).isNotEqualTo(eventDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(eventMapper.fromId(DEFAULT_EVENT_ID).getId()).isEqualTo(DEFAULT_EVENT_ID);
        assertThat(eventMapper.fromId(null)).isNull();
    }

    @Test
    @Transactional
    public void getEvent() throws Exception {
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(event.getId().toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.host").value(DEFAULT_HOST))
            .andExpect(jsonPath("$.companyId").value(DEFAULT_COMPANY_ID.toString()));
    }

    @Test
    @Transactional
    public void getEventUaaReturnNullCompanyId() throws Exception {
        // Initialize the database
        given(uaaClient.getUserCompanyId()).willReturn(null);
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void getEventThatDoesNotExists() throws Exception {
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", UPDATED_EVENT_ID))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void getEventWithDifferentCompanyId() throws Exception {

        given(uaaClient.getUserCompanyId()).willReturn(UPDATED_COMPANY_ID);
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);

        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", event.getId()))
            .andExpect(status().is5xxServerError());
    }


    @Test
    @Transactional
    public void deleteEvent() throws Exception {
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);
        int databaseSizeBeforeDelete = eventRepository.findAll().size();

        // Get the event
        restEventMockMvc.perform(delete("/api/events/{id}", event.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void deleteEventWithUaaReturnNullCompanyId() throws Exception {

        given(uaaClient.getUserCompanyId()).willReturn(null);
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);
        int databaseSizeBeforeDelete = eventRepository.findAll().size();

        // Get the event
        restEventMockMvc.perform(delete("/api/events/{id}", event.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().is4xxClientError());

    }

    @Test
    @Transactional
    public void deleteEventWithDifferentCompanyId() throws Exception {
        given(uaaClient.getUserCompanyId()).willReturn(UPDATED_COMPANY_ID);
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);
        int databaseSizeBeforeDelete = eventRepository.findAll().size();

        // Get the event
        restEventMockMvc.perform(delete("/api/events/{id}", event.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().is5xxServerError());

    }




    @Test
    @Transactional
    public void createEventWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = eventRepository.findAll().size();

        // Create the Event with an existing ID
        event.setId(DEFAULT_EVENT_ID);
        EventDTO eventDTO = eventMapper.toDto(event);

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventMockMvc.perform(post("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllEvents() throws Exception {
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);

        // Get all the eventList
        restEventMockMvc.perform(get("/api/events?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].host").value(hasItem(DEFAULT_HOST)))
            .andExpect(jsonPath("$.[*].companyId").value(hasItem(DEFAULT_COMPANY_ID.toString())));
    }

    @Test
    @Transactional
    public void getNonExistingEvent() throws Exception {
        // Get the event
        restEventMockMvc.perform(get("/api/events/{id}", DEFAULT_EVENT_ID))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void updateEvent() throws Exception {
        // Initialize the database
        event.setCompanyId(DEFAULT_COMPANY_ID);
        eventRepository.saveAndFlush(event);
        int databaseSizeBeforeUpdate = eventRepository.findAll().size();

        // Update the event
        Event updatedEvent = eventRepository.findOne(event.getId());
        // Disconnect from session so that the updates on updatedEvent are not directly saved in db
        em.detach(updatedEvent);
        updatedEvent
            .name(UPDATED_NAME)
            .date(UPDATED_DATE)
            .host(UPDATED_HOST);
        EventDTO eventDTO = eventMapper.toDto(updatedEvent);

        restEventMockMvc.perform(put("/api/events")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(eventDTO)))
            .andExpect(status().isOk());

        // Validate the Event in the database
        List<Event> eventList = eventRepository.findAll();
        assertThat(eventList).hasSize(databaseSizeBeforeUpdate);
        Event testEvent = eventList.get(eventList.size() - 1);
        assertThat(testEvent.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testEvent.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testEvent.getHost()).isEqualTo(UPDATED_HOST);
    }



}
