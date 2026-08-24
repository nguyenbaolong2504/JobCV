package vn.edu.eaut.recruitflow.filter;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityHeadersFilterTest {
    @Test
    void protectsPrivatePagesFromCachingAndClickjacking() throws Exception {
        Map<String, String> headers = new HashMap<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        HttpServletRequest request = request("/recruitflow/candidate/dashboard", "/recruitflow", false);
        HttpServletResponse response = response(headers);
        FilterChain chain = (servletRequest, servletResponse) -> chainCalled.set(true);

        new SecurityHeadersFilter().doFilter(request, response, chain);

        assertTrue(chainCalled.get());
        assertEquals("DENY", headers.get("X-Frame-Options"));
        assertEquals("nosniff", headers.get("X-Content-Type-Options"));
        assertEquals("no-store, no-cache, must-revalidate, max-age=0", headers.get("Cache-Control"));
        assertTrue(headers.get("Content-Security-Policy").contains("frame-ancestors 'none'"));
    }

    @Test
    void addsHstsOnlyForHttpsRequests() throws Exception {
        Map<String, String> headers = new HashMap<>();
        HttpServletRequest request = request("/recruitflow/home", "/recruitflow", true);
        HttpServletResponse response = response(headers);

        new SecurityHeadersFilter().doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertTrue(headers.get("Strict-Transport-Security").contains("max-age=31536000"));
    }

    private HttpServletRequest request(String requestUri, String contextPath, boolean secure) {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, arguments) -> switch (method.getName()) {
                    case "getRequestURI" -> requestUri;
                    case "getContextPath" -> contextPath;
                    case "isSecure" -> secure;
                    default -> defaultValue(method);
                });
    }

    private HttpServletResponse response(Map<String, String> headers) {
        return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class}, (proxy, method, arguments) -> switch (method.getName()) {
                    case "setHeader" -> {
                        headers.put((String) arguments[0], (String) arguments[1]);
                        yield null;
                    }
                    case "setDateHeader" -> {
                        headers.put((String) arguments[0], String.valueOf(arguments[1]));
                        yield null;
                    }
                    default -> defaultValue(method);
                });
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == int.class || returnType == short.class || returnType == byte.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0F;
        if (returnType == double.class) return 0D;
        if (returnType == char.class) return '\0';
        return null;
    }
}
