package hu.portfoliotracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BalanceDto {

    private List<OpenPositionDto> openPositionDtos;

    private List<ClosedPositionDto> closedPositionDtos;

    private BigDecimal totalDeposit;

    private BigDecimal totalUnrealizedGains;

    private BigDecimal totalUnrealizedGainsPercent;

    private BigDecimal totalRealizedGains;

    private BigDecimal totalRealizedGainsPercent;

}
