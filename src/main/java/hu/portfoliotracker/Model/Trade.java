package hu.portfoliotracker.Model;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TRADE")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //TODO: Forma validáció szükséges
    @Column(name = "DATE", nullable = false)
    //@NotNull(message = "Dátum megadása kötelező")
    private LocalDateTime date;

    @Column(name = "PAIR", nullable = false)
    //@Enumerated(EnumType.STRING)
    private String pair;

    @Column(name = "SIDE", nullable = false)
    @NotEmpty(message = "Az oldal nem lehet üres")
    private String side;

    //FIXME: A mező üresen hagyása errort dob
    @Min(value = 0, message = "Az ár nem lehet negatív")
    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    @Min(value = 0, message = "A mennyiség nem lehet negatív")
    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Column(name = "TOTAL", nullable = false)
    private BigDecimal total;

    @Column(name = "TRADING_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private TRADING_TYPE tradingType;

    @ManyToOne
    private User user;

}
