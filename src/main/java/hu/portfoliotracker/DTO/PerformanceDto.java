package hu.portfoliotracker.DTO;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PerformanceDto {

    private LocalDate date;

    private TRADING_TYPE trading_type;

    private List<OpenPositionDto> openPositionDtos;

    private List<ClosedPositionDto> closedPositionDtos;

    private Double totalValue = Double.valueOf(0);
}
