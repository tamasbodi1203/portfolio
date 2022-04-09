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
public class OpenPositionDto {

    private String symbol;

    private Long cmcId;

    private BigDecimal currentPrice;

    private LocalDateTime date;

    private BigDecimal deposit;

    private BigDecimal quantity;

    private BigDecimal averageCostBasis;

    private BigDecimal marketValue;

    private BigDecimal unrealizedGains;

    private BigDecimal unrealizedGainsPercent;

    private TRADING_TYPE tradingType;

}
