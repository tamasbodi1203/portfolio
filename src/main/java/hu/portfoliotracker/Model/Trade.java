package hu.portfoliotracker.Model;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TRADE_TABLE")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //TODO: Forma validáció szükséges
    @Column(name = "date_col", nullable = false)
    @NotNull(message = "Dátum megadása kötelező")
    private LocalDateTime date;
    @Column(name = "pair_col", nullable = false)
    @Enumerated(EnumType.STRING)
    private CURRENCY_PAIR pair;
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
