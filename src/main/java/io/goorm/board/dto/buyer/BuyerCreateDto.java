package io.goorm.board.dto.buyer;

import io.goorm.board.enums.BuyerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerCreateDto {

    private Long buyerSeq;

    @NotBlank(message = "{buyer.validation.companyName.notblank}")
    @Size(max = 100, message = "{buyer.validation.companyName.size}")
    private String companyName;

    @Size(max = 20, message = "{buyer.validation.businessNumber.size}")
    private String businessNumber;

    @Size(max = 50, message = "{buyer.validation.contactPerson.size}")
    private String contactPerson;

    @Email(message = "{buyer.validation.email.format}")
    @Size(max = 255, message = "{buyer.validation.email.size}")
    private String email;

    @Size(max = 20, message = "{buyer.validation.phone.size}")
    private String phone;

    @Size(max = 1000, message = "{buyer.validation.address.size}")
    private String address;

    @PositiveOrZero(message = "{buyer.validation.creditLimit.positive}")
    private BigDecimal creditLimit;

    @PositiveOrZero(message = "{buyer.validation.discountRate.positive}")
    private BigDecimal discountRate;

    @Size(max = 50, message = "{buyer.validation.paymentTerms.size}")
    private String paymentTerms;

    @Size(max = 1000, message = "{buyer.validation.description.size}")
    private String description;

    private BuyerStatus status;
}