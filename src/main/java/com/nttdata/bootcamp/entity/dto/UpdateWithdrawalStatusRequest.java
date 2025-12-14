package com.nttdata.bootcamp.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateWithdrawalStatusRequest {
    @Schema(
            description = "Nuevo estado del retiro",
            example = "COMPLETED",
            required = true
    )
    private String status;
}
