package vn.edu.eaut.recruitflow.service;

import vn.edu.eaut.recruitflow.model.CompanyProfile;
import vn.edu.eaut.recruitflow.model.Department;
import vn.edu.eaut.recruitflow.model.Job;
import vn.edu.eaut.recruitflow.model.PageResult;
import vn.edu.eaut.recruitflow.util.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Provides coherent public employer pages without exposing HR account data. */
public class CompanyProfileService {
    private final DepartmentService departmentService;
    private final JobService jobService;

    public CompanyProfileService() {
        this.departmentService = new DepartmentService();
        this.jobService = new JobService();
    }

    public List<CompanyProfile> getCompanies(String keyword) throws BusinessException {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<CompanyProfile> result = new ArrayList<>();
        for (Department department : departmentService.getAllDepartments()) {
            CompanyProfile company = fromDepartment(department);
            if (normalized.isEmpty() || company.getName().toLowerCase(Locale.ROOT).contains(normalized)
                    || company.getIndustry().toLowerCase(Locale.ROOT).contains(normalized)
                    || company.getLocation().toLowerCase(Locale.ROOT).contains(normalized)) {
                company.setOpenJobs(jobService.searchPublishedJobs(null, department.getId(), null, null, 1, 1, "newest").getTotalItems());
                result.add(company);
            }
        }
        return result;
    }

    public CompanyProfile getCompany(int id) throws BusinessException {
        Department department = departmentService.getById(id);
        CompanyProfile company = fromDepartment(department);
        company.setOpenJobs(jobService.searchPublishedJobs(null, id, null, null, 1, 1, "newest").getTotalItems());
        return company;
    }

    public List<Job> getOpenJobs(int id) throws BusinessException {
        PageResult<Job> page = jobService.searchPublishedJobs(null, id, null, null, 1, 24, "newest");
        return page.getItems();
    }

    public CompanyProfile fromDepartment(Department department) {
        CompanyProfile company = new CompanyProfile();
        company.setId(department.getId());
        company.setVerified(true);
        company.setLocation("Hà Nội · TP.HCM · Làm việc linh hoạt");
        company.setSize("100–500 nhân sự");
        company.setWebsite("recruitflow.local");
        company.setHighlights(List.of("Quy trình tuyển dụng minh bạch", "Môi trường học hỏi và phát triển", "Đánh giá theo năng lực"));
        switch (department.getName()) {
            case "Information Technology" -> assign(company, "NovaTech Solutions", "IT - Phần mềm", "novatech.svg",
                    "NovaTech xây dựng sản phẩm số và nền tảng doanh nghiệp, tập trung vào chất lượng kỹ thuật, khả năng mở rộng và trải nghiệm người dùng.");
            case "Finance" -> assign(company, "Horizon Finance", "Tài chính", "horizon-finance.svg",
                    "Horizon Finance phát triển các giải pháp tài chính vận hành dựa trên dữ liệu, kiểm soát rủi ro và dịch vụ khách hàng đáng tin cậy.");
            case "Human Resources" -> assign(company, "PeopleFirst Group", "Nhân sự", "peoplefirst.svg",
                    "PeopleFirst đồng hành cùng doanh nghiệp trong tuyển dụng, phát triển con người và xây dựng trải nghiệm nhân viên bền vững.");
            case "Marketing" -> assign(company, "Aurora Media", "Marketing & Truyền thông", "aurora-media.svg",
                    "Aurora Media là đội ngũ chiến lược và sáng tạo đa kênh, kết nối thương hiệu với khách hàng bằng nội dung và dữ liệu.");
            case "Sales" -> assign(company, "NextCommerce", "Kinh doanh & Thương mại", "nextcommerce.svg",
                    "NextCommerce phát triển hệ sinh thái bán hàng hiện đại, nơi đội ngũ kinh doanh được hỗ trợ bởi công nghệ và dữ liệu thị trường.");
            case "Construction" -> assign(company, "BuildCore Vietnam", "Xây dựng", "generic-careers.svg",
                    "BuildCore Vietnam triển khai các dự án xây dựng với trọng tâm an toàn, chất lượng công trình và phát triển đội ngũ kỹ sư hiện trường.");
            case "Customer Service" -> assign(company, "CarePlus Services", "Chăm sóc khách hàng", "generic-careers.svg",
                    "CarePlus xây dựng trải nghiệm khách hàng nhất quán qua đội ngũ tư vấn, chăm sóc và quản lý chất lượng dịch vụ.");
            case "Design" -> assign(company, "PixelCraft Studio", "Thiết kế", "generic-careers.svg",
                    "PixelCraft là studio thiết kế sản phẩm và truyền thông, đề cao tư duy người dùng, tính nhất quán và khả năng cộng tác đa chức năng.");
            case "Operations" -> assign(company, "FlowOps Vietnam", "Vận hành", "generic-careers.svg",
                    "FlowOps tối ưu quy trình vận hành, phối hợp nguồn lực và chất lượng dịch vụ để giúp tổ chức tăng trưởng bền vững.");
            default -> assign(company, vietnameseDepartmentName(department.getName()) + " Careers", vietnameseDepartmentName(department.getName()), "generic-careers.svg",
                    department.getDescription() == null || department.getDescription().isBlank()
                            ? "Doanh nghiệp chú trọng xây dựng đội ngũ chuyên môn, quy trình làm việc rõ ràng và cơ hội phát triển dài hạn."
                            : department.getDescription());
        }
        return company;
    }

    private void assign(CompanyProfile company, String name, String industry, String logo, String description) {
        company.setName(name);
        company.setIndustry(industry);
        company.setLogoFile(logo);
        company.setDescription(description);
    }

    private String vietnameseDepartmentName(String value) {
        if (value == null) return "Doanh nghiệp";
        return switch (value) {
            case "Engineering" -> "Kỹ thuật";
            case "Operations" -> "Vận hành";
            case "Customer Service" -> "Chăm sóc khách hàng";
            case "Design" -> "Thiết kế";
            default -> value;
        };
    }
}
