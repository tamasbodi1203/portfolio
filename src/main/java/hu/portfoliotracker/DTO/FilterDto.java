package hu.portfoliotracker.DTO;

import lombok.Data;

@Data
public class FilterDto {

    boolean showSpot = false;
    boolean showCross = false;
    boolean showIsolated = false;

}