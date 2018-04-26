package com.gabrielebonadiman.multitenancy.repository;

import com.gabrielebonadiman.multitenancy.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.UUID;


/**
 * Spring Data JPA repository for the Event entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findAllByCompanyId(Pageable pageable, UUID companyId);

    Event findOneByIdAndCompanyId(UUID id, UUID companyId);

}
