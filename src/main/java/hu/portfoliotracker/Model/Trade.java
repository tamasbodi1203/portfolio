package hu.portfoliotracker.Model;

import hu.portfoliotracker.Enum.CurrencyPair;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;

@Entity
@Data
@Table(name = "TRADE_TABLE")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "date_col", nullable = false)
    @NotNull(message = "Dátum megadása kötelező")
    @PastOrPresent (message = "A dátum nem lehet későbbi időpont, mint a mai nap")
    private Date date;
    @Column(name = "pair_col", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyPair pair;
    @Column(name = "side_col", nullable = false)
    @NotEmpty(message = "Az oldal nem lehet üres")
    private String side;

    //FIXME: A mező üresen hagyása errort dob
    @Min(value = 0, message = "Az ár nem lehet negatív")
    @Column(name = "price_col", nullable = false)
    private double price;
    @Min(value = 0, message = "A mennyiség nem lehet negatív")
    @Column(name = "amount_col", nullable = false)
    private double amount;
    @Column(name = "total_col", nullable = false)
    private double total;

}
