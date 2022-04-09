package hu.portfoliotracker.DTO;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClosedPositionDto {
    private String symbol;

    private BigDecimal sellPrice;

    private LocalDateTime date;

    private BigDecimal deposit;

    private BigDecimal quantity;

    private BigDecimal averageCostBasis;

    private BigDecimal marketValue;

    private BigDecimal realizedGains;

    private BigDecimal realizedGainsPercent;

    private TRADING_TYPE tradingType;
}
