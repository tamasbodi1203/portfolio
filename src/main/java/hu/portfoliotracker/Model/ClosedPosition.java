package hu.portfoliotracker.Model;

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
@Table(name = "CLOSED_POSITION_TABLE")
public class ClosedPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "pair_col")
    @Enumerated(EnumType.STRING)
    private CURRENCY_PAIR pair;
    @Column(name = "sellPrice_col")
    private double sellPrice;
    @Column(name = "date_col")
    private LocalDateTime date;
    @Column(name = "deposit_col")
    private double deposit;
    @Column(name = "quantity_col")
    private double quantity;
    @Column(name = "averageCostBasis_col")
    private double averageCostBasis;
    @Column(name = "marketValue_col")
    private double marketValue;
    @Column(name = "profit_col")
    private double profit;
}
