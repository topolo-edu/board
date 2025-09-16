package io.goorm.board.dto.product;

import io.goorm.board.enums.ProductStatus;
import io.goorm.board.enums.ProductUnit;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

/**
 * 상품 수정용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDto {

    @NotNull(message = "{validation.product.seq.required}")
    private Long productSeq;

    @NotBlank(message = "{validation.product.code.required}")
    @Size(max = 50, message = "{validation.product.code.size}")
    private String code;

    @NotBlank(message = "{validation.product.name.required}")
    @Size(max = 200, message = "{validation.product.name.size}")
    private String name;

    @Size(max = 4000, message = "{validation.product.description.size}")
    private String description;

    private Long categorySeq;

    private Long supplierSeq;

    @NotNull(message = "{validation.product.price.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.price.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.product.price.format}")
    private BigDecimal unitPrice;

    @NotNull(message = "{validation.product.cost.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.cost.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.product.cost.format}")
    private BigDecimal unitCost;

    private ProductUnit unit;

    @Size(max = 100, message = "{validation.product.sku.size}")
    private String sku;

    @Size(max = 100, message = "{validation.product.barcode.size}")
    private String barcode;

    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.weight.min}")
    @Digits(integer = 5, fraction = 3, message = "{validation.product.weight.format}")
    private BigDecimal weight;

    @Size(max = 100, message = "{validation.product.dimensions.size}")
    private String dimensions;

    private ProductStatus status;

    private Long updatedSeq;

    private String currentImageUrl;

    // 파일 업로드용 필드
    private MultipartFile imageFile;

    // 이미지 삭제 여부
    @Builder.Default
    private Boolean deleteImage = false;


    /**
     * 이미지 파일 존재 여부
     */
    public boolean hasImageFile() {
        return imageFile != null && !imageFile.isEmpty();
    }

    /**
     * 현재 이미지 존재 여부
     */
    public boolean hasCurrentImage() {
        return currentImageUrl != null && !currentImageUrl.trim().isEmpty();
    }

    /**
     * 이미지 삭제 요청 여부
     */
    public boolean isDeleteImageRequested() {
        return deleteImage != null && deleteImage;
    }

    /**
     * 가격 검증
     */
    public boolean isPriceValid() {
        if (unitPrice == null || unitCost == null) {
            return false;
        }
        return unitPrice.compareTo(unitCost) >= 0;
    }
}