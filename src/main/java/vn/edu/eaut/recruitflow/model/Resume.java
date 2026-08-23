package vn.edu.eaut.recruitflow.model;

import java.sql.Timestamp;

public class Resume {
    private int id;
    private int candidateId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;
    private String extractedText;
    private boolean defaultResume;
    private boolean fileAvailable = true;
    private Timestamp uploadedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public boolean isDefaultResume() { return defaultResume; }
    /** JSP-compatible property name: ${resume.default}. */
    public boolean isDefault() { return defaultResume; }
    public boolean getIsDefault() { return defaultResume; }
    public void setDefaultResume(boolean defaultResume) { this.defaultResume = defaultResume; }
    public void setDefault(boolean defaultResume) { this.defaultResume = defaultResume; }
    /** Transient display/use guard; file content never comes from the database. */
    public boolean isFileAvailable() { return fileAvailable; }
    public void setFileAvailable(boolean fileAvailable) { this.fileAvailable = fileAvailable; }
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }
}
