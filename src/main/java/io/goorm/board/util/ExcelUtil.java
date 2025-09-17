package io.goorm.board.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.math.BigDecimal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class ExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 범용 Excel 생성 메서드 (기존 호환성)
     */
    public static <T> byte[] createExcel(String sheetName, String[] headers, List<T> data,
                                       Function<T, String[]> rowMapper) throws IOException {
        Function<T, Object[]> objectMapper = item -> {
            String[] stringArray = rowMapper.apply(item);
            Object[] objectArray = new Object[stringArray.length];
            System.arraycopy(stringArray, 0, objectArray, 0, stringArray.length);
            return objectArray;
        };
        return createExcelWithTypes(sheetName, headers, data, objectMapper, null);
    }

    /**
     * 범용 Excel 생성 메서드 (데이터 타입 지원)
     */
    public static <T> byte[] createExcelWithTypes(String sheetName, String[] headers, List<T> data,
                                       Function<T, Object[]> rowMapper, CellType[] columnTypes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // 헤더 스타일
        CellStyle headerStyle = createHeaderStyle(workbook);

        // 데이터 스타일
        CellStyle dataStyle = createDataStyle(workbook);

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Object[] rowData = rowMapper.apply(data.get(i));

            for (int j = 0; j < rowData.length; j++) {
                CellType cellType = (columnTypes != null && j < columnTypes.length)
                    ? columnTypes[j] : CellType.STRING;
                createTypedCell(row, j, rowData[j], dataStyle, cellType);
            }
        }

        // 컬럼 폭 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 바이트 배열로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * 타입별 셀 생성
     */
    private static void createTypedCell(Row row, int column, Object value, CellStyle style, CellType cellType) {
        Cell cell = row.createCell(column);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
            return;
        }

        switch (cellType) {
            case NUMERIC:
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else {
                    // 문자열로 된 숫자를 파싱
                    try {
                        double numValue = Double.parseDouble(value.toString());
                        cell.setCellValue(numValue);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(value.toString());
                    }
                }
                break;
            case STRING:
            default:
                cell.setCellValue(value.toString());
                // 텍스트 타입으로 명시적 설정
                cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
                break;
        }
    }

    /**
     * 셀 타입 열거형
     */
    public enum CellType {
        STRING, NUMERIC
    }

    /**
     * Excel 파일명 생성
     */
    public static String generateFileName(String prefix) throws UnsupportedEncodingException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = prefix + "_" + timestamp + ".xlsx";
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
    }
}