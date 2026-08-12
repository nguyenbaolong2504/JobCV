package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.dao.AuditLogDAO;
import vn.edu.eaut.recruitflow.model.AuditLog;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.sql.SQLException;

/** Writes structured audit entries for administrative and workflow changes. */
public class AuditLogService {
    private final AuditLogDAO auditLogDAO;

    public AuditLogService() {
        this(new AuditLogDAO());
    }

    AuditLogService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
    }

    public void record(int userId, String action, String entityName, Integer entityId, String details, String ipAddress)
            throws BusinessException {
        try {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setAction(action);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setDetails(details);
            log.setIpAddress(ipAddress);
            auditLogDAO.insert(log);
        } catch (SQLException exception) {
            throw new BusinessException("Không thể ghi nhật ký hệ thống.", exception);
        }
    }
}
