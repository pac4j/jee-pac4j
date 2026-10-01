package org.pac4j.jee.util;

import org.pac4j.core.exception.TechnicalException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.pac4j.core.util.CommonHelper.assertNotBlank;
import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * Helper to define filter mappings.
 *
 * @author Jerome Leleu
 * @since 3.0.0
 * @deprecated Use the corresponding class from the {@code jakartaee-pac4j} library.
 */
@Deprecated
public class FilterHelper {

    private final ServletContext servletContext;

    /**
     * Creates a helper for registering filters in a servlet context.
     *
     * @param servletContext the servlet context in which filters are registered
     */
    public FilterHelper(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Registers a filter and appends its URL mappings after existing mappings.
     * At least one URL pattern is required. If no dispatcher type is supplied,
     * the mapping uses {@link DispatcherType#REQUEST}.
     * Asynchronous operations are enabled by default when {@link DispatcherType#ASYNC}
     * is supplied. An explicit Boolean overrides this default; if several are supplied,
     * the last one is used. All filters and the servlet in the request chain must support
     * asynchronous operations for the request to start asynchronous processing.
     *
     * @param name the filter registration name
     * @param filter the filter instance
     * @param parameters URL patterns as strings, dispatcher types, and an optional Boolean
     *                   indicating whether the filter supports asynchronous operations
     * @throws TechnicalException if the name is blank, the filter or parameter array is null,
     *                            no URL pattern is supplied, or a parameter type is unsupported
     * @throws IllegalStateException if the servlet context has already been initialized
     */
    public void addFilterMapping(final String name, final Filter filter, final Object... parameters) {
        assertNotBlank("name", name);
        assertNotNull("filter", filter);
        assertNotNull("parameters", parameters);

        final List<String> urls = new ArrayList<>();
        final List<DispatcherType> types = new ArrayList<>();
        Boolean asyncSupported = null;
        for (final Object parameter : parameters) {
            if (parameter instanceof String) {
                urls.add((String) parameter);
            } else if (parameter instanceof DispatcherType) {
                types.add((DispatcherType) parameter);
            } else if (parameter instanceof Boolean) {
                asyncSupported = (Boolean) parameter;
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
        registration.setAsyncSupported(asyncSupported != null ? asyncSupported : types.contains(DispatcherType.ASYNC));
        registration.addMappingForUrlPatterns(EnumSet.copyOf(types), true, urls.toArray(new String[urls.size()]));
    }
}
