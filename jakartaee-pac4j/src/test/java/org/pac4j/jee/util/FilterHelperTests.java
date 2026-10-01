package org.pac4j.jee.util;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.TechnicalException;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FilterHelperTests {

    private ServletContext servletContext;
    private FilterRegistration.Dynamic registration;
    private Filter filter;
    private FilterHelper helper;

    @BeforeEach
    void setUp() {
        servletContext = mock(ServletContext.class);
        registration = mock(FilterRegistration.Dynamic.class);
        filter = mock(Filter.class);
        when(servletContext.addFilter("security", filter)).thenReturn(registration);
        helper = new FilterHelper(servletContext);
    }

    @Test
    void asyncDispatcherEnablesAsynchronousOperations() {
        helper.addFilterMapping("security", filter, "/protected/*", DispatcherType.REQUEST, DispatcherType.ASYNC);

        verify(registration).setAsyncSupported(true);
        verify(registration).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC),
            true, "/protected/*");
    }

    @Test
    void asyncSupportCanBeEnabledForInitialRequestOnly() {
        helper.addFilterMapping("security", filter, true, "/protected/*");

        verify(registration).setAsyncSupported(true);
        verify(registration).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/protected/*");
    }

    @Test
    void synchronousFiltersKeepDefaultRequestMapping() {
        helper.addFilterMapping("security", filter, "/protected/*");

        verify(registration).setAsyncSupported(false);
        verify(registration).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/protected/*");
    }

    @Test
    void asyncSupportCanBeExplicitlyDisabled() {
        helper.addFilterMapping("security", filter, false, "/protected/*", DispatcherType.ASYNC);

        verify(registration).setAsyncSupported(false);
    }

    @Test
    void missingUrlIsRejectedBeforeRegistration() {
        assertThrows(TechnicalException.class,
            () -> helper.addFilterMapping("security", filter, true, DispatcherType.ASYNC));
        verify(servletContext, never()).addFilter(anyString(), any(Filter.class));
    }

    @Test
    void unsupportedParameterIsRejectedBeforeRegistration() {
        assertThrows(TechnicalException.class, () -> helper.addFilterMapping("security", filter, "/protected/*", 1));
        verify(servletContext, never()).addFilter(anyString(), any(Filter.class));
    }
}
