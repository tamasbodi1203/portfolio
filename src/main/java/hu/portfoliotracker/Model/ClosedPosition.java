package hu.portfoliotracker.Model;

import hu.portfoliotracker.Enum.CURRENCY;
import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
    //@Enumerated(EnumType.STRING)
    private String symbol;
    @Column(name = "SELL_PRICE")
    private double sellPrice;
    @Column(name = "DATE")
    private LocalDateTime date;
    @Column(name = "DEPOSIT")
    private double deposit;
    @Column(name = "QUANTITY")
    private double quantity;
    @Column(name = "AVERAGE_COST_BASIS")
    private double averageCostBasis;
    @Column(name = "MARKET_VALUE")
    private double marketValue;
    @Column(name = "PROFIT")
    private double profit;
}
