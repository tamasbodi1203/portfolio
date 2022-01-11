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
public class ClosedPositionDto {
    private String symbol;

    private double sellPrice;

    private LocalDateTime date;

    private double deposit;

    private double quantity;

    private double averageCostBasis;

    private double marketValue;

    private double realizedGains;

    private double realizedGainsPercent;

    private TRADING_TYPE tradingType;
}
