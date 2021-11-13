package hu.portfoliotracker.Model;

import lombok.Data;

import java.util.Date;

@Data
public class Trade {

    private Long id;
    private Date date;
    private String pair;
    private String side;
    private double price;
    private double amount;
    private double total;

}
