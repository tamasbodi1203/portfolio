package hu.portfoliotracker.Model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CRYPTOCURRENCY")
public class Cryptocurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CURRENCY")
    //@Enumerated(EnumType.STRING)
    private String currency;

    @Column(name = "CURRENT_PRICE")
    private double currentPrice;
}
