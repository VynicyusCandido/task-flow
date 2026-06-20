package com.example.taskflow.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void whenNoHeader_generatesUUIDAndSetsResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        String correlationId = response.getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotNull().matches("[0-9a-f\\-]{36}");
        verify(chain).doFilter(request, response);
    }

    @Test
    void whenHeaderProvided_usesExistingId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-ID", "custom-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("custom-id-123");
    }

    @Test
    void afterRequest_mdcIsCleared() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void whenHeaderContainsControlChar_generatesNewUUID() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-ID", "injected\nfake-log-line");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        String correlationId = response.getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotNull().matches("[0-9a-f\\-]{36}");
    }
}
