package com.gabrielebonadiman.multitenancy.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

/**
 * Interface to connect core-microservice with the UAA server
 */
@AuthorizedFeignClient(name = "uaa")
public interface UaaClient{

    /**
     * Send Get request to UAA server
     * @return user company ID
     */
    @RequestMapping(value = "/api/users/company", method = RequestMethod.GET)
    UUID getUserCompanyId();

}
