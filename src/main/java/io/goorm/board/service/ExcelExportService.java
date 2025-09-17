package io.goorm.board.service;

import io.goorm.board.exception.excel.ExcelCreationException;
import io.goorm.board.exception.excel.ExcelDataException;
import io.goorm.board.exception.excel.ExcelFileNameException;
import io.goorm.board.util.ExcelUtil;
import io.goorm.board.util.ExcelUtil.CellType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * 범용 Excel 내보내기 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final MessageSource messageSource;

    /**
     * 엔티티 목록을 Excel로 내보내기 (기존 호환성)
     *
     * @param sheetName 시트명
     * @param headers 헤더 배열
     * @param data 데이터 목록
     * @param rowMapper 엔티티를 문자열 배열로 변환하는 함수
     * @param locale 로케일
     * @param <T> 엔티티 타입
     * @return Excel 파일 바이트 배열
     */
    public <T> byte[] exportToExcel(String sheetName, String[] headers, List<T> data,
                                   Function<T, String[]> rowMapper, Locale locale) {
        return exportToExcelWithTypes(sheetName, headers, data,
            item -> {
                String[] stringArray = rowMapper.apply(item);
                Object[] objectArray = new Object[stringArray.length];
                System.arraycopy(stringArray, 0, objectArray, 0, stringArray.length);
                return objectArray;
            }, null, locale);
    }

    /**
     * 엔티티 목록을 Excel로 내보내기 (데이터 타입 지원)
     *
     * @param sheetName 시트명
     * @param headers 헤더 배열
     * @param data 데이터 목록
     * @param rowMapper 엔티티를 Object 배열로 변환하는 함수
     * @param columnTypes 각 컬럼의 데이터 타입
     * @param locale 로케일
     * @param <T> 엔티티 타입
     * @return Excel 파일 바이트 배열
     */
    public <T> byte[] exportToExcelWithTypes(String sheetName, String[] headers, List<T> data,
                                   Function<T, Object[]> rowMapper, CellType[] columnTypes, Locale locale) {
        try {
            // 데이터 검증
            if (data == null || data.isEmpty()) {
                String message = messageSource.getMessage("excel.no.data", null, locale);
                throw new ExcelDataException(message);
            }

            log.debug("Exporting {} records to Excel sheet: {}", data.size(), sheetName);
            return ExcelUtil.createExcelWithTypes(sheetName, headers, data, rowMapper, columnTypes);

        } catch (ExcelDataException e) {
            throw e; // 이미 메시지가 처리된 예외는 그대로 전파
        } catch (IOException e) {
            log.error("Failed to create Excel file", e);
            String message = messageSource.getMessage("excel.creation.failed", null, locale);
            throw new ExcelCreationException(message, e);
        } catch (Exception e) {
            log.error("Unexpected error during Excel export", e);
            String message = messageSource.getMessage("excel.data.processing.failed", null, locale);
            throw new ExcelDataException(message, e);
        }
    }

    /**
     * Excel 파일명 생성
     *
     * @param prefix 파일명 접두사
     * @param locale 로케일
     * @return URL 인코딩된 파일명
     */
    public String generateFileName(String prefix, Locale locale) {
        try {
            return ExcelUtil.generateFileName(prefix);
        } catch (Exception e) {
            log.error("Failed to generate Excel filename", e);
            String message = messageSource.getMessage("excel.filename.generation.failed", null, locale);
            throw new ExcelFileNameException(message, e);
        }
    }
}