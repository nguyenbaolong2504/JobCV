package vn.edu.eaut.recruitflow.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DBUtilTest {

    @Test
    void enablesPublicKeyRetrievalForLocalNonTlsMysql() {
        String input = "jdbc:mysql://localhost:3306/recruitflow?useSSL=false&characterEncoding=UTF-8";

        assertEquals(input + "&allowPublicKeyRetrieval=true",
                DBUtil.normalizeLocalDevelopmentUrl(input));
    }

    @Test
    void preservesAnExplicitPublicKeyRetrievalSetting() {
        String input = "jdbc:mysql://127.0.0.1:3306/recruitflow?useSSL=false&allowPublicKeyRetrieval=false";

        assertEquals(input, DBUtil.normalizeLocalDevelopmentUrl(input));
    }

    @Test
    void doesNotChangeRemoteMysqlConnections() {
        String input = "jdbc:mysql://database.example.com:3306/recruitflow?useSSL=false";

        assertEquals(input, DBUtil.normalizeLocalDevelopmentUrl(input));
    }
}
