package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Transaction {

    private Long id = null;

    @JsonProperty("originator_id")
    private Long originatorId;

    @JsonProperty("beneficiary_id")
    private Long beneficiaryId;

    @JsonProperty("transfer_amount")
    private Float transferAmount;

    private TransactionStatus status;

    private String reason;
}
