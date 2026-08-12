package vn.edu.eaut.recruitflow.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Extracts searchable plain text from the allowed CV formats. */
public final class ResumeParser {
    private ResumeParser() {
    }

    public static String extractText(InputStream input, String extension) throws IOException, BusinessException {
        String normalized = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        try (InputStream source = input) {
            switch (normalized) {
                case "pdf":
                    try (PDDocument document = PDDocument.load(source)) {
                        return new PDFTextStripper().getText(document);
                    }
                case "docx":
                    try (XWPFDocument document = new XWPFDocument(source)) {
                        StringBuilder builder = new StringBuilder();
                        document.getParagraphs().forEach(paragraph -> builder.append(paragraph.getText()).append('\n'));
                        return builder.toString();
                    }
                case "doc":
                    try (HWPFDocument document = new HWPFDocument(source)) {
                        return document.getRange().text();
                    }
                case "txt":
                    return new String(source.readAllBytes(), StandardCharsets.UTF_8);
                default:
                    throw new BusinessException("Chỉ hỗ trợ CV định dạng PDF, DOC hoặc DOCX.");
            }
        }
    }
}
