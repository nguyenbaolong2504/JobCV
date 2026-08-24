package vn.edu.eaut.recruitflow.tool;

import vn.edu.eaut.recruitflow.util.VietnameseTextUtil;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * One-time maintenance tool for legacy rows whose UTF-8 bytes were previously decoded as
 * Windows-1252/Latin-1. It only scans the explicitly whitelisted user-facing text columns.
 */
public final class DatabaseTextRepairTool {
    private static final List<TableSpec> TABLES = List.of(
            table("users", "id", "full_name"),
            table("candidate_profiles", "id", "address", "university", "major", "skills", "summary"),
            table("recruiter_profiles", "id", "organization_name", "job_title"),
            table("departments", "id", "name", "description"),
            table("job_categories", "id", "name", "description"),
            table("jobs", "id", "title", "location", "description", "requirements"),
            table("job_skills", "id", "skill_name"),
            table("resumes", "id", "file_name", "extracted_text"),
            table("application_status_history", "id", "remarks"),
            table("interviews", "id", "location", "note"),
            table("interview_feedbacks", "id", "comment"),
            table("offers", "id", "location", "note"),
            table("onboarding_tasks", "id", "task_name", "description"),
            table("notifications", "id", "title", "message"),
            table("audit_logs", "id", "action", "entity_name", "details"),
            table("permissions", "id", "module", "display_name", "description"),
            table("roles", "id", "description")
    );

    private DatabaseTextRepairTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].isBlank()) {
            System.err.println("Usage: DatabaseTextRepairTool <jdbc-url> [db-user] [--dry-run]");
            System.exit(2);
        }
        String jdbcUrl = args[0];
        String user = args.length > 1 && !args[1].startsWith("--") ? args[1] : "root";
        boolean dryRun = Arrays.asList(args).contains("--dry-run");
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Chạy công cụ trong terminal tương tác để nhập mật khẩu an toàn.");
        }
        char[] passwordChars = console.readPassword("MySQL password: ");
        String password = passwordChars == null ? "" : new String(passwordChars);
        if (passwordChars != null) {
            Arrays.fill(passwordChars, '\0');
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, user, password)) {
            connection.setAutoCommit(false);
            int repaired = 0;
            try {
                for (TableSpec table : TABLES) {
                    int tableCount = repairTable(connection, table, dryRun);
                    repaired += tableCount;
                    if (tableCount > 0) {
                        System.out.printf("%s: %d row(s)%n", table.name(), tableCount);
                    }
                }
                if (dryRun) {
                    connection.rollback();
                    System.out.printf("Dry-run hoàn tất: tìm thấy %d row(s), chưa ghi dữ liệu.%n", repaired);
                } else {
                    connection.commit();
                    System.out.printf("Hoàn tất: đã sửa %d row(s).%n", repaired);
                }
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        } finally {
            password = null;
        }
    }

    private static int repairTable(Connection connection, TableSpec table, boolean dryRun) throws Exception {
        String select = "SELECT `" + table.key() + "`, "
                + table.columns().stream().map(column -> "`" + column + "`").reduce((a, b) -> a + ", " + b).orElseThrow()
                + " FROM `" + table.name() + "`";
        int count = 0;
        try (PreparedStatement statement = connection.prepareStatement(select);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                List<String> changedColumns = new ArrayList<>();
                List<String> repairedValues = new ArrayList<>();
                for (String column : table.columns()) {
                    String original = resultSet.getString(column);
                    String repaired = VietnameseTextUtil.repairLegacyMojibake(original);
                    if (original != null && !original.equals(repaired)) {
                        changedColumns.add(column);
                        repairedValues.add(repaired);
                    }
                }
                if (changedColumns.isEmpty()) {
                    continue;
                }
                count++;
                if (!dryRun) {
                    updateRow(connection, table, resultSet.getObject(table.key()), changedColumns, repairedValues);
                }
            }
        }
        return count;
    }

    private static void updateRow(Connection connection, TableSpec table, Object keyValue,
                                  List<String> columns, List<String> values) throws Exception {
        String assignments = columns.stream().map(column -> "`" + column + "` = ?")
                .reduce((a, b) -> a + ", " + b).orElseThrow();
        String update = "UPDATE `" + table.name() + "` SET " + assignments
                + " WHERE `" + table.key() + "` = ?";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            int index = 1;
            for (String value : values) {
                statement.setString(index++, value);
            }
            statement.setObject(index, keyValue);
            statement.executeUpdate();
        }
    }

    private static TableSpec table(String name, String key, String... columns) {
        return new TableSpec(name, key, List.of(columns));
    }

    private record TableSpec(String name, String key, List<String> columns) {
    }
}
