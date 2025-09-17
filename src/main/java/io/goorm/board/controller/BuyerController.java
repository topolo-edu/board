package io.goorm.board.controller;

import io.goorm.board.dto.buyer.*;
import io.goorm.board.enums.BuyerStatus;
import io.goorm.board.service.BuyerService;
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
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/buyers")
@RequiredArgsConstructor
@Slf4j
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping
    public String list(@ModelAttribute("search") BuyerSearchDto searchDto, Model model) {
        try {
            Page<BuyerDto> buyers = buyerService.searchBuyers(searchDto);
            List<BuyerStatus> buyerStatuses = buyerService.getBuyerStatuses();

            model.addAttribute("buyers", buyers.getContent());
            model.addAttribute("currentPage", buyers.getNumber() + 1);
            model.addAttribute("totalPages", buyers.getTotalPages());
            model.addAttribute("totalElements", buyers.getTotalElements());
            model.addAttribute("buyerStatuses", buyerStatuses);
            model.addAttribute("search", searchDto);

            return "buyers/list";
        } catch (Exception e) {
            log.error("고객 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "고객 목록을 불러오는 중 오류가 발생했습니다.");
            return "buyers/list";
        }
    }

    @GetMapping("/{seq}")
    public String show(@PathVariable("seq") Long buyerSeq, Model model, RedirectAttributes redirectAttributes) {
        try {
            BuyerDto buyer = buyerService.findBySeq(buyerSeq);
            model.addAttribute("buyer", buyer);
            return "buyers/show";
        } catch (Exception e) {
            log.error("고객 상세보기 중 오류 발생. ID: {}", buyerSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "고객을 찾을 수 없습니다.");
            return "redirect:/buyers";
        }
    }

    @GetMapping("/new")
    public String newBuyer(Model model) {
        BuyerCreateDto createDto = BuyerCreateDto.builder()
                .creditLimit(BigDecimal.ZERO)
                .discountRate(BigDecimal.ZERO)
                .paymentTerms("월말결제")
                .build();
        model.addAttribute("buyer", createDto);
        return "buyers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("buyer") BuyerCreateDto createDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("고객 등록 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "buyers/form";
        }

        try {
            Long buyerSeq = buyerService.create(createDto);
            redirectAttributes.addFlashAttribute("successMessage", "고객이 성공적으로 등록되었습니다.");
            return "redirect:/buyers/" + buyerSeq;
        } catch (Exception e) {
            log.error("고객 등록 중 오류 발생", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "buyers/form";
        }
    }

    @GetMapping("/{seq}/edit")
    public String edit(@PathVariable("seq") Long buyerSeq, Model model, RedirectAttributes redirectAttributes) {
        try {
            BuyerDto buyer = buyerService.findBySeq(buyerSeq);
            BuyerCreateDto buyerForm = BuyerCreateDto.builder()
                    .buyerSeq(buyer.getBuyerSeq())
                    .companyName(buyer.getCompanyName())
                    .businessNumber(buyer.getBusinessNumber())
                    .contactPerson(buyer.getContactPerson())
                    .email(buyer.getEmail())
                    .phone(buyer.getPhone())
                    .address(buyer.getAddress())
                    .creditLimit(buyer.getCreditLimit())
                    .discountRate(buyer.getDiscountRate())
                    .paymentTerms(buyer.getPaymentTerms())
                    .description(buyer.getDescription())
                    .status(buyer.getStatus())
                    .build();

            model.addAttribute("buyer", buyerForm);
            return "buyers/form";
        } catch (Exception e) {
            log.error("고객 수정 폼 로드 중 오류 발생. ID: {}", buyerSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "고객을 찾을 수 없습니다.");
            return "redirect:/buyers";
        }
    }

    @PostMapping("/{seq}")
    public String update(@PathVariable("seq") Long buyerSeq,
                         @Valid @ModelAttribute("buyer") BuyerCreateDto updateForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            log.warn("고객 수정 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "buyers/form";
        }

        try {
            BuyerUpdateDto updateDto = BuyerUpdateDto.builder()
                    .companyName(updateForm.getCompanyName())
                    .businessNumber(updateForm.getBusinessNumber())
                    .contactPerson(updateForm.getContactPerson())
                    .email(updateForm.getEmail())
                    .phone(updateForm.getPhone())
                    .address(updateForm.getAddress())
                    .creditLimit(updateForm.getCreditLimit())
                    .discountRate(updateForm.getDiscountRate())
                    .paymentTerms(updateForm.getPaymentTerms())
                    .description(updateForm.getDescription())
                    .build();

            buyerService.update(buyerSeq, updateDto);
            redirectAttributes.addFlashAttribute("successMessage", "고객이 성공적으로 수정되었습니다.");
            return "redirect:/buyers/" + buyerSeq;
        } catch (Exception e) {
            log.error("고객 수정 중 오류 발생. ID: {}", buyerSeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "buyers/form";
        }
    }

    @PostMapping("/{seq}/activate")
    public String activate(@PathVariable("seq") Long buyerSeq, RedirectAttributes redirectAttributes) {
        try {
            buyerService.activate(buyerSeq);
            redirectAttributes.addFlashAttribute("successMessage", "고객이 활성화되었습니다.");
        } catch (Exception e) {
            log.error("고객 활성화 중 오류 발생. ID: {}", buyerSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "고객 활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/buyers/" + buyerSeq;
    }

    @PostMapping("/{seq}/deactivate")
    public String deactivate(@PathVariable("seq") Long buyerSeq, RedirectAttributes redirectAttributes) {
        try {
            buyerService.deactivate(buyerSeq);
            redirectAttributes.addFlashAttribute("successMessage", "고객이 비활성화되었습니다.");
        } catch (Exception e) {
            log.error("고객 비활성화 중 오류 발생. ID: {}", buyerSeq, e);
            redirectAttributes.addFlashAttribute("errorMessage", "고객 비활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/buyers/" + buyerSeq;
    }

    @GetMapping("/excel")
    public void downloadExcel(@ModelAttribute BuyerSearchDto searchDto, HttpServletResponse response) {
        try {
            List<BuyerExcelDto> buyers = buyerService.findBuyersForExcel(searchDto);

            String[] headers = {"회사명", "사업자등록번호", "담당자명", "이메일", "전화번호", "주소",
                              "신용한도", "할인율", "총주문금액", "최근주문일", "결제조건", "설명", "상태", "등록일", "수정일"};
            String[] properties = {"companyName", "businessNumber", "contactPerson", "email", "phone", "address",
                                 "creditLimit", "discountRate", "totalOrderAmount", "lastOrderDate", "paymentTerms",
                                 "description", "status", "createdAt", "updatedAt"};

            ExcelUtil.downloadExcel(response, buyers, headers, properties, "buyers");
        } catch (IOException e) {
            log.error("고객 엑셀 다운로드 중 오류 발생", e);
            throw new RuntimeException("엑셀 다운로드 중 오류가 발생했습니다.", e);
        }
    }
}