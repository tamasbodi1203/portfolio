package hu.portfoliotracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PortfolioDto {

    private List<OpenPositionDto> openPositionDtos;

    private List<ClosedPositionDto> closedPositionDtos;

    private Double totalDeposit;

    private Double totalUnrealizedGains;

    private Double totalUnrealizedGainsPercent;

    private Double totalRealizedGains;

    private Double totalRealizedGainsPercent;

}
