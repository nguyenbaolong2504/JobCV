package vn.edu.eaut.recruitflow.util;

import org.junit.jupiter.api.Test;
import vn.edu.eaut.recruitflow.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSessionTest {
    @Test
    void establishesFreshSessionAndPersistsCredentialVersion() {
        SessionProbe previous = new SessionProbe();
        SessionProbe fresh = new SessionProbe();
        HttpServletRequest request = requestReturning(previous.session(), fresh.session());
        User user = new User();
        user.setId(42);
        user.setRoleName("CANDIDATE");
        user.setFullName("Candidate Test");
        user.setSessionVersion(7);

        HttpSession established = AuthSession.establish(request, user);

        assertTrue(previous.invalidated);
        assertSame(fresh.session(), established);
        assertEquals(42, fresh.attributes.get("userId"));
        assertEquals("CANDIDATE", fresh.attributes.get("role"));
        assertEquals(7, fresh.attributes.get(AuthSession.SESSION_VERSION_ATTRIBUTE));
        assertEquals(AuthSession.SESSION_TIMEOUT_SECONDS, fresh.maxInactiveInterval);
    }

    @Test
    void routesCandidateToOpportunityHomeAndStaffToTheirWorkspaces() {
        assertEquals("/home", AuthSession.landingPath("candidate"));
        assertEquals("/hr/dashboard", AuthSession.landingPath("HR"));
        assertEquals("/interviewer/dashboard", AuthSession.landingPath("INTERVIEWER"));
        assertEquals("/admin/dashboard", AuthSession.landingPath("ADMIN"));
    }

    private HttpServletRequest requestReturning(HttpSession previous, HttpSession fresh) {
        InvocationHandler handler = (proxy, method, arguments) -> {
            if ("getSession".equals(method.getName())) {
                if (method.getParameterCount() == 0 || Boolean.TRUE.equals(arguments[0])) {
                    return fresh;
                }
                return previous;
            }
            return defaultValue(method);
        };
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, handler);
    }

    private static final class SessionProbe implements InvocationHandler {
        private final Map<String, Object> attributes = new HashMap<>();
        private final HttpSession session = (HttpSession) Proxy.newProxyInstance(
                AuthSessionTest.class.getClassLoader(), new Class<?>[]{HttpSession.class}, this);
        private boolean invalidated;
        private int maxInactiveInterval;

        HttpSession session() {
            return session;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "setAttribute" -> {
                    attributes.put((String) arguments[0], arguments[1]);
                    yield null;
                }
                case "getAttribute" -> attributes.get(arguments[0]);
                case "removeAttribute" -> {
                    attributes.remove(arguments[0]);
                    yield null;
                }
                case "invalidate" -> {
                    invalidated = true;
                    yield null;
                }
                case "setMaxInactiveInterval" -> {
                    maxInactiveInterval = (Integer) arguments[0];
                    yield null;
                }
                case "getMaxInactiveInterval" -> maxInactiveInterval;
                case "getId" -> "test-session";
                case "isNew" -> false;
                default -> defaultValue(method);
            };
        }
    }

    private static Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == short.class || returnType == byte.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
