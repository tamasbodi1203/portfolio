package hu.portfoliotracker.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "SNAPSHOT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "DATE", nullable = false)
    private LocalDate date;

    @Column(name = "ACCOUNT_TOTAL", nullable = false)
    private double accountTotal;

    @ManyToOne
    private User user;
}
