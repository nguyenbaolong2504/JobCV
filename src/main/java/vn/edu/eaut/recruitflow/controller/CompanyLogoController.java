package vn.edu.eaut.recruitflow.controller;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.service.CompanyProfileService;
import vn.edu.eaut.recruitflow.util.BusinessException;
import vn.edu.eaut.recruitflow.util.CompanyLogoStorageUtil;
import vn.edu.eaut.recruitflow.util.RequestUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "CompanyLogoController", urlPatterns = "/company-logo")
public class CompanyLogoController extends BaseController {
    private static final Map<String, String> TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "webp", "image/webp", "svg", "image/svg+xml");
    private static final String[] GRADIENTS = {
            "#065f46,#00b14f", "#1d4ed8,#38bdf8", "#6d28d9,#a78bfa",
            "#b45309,#f59e0b", "#be123c,#fb7185", "#0f766e,#2dd4bf",
            "#4338ca,#818cf8", "#9a3412,#fb923c"
    };
    private final CompanyProfileService companyService = new CompanyProfileService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            CompanyProfile company = companyService.getCompany(RequestUtil.requiredPositiveInt(request, "id", "Công ty"));
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Cache-Control", "public, max-age=86400");
            if (company.isUploadedLogo() && serveUploaded(company, response)) return;
            if (serveBundled(company, response)) return;
            serveGenerated(company, response);
        } catch (BusinessException | IllegalArgumentException exception) { response.sendError(HttpServletResponse.SC_NOT_FOUND); }
    }

    private boolean serveUploaded(CompanyProfile company, HttpServletResponse response) throws IOException {
        try {
            Path logo = CompanyLogoStorageUtil.resolveStored(
                    CompanyLogoStorageUtil.resolveDirectory(getServletContext()), company.getLogoFile());
            String type = contentType(logo.getFileName().toString());
            if (type == null) return false;
            response.setContentType(type);
            response.setContentLengthLong(Files.size(logo));
            Files.copy(logo, response.getOutputStream());
            return true;
        } catch (BusinessException exception) {
            return false;
        }
    }

    private boolean serveBundled(CompanyProfile company, HttpServletResponse response) throws IOException {
        String name = company.getLogoFile();
        if (name == null || name.isBlank() || "generic-careers.svg".equals(name)
                || !name.matches("[A-Za-z0-9._-]{1,120}")) return false;
        String type = contentType(name);
        if (type == null) return false;
        try (InputStream input = getServletContext().getResourceAsStream("/assets/images/employers/" + name)) {
            if (input == null) return false;
            response.setContentType(type);
            input.transferTo(response.getOutputStream());
            return true;
        }
    }

    private void serveGenerated(CompanyProfile company, HttpServletResponse response) throws IOException {
        String[] colors = GRADIENTS[Math.floorMod(company.getId(), GRADIENTS.length)].split(",");
        String initials = initials(company.getName());
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"160\" height=\"160\" viewBox=\"0 0 160 160\">"
                + "<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\"><stop stop-color=\""
                + colors[0] + "\"/><stop offset=\"1\" stop-color=\"" + colors[1]
                + "\"/></linearGradient></defs><rect width=\"160\" height=\"160\" rx=\"32\" fill=\"url(#g)\"/>"
                + "<circle cx=\"132\" cy=\"28\" r=\"18\" fill=\"#fff\" opacity=\".16\"/>"
                + "<text x=\"80\" y=\"96\" text-anchor=\"middle\" fill=\"#fff\" font-family=\"Arial,sans-serif\" "
                + "font-size=\"58\" font-weight=\"800\">" + initials + "</text></svg>";
        byte[] bytes = svg.getBytes(StandardCharsets.UTF_8);
        response.setContentType("image/svg+xml;charset=UTF-8");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private String contentType(String name) {
        int dot = name.lastIndexOf('.');
        return TYPES.get(dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "CT";
        StringBuilder result = new StringBuilder(2);
        for (String part : name.trim().split("\\s+")) {
            if (part.isBlank()) continue;
            int codePoint = part.codePointAt(0);
            if (Character.isLetterOrDigit(codePoint)) result.appendCodePoint(Character.toUpperCase(codePoint));
            if (result.codePointCount(0, result.length()) == 2) break;
        }
        return result.length() == 0 ? "CT" : result.toString();
    }
}
