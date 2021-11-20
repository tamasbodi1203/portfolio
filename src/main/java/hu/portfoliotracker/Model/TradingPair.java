package hu.portfoliotracker.Model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "TRADING_PAIR")
public class TradingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "SYMBOL")
    private String symbol;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "BASE_ASSET")
    private String baseAsset;

    @Column(name = "QUOTE_ASSET")
    private String quoteAsset;
}
