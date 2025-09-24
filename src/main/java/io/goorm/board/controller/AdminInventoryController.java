package io.goorm.board.controller;

import io.goorm.board.dto.excel.StockReceivingDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.ExcelService;
import io.goorm.board.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 관리자 재고 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final ExcelService excelService;
    private final InventoryService inventoryService;

    /**
     * 재고 관리 메인 페이지
     */
    @GetMapping
    public String inventory(Model model) {
        return "admin/inventory/main";
    }

    /**
     * 엑셀 입고 처리 페이지
     */
    @GetMapping("/receiving")
    public String receivingForm() {
        return "admin/inventory/receiving";
    }

    /**
     * 엑셀 템플릿 다운로드 (헤더만)
     */
    @GetMapping("/template/download")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam(defaultValue = "empty") String type) {
        try {
            log.info("엑셀 템플릿 다운로드 요청 - Type: {}", type);

            byte[] templateBytes;
            String fileName;

            if ("sample".equals(type)) {
                // 완성된 테스트 데이터가 포함된 템플릿
                templateBytes = excelService.generateStockReceivingTemplate();
                fileName = "stock_receiving_sample.xlsx";
                log.info("샘플 데이터 포함 템플릿 다운로드");
            } else {
                // 헤더만 있는 빈 템플릿
                templateBytes = excelService.generateEmptyTemplate();
                fileName = "stock_receiving_template.xlsx";
                log.info("빈 템플릿 다운로드");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(templateBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);

        } catch (Exception e) {
            log.error("엑셀 템플릿 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 엑셀 파일 업로드 및 입고 처리
     */
    @PostMapping("/receiving/upload")
    public String uploadAndProcess(@RequestParam("file") MultipartFile file,
                                  @AuthenticationPrincipal User user,
                                  RedirectAttributes redirectAttributes) {
        try {
            log.info("엑셀 입고 처리 시작 - User: {}, FileName: {}", user.getEmail(), file.getOriginalFilename());

            // 파일 검증
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
                return "redirect:/admin/inventory/receiving";
            }

            if (!isExcelFile(file)) {
                redirectAttributes.addFlashAttribute("errorMessage", "엑셀 파일만 업로드 가능합니다. (.xlsx, .xls)");
                return "redirect:/admin/inventory/receiving";
            }

            // 엑셀 파싱
            List<StockReceivingDto> stockList = excelService.parseStockReceivingExcel(file);

            if (stockList.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "처리할 데이터가 없습니다.");
                return "redirect:/admin/inventory/receiving";
            }

            // 입고 처리
            List<String> errors = inventoryService.processStockReceiving(stockList, user);

            // 결과 메시지 설정
            int successCount = stockList.size() - errors.size();
            int totalCount = stockList.size();

            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage",
                    String.format("입고 처리가 완료되었습니다. (성공: %d건)", successCount));
            } else {
                redirectAttributes.addFlashAttribute("warningMessage",
                    String.format("입고 처리가 완료되었습니다. (성공: %d건, 실패: %d건)", successCount, errors.size()));
                redirectAttributes.addFlashAttribute("errorDetails", errors);
            }

            log.info("엑셀 입고 처리 완료 - 전체: {}건, 성공: {}건, 실패: {}건",
                    totalCount, successCount, errors.size());

            return "redirect:/admin/inventory/receiving";

        } catch (Exception e) {
            log.error("엑셀 입고 처리 실패 - User: {}, Error: {}", user.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "입고 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/inventory/receiving";
        }
    }

    /**
     * 엑셀 파일 확장자 검증
     */
    private boolean isExcelFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return false;

        String extension = fileName.toLowerCase();
        return extension.endsWith(".xlsx") || extension.endsWith(".xls");
    }
}