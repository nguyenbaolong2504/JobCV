package vn.edu.eaut.recruitflow.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Extracts searchable plain text from the allowed CV formats. */
public final class ResumeParser {
    private static final int MAX_PDF_PAGES = 50;
    private static final int MAX_EXTRACTED_TEXT_CHARACTERS = 200_000;

    static {
        // CV uploads are capped at 5 MB, but a crafted Office ZIP can expand far beyond that.
        // POI applies these limits before materializing dangerous entries in memory.
        ZipSecureFile.setMinInflateRatio(0.01d);
        ZipSecureFile.setMaxEntrySize(20L * 1024L * 1024L);
        ZipSecureFile.setMaxTextSize(2L * 1024L * 1024L);
    }

    private ResumeParser() {
    }

    public static String extractText(InputStream input, String extension) throws IOException, BusinessException {
        String normalized = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        try (InputStream source = input) {
            switch (normalized) {
                case "pdf":
                    try (PDDocument document = PDDocument.load(source, MemoryUsageSetting.setupTempFileOnly())) {
                        if (document.getNumberOfPages() > MAX_PDF_PAGES) {
                            throw new BusinessException("CV PDF không được vượt quá " + MAX_PDF_PAGES + " trang.");
                        }
                        return limitExtractedText(new PDFTextStripper().getText(document));
                    }
                case "docx":
                    try (XWPFDocument document = new XWPFDocument(source)) {
                        StringBuilder builder = new StringBuilder();
                        document.getParagraphs().forEach(paragraph -> builder.append(paragraph.getText()).append('\n'));
                        return limitExtractedText(builder.toString());
                    }
                case "doc":
                    try (HWPFDocument document = new HWPFDocument(source)) {
                        return limitExtractedText(document.getRange().text());
                    }
                case "txt":
                    return limitExtractedText(new String(source.readAllBytes(), StandardCharsets.UTF_8));
                default:
                    throw new BusinessException("Chỉ hỗ trợ CV định dạng PDF, DOC hoặc DOCX.");
            }
        }
    }

    private static String limitExtractedText(String text) throws BusinessException {
        String safeText = text == null ? "" : text;
        if (safeText.length() > MAX_EXTRACTED_TEXT_CHARACTERS) {
            throw new BusinessException("Nội dung CV quá lớn để hệ thống xử lý an toàn.");
        }
        return safeText;
    }
}
