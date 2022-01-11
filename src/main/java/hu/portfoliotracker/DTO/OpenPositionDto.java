package hu.portfoliotracker.DTO;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OpenPositionDto {

    private String symbol;

    private Long cmcId;

    private double currentPrice;

    private LocalDateTime date;

    private double deposit;

    private double quantity;

    private double averageCostBasis;

    private double marketValue;

    private double unrealizedGains;

    private double unrealizedGainsPercent;

    private TRADING_TYPE tradingType;

}
