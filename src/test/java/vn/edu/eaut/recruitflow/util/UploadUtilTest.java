package vn.edu.eaut.recruitflow.util;

import org.junit.jupiter.api.Test;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadUtilTest {
    @Test
    void acceptsPdfWithMatchingMimeNameAndMagicBytes() {
        Part pdf = part("candidate.pdf", "application/pdf", "%PDF-1.7".getBytes());

        assertDoesNotThrow(() -> UploadUtil.validateResumePart(pdf));
    }

    @Test
    void rejectsFileThatOnlyPretendsToBePdf() {
        Part disguisedScript = part("candidate.pdf", "application/pdf", "<script>alert(1)</script>".getBytes());

        assertThrows(BusinessException.class, () -> UploadUtil.validateResumePart(disguisedScript));
    }

    private Part part(String filename, String contentType, byte[] content) {
        return (Part) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Part.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getSubmittedFileName" -> filename;
                    case "getContentType" -> contentType;
                    case "getSize" -> (long) content.length;
                    case "getInputStream" -> new ByteArrayInputStream(content);
                    default -> defaultValue(method);
                });
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
