package io.goorm.board.controller;

import io.goorm.board.dto.supplier.*;
import io.goorm.board.enums.SupplierStatus;
import io.goorm.board.service.SupplierService;
import io.goorm.board.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * 공급업체 목록 페이지
     */
    @GetMapping
    public String list(@ModelAttribute("search") SupplierSearchDto searchDto, Model model) {
        try {
            Page<SupplierDto> suppliers = supplierService.searchSuppliers(searchDto);
            List<SupplierStatus> supplierStatuses = supplierService.getSupplierStatuses();

            model.addAttribute("suppliers", suppliers.getContent());
            model.addAttribute("currentPage", suppliers.getNumber() + 1);
            model.addAttribute("totalPages", suppliers.getTotalPages());
            model.addAttribute("totalElements", suppliers.getTotalElements());
            model.addAttribute("supplierStatuses", supplierStatuses);
            model.addAttribute("search", searchDto);

            return "suppliers/list";
        } catch (Exception e) {
            log.error("공급업체 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "공급업체 목록을 불러오는 중 오류가 발생했습니다.");
            return "suppliers/list";
        }
    }

    /**
     * 공급업체 상세보기 페이지
     */
    @GetMapping("/{seq}")
    public String show(@PathVariable("seq") Long supplierSeq, Model model, RedirectAttributes redirectAttributes) {
        try {
            SupplierDto supplier = supplierService.findBySeq(supplierSeq);
            model.addAttribute("supplier", supplier);
            return "suppliers/show";
        } catch (Exception e) {
            log.error("공급업체 상세보기 중 오류 발생. ID: {}", supplierSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "공급업체를 찾을 수 없습니다.");
            return "redirect:/suppliers";
        }
    }

    /**
     * 공급업체 등록 페이지
     */
    @GetMapping("/new")
    public String newSupplier(Model model) {
        model.addAttribute("supplier", new SupplierCreateDto());
        return "suppliers/form";
    }

    /**
     * 공급업체 등록 처리
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("supplier") SupplierCreateDto createDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("공급업체 등록 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "suppliers/form";
        }

        try {
            Long supplierSeq = supplierService.create(createDto);
            redirectAttributes.addFlashAttribute("successMessage", "공급업체가 성공적으로 등록되었습니다.");
            return "redirect:/suppliers/" + supplierSeq;
        } catch (Exception e) {
            log.error("공급업체 등록 중 오류 발생", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "suppliers/form";
        }
    }

    /**
     * 공급업체 수정 페이지
     */
    @GetMapping("/{seq}/edit")
    public String edit(@PathVariable("seq") Long supplierSeq, Model model, RedirectAttributes redirectAttributes) {
        try {
            SupplierDto supplier = supplierService.findBySeq(supplierSeq);
            SupplierCreateDto supplierForm = SupplierCreateDto.builder()
                    .supplierSeq(supplier.getSupplierSeq())
                    .name(supplier.getName())
                    .contactPerson(supplier.getContactPerson())
                    .email(supplier.getEmail())
                    .phone(supplier.getPhone())
                    .address(supplier.getAddress())
                    .description(supplier.getDescription())
                    .status(supplier.getStatus())
                    .build();

            model.addAttribute("supplier", supplierForm);
            return "suppliers/form";
        } catch (Exception e) {
            log.error("공급업체 수정 폼 로드 중 오류 발생. ID: {}", supplierSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "공급업체를 찾을 수 없습니다.");
            return "redirect:/suppliers";
        }
    }

    /**
     * 공급업체 수정 처리
     */
    @PostMapping("/{seq}")
    public String update(@PathVariable("seq") Long supplierSeq,
                         @Valid @ModelAttribute("supplier") SupplierCreateDto updateForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("공급업체 수정 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "suppliers/form";
        }

        try {
            SupplierUpdateDto updateDto = SupplierUpdateDto.builder()
                    .name(updateForm.getName())
                    .contactPerson(updateForm.getContactPerson())
                    .email(updateForm.getEmail())
                    .phone(updateForm.getPhone())
                    .address(updateForm.getAddress())
                    .description(updateForm.getDescription())
                    .build();

            supplierService.update(supplierSeq, updateDto);
            redirectAttributes.addFlashAttribute("successMessage", "공급업체가 성공적으로 수정되었습니다.");
            return "redirect:/suppliers/" + supplierSeq;
        } catch (Exception e) {
            log.error("공급업체 수정 중 오류 발생. ID: {}", supplierSeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "suppliers/form";
        }
    }

    /**
     * 공급업체 활성화
     */
    @PostMapping("/{seq}/activate")
    public String activate(@PathVariable("seq") Long supplierSeq, RedirectAttributes redirectAttributes) {
        try {
            supplierService.activate(supplierSeq);
            redirectAttributes.addFlashAttribute("successMessage", "공급업체가 활성화되었습니다.");
        } catch (Exception e) {
            log.error("공급업체 활성화 중 오류 발생. ID: {}", supplierSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "공급업체 활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/suppliers/" + supplierSeq;
    }

    /**
     * 공급업체 비활성화
     */
    @PostMapping("/{seq}/deactivate")
    public String deactivate(@PathVariable("seq") Long supplierSeq, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deactivate(supplierSeq);
            redirectAttributes.addFlashAttribute("successMessage", "공급업체가 비활성화되었습니다.");
        } catch (Exception e) {
            log.error("공급업체 비활성화 중 오류 발생. ID: {}", supplierSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "공급업체 비활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/suppliers/" + supplierSeq;
    }

    /**
     * 공급업체 엑셀 다운로드
     */
    @GetMapping("/excel")
    public void downloadExcel(@ModelAttribute SupplierSearchDto searchDto, HttpServletResponse response) {
        try {
            List<SupplierExcelDto> suppliers = supplierService.findSuppliersForExcel(searchDto);

            String[] headers = {"업체명", "담당자명", "이메일", "전화번호", "주소", "설명", "상태", "등록일", "수정일"};
            String[] properties = {"name", "contactPerson", "email", "phone", "address", "description", "status", "createdAt", "updatedAt"};

            ExcelUtil.downloadExcel(response, suppliers, headers, properties, "suppliers");
        } catch (IOException e) {
            log.error("공급업체 엑셀 다운로드 중 오류 발생", e);
            throw new RuntimeException("엑셀 다운로드 중 오류가 발생했습니다.", e);
        }
    }
}