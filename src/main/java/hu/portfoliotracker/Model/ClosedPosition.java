package hu.portfoliotracker.Model;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "CLOSED_POSITION")
public class ClosedPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "SYMBOL")
    private String symbol;

    @Column(name = "SELL_PRICE")
    private BigDecimal sellPrice;

    @Column(name = "DATE")
    private LocalDateTime date;

    @Column(name = "DEPOSIT")
    private BigDecimal deposit;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "AVERAGE_COST_BASIS")
    private BigDecimal averageCostBasis;

    @Column(name = "MARKET_VALUE")
    private BigDecimal marketValue;

    @Column(name = "TRADING_TYPE")
    @Enumerated(EnumType.STRING)
    private TRADING_TYPE tradingType;

    @ManyToOne
    private User user;
}
