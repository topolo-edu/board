package io.goorm.board.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공급업체 엑셀 다운로드용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierExcelDto {

    private String name;

    private String contactPerson;

    private String email;

    private String phone;

    private String address;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}