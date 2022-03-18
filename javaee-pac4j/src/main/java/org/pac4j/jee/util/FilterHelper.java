package org.pac4j.jee.util;

import org.pac4j.core.exception.TechnicalException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.pac4j.core.util.CommonHelper.*;

/**
 * Helper to define filter mappings.
 *
 * @author Jerome Leleu
 * @since 3.0.0
 */
public class FilterHelper {

    private final ServletContext servletContext;

    public FilterHelper(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Add a filter mapping.
     *
     * @param name the name fo the filter
     * @param filter the filter
     * @param parameters the URLs on which it applies and the supported dispatcher types
     */
    public void addFilterMapping(final String name, final Filter filter, final Object... parameters) {
        assertNotBlank("name", name);
        assertNotNull("filter", filter);
        assertNotNull("parameters", parameters);

        final List<String> urls = new ArrayList<>();
        final List<DispatcherType> types = new ArrayList<>();
        for (final Object parameter : parameters) {
            if (parameter instanceof String) {
                urls.add((String) parameter);
            } else if (parameter instanceof DispatcherType) {
                types.add((DispatcherType) parameter);
            } else {
                throw new TechnicalException("Unsupported parameter type: " + parameter);
            }
        }
        if (urls.isEmpty()) {
            throw new TechnicalException("No URL mapping defined for filter: " + name);
        }
        if (types.isEmpty()) {
            types.add(DispatcherType.REQUEST);
        }

        final FilterRegistration.Dynamic registration = servletContext.addFilter(name, filter);
        registration.addMappingForUrlPatterns(EnumSet.copyOf(types), true, urls.toArray(new String[urls.size()]));
    }
}
