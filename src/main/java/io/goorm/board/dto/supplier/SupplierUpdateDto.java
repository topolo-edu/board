package io.goorm.board.dto.supplier;

import io.goorm.board.enums.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공급업체 수정용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierUpdateDto {

    private Long supplierSeq;

    @NotBlank(message = "{supplier.validation.name.notblank}")
    @Size(max = 100, message = "{supplier.validation.name.size}")
    private String name;

    @Size(max = 50, message = "{supplier.validation.contactPerson.size}")
    private String contactPerson;

    @Email(message = "{supplier.validation.email.format}")
    @Size(max = 255, message = "{supplier.validation.email.size}")
    private String email;

    @Size(max = 20, message = "{supplier.validation.phone.size}")
    private String phone;

    @Size(max = 1000, message = "{supplier.validation.address.size}")
    private String address;

    @Size(max = 1000, message = "{supplier.validation.description.size}")
    private String description;

    private Boolean isActive;

    private SupplierStatus status;

    /**
     * Boolean isActive를 SupplierStatus로 변환
     */
    public SupplierStatus getStatus() {
        return status != null ? status : (Boolean.TRUE.equals(isActive) ? SupplierStatus.ACTIVE : SupplierStatus.INACTIVE);
    }

    /**
     * SupplierStatus를 설정
     */
    public void setStatus(SupplierStatus status) {
        this.status = status;
        this.isActive = status == SupplierStatus.ACTIVE;
    }
}