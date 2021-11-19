package hu.portfoliotracker.Utility;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.Trade;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {

    public static String TYPE = "text/csv";
    static String[] HEADERs = { "Date(UTC)", "Market", "Type", "Price", "Amount", "Total" };

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static List<Trade> csvToTrades(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

            List<Trade> trades = new ArrayList<Trade>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                Trade trade = Trade.builder()
                        .date(LocalDateTime.parse(csvRecord.get("Date(UTC)"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .pair(csvRecord.get("Market"))
                        .side(csvRecord.get("Type"))
                        .price(Double.parseDouble(csvRecord.get("Price")))
                        .amount(Double.parseDouble(csvRecord.get("Amount")))
                        .total(Double.parseDouble(csvRecord.get("Total")))
                        .build();

                trades.add(trade);
            }

            return trades;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
}
