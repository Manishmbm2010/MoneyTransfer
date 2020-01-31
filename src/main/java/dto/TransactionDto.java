package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import model.TransactionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionDto {

    @JsonProperty("originator_id")
    private Long originatorId;

    @JsonProperty("beneficiary_id")
    private Long beneficiaryId;

    @JsonProperty("transfer_amount")
    private Float transferAmount;

    private TransactionStatus status;
}
