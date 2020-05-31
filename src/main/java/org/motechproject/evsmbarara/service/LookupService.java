package org.motechproject.evsmbarara.service;

import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.evsmbarara.web.domain.GridSettings;
import org.motechproject.evsmbarara.web.domain.Records;

import java.util.List;

public interface LookupService {

    <T> List<T> getEntities(Class<T> entityType, GridSettings settings, QueryParams queryParams);

    <T> Records<T> getEntities(Class<T> entityType, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> Records<T> getEntities(Class<T> entityDtoType, Class<?> entityType, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> Records<T> getEntities(String entityClassName, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> long getEntitiesCount(Class<T> entityType, String lookup, String lookupFields);

    <T> long getEntitiesCount(String entityClassName, String lookup, String lookupFields);

    List<LookupDto> getAvailableLookups(String entityName);
}
