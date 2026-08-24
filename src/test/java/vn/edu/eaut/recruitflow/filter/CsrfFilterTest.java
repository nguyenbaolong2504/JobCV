package vn.edu.eaut.recruitflow.filter;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfFilterTest {
    @Test
    void doesNotCreateSessionForPublicGetWithoutAForm() throws Exception {
        RequestProbe probe = new RequestProbe("/recruitflow/home");
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        new CsrfFilter().doFilter(probe.request(), emptyResponse(), chainSetting(chainCalled));

        assertTrue(chainCalled.get());
        assertFalse(probe.createdSession);
        assertFalse(probe.requestAttributes.containsKey("csrfToken"));
    }

    @Test
    void createsTokenForAnonymousLoginForm() throws Exception {
        RequestProbe probe = new RequestProbe("/recruitflow/login");

        new CsrfFilter().doFilter(probe.request(), emptyResponse(), chainSetting(new AtomicBoolean()));

        assertTrue(probe.createdSession);
        assertEquals(probe.session.attributes.get("csrfToken"), probe.requestAttributes.get("csrfToken"));
    }

    private FilterChain chainSetting(AtomicBoolean called) {
        return (request, response) -> called.set(true);
    }

    private HttpServletResponse emptyResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{HttpServletResponse.class}, (proxy, method, arguments) -> defaultValue(method));
    }

    private static final class RequestProbe {
        private final Map<String, Object> requestAttributes = new HashMap<>();
        private final SessionProbe session = new SessionProbe();
        private final String requestUri;
        private boolean createdSession;

        private RequestProbe(String requestUri) {
            this.requestUri = requestUri;
        }

        private HttpServletRequest request() {
            return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class}, (proxy, method, arguments) -> switch (method.getName()) {
                        case "getRequestURI" -> requestUri;
                        case "getContextPath" -> "/recruitflow";
                        case "getMethod" -> "GET";
                        case "getSession" -> {
                            boolean create = method.getParameterCount() == 0 || Boolean.TRUE.equals(arguments[0]);
                            if (create) {
                                createdSession = true;
                                yield session.session();
                            }
                            yield createdSession ? session.session() : null;
                        }
                        case "setAttribute" -> {
                            requestAttributes.put((String) arguments[0], arguments[1]);
                            yield null;
                        }
                        case "getAttribute" -> requestAttributes.get(arguments[0]);
                        default -> defaultValue(method);
                    });
        }
    }

    private static final class SessionProbe implements java.lang.reflect.InvocationHandler {
        private final Map<String, Object> attributes = new HashMap<>();
        private final HttpSession session = (HttpSession) Proxy.newProxyInstance(
                CsrfFilterTest.class.getClassLoader(), new Class<?>[]{HttpSession.class}, this);

        private HttpSession session() {
            return session;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "getAttribute" -> attributes.get(arguments[0]);
                case "setAttribute" -> {
                    attributes.put((String) arguments[0], arguments[1]);
                    yield null;
                }
                default -> defaultValue(method);
            };
        }
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == int.class || returnType == short.class || returnType == byte.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0F;
        if (returnType == double.class) return 0D;
        if (returnType == char.class) return '\0';
        return null;
    }
}
