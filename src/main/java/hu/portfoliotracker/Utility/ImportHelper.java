package hu.portfoliotracker.Utility;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ImportHelper {

    public static String TYPE = "text/csv";
    static String[] HEADERs = { "Date(UTC)", "Market", "Type", "Price", "Amount", "Total" };

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static List<Trade> csvToTrades(InputStream is, TRADING_TYPE tradingType) {
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
                        .tradingType(tradingType)
                        .build();

                trades.add(trade);
            }

            return trades;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    public static void testXlsxToTrades() {
        try {
            val fileInputStream = new FileInputStream(new ClassPathResource("xlsx/SPOT-2022-01-10 16_53_54.xlsx").getFile());
            val workbook = new XSSFWorkbook(fileInputStream);
            val sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            val trades = new ArrayList<Trade>();

            // Fejléc kihagyása
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                Trade trade = Trade.builder()
                        .date(LocalDateTime.parse(row.getCell(0).getStringCellValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .pair(row.getCell(1).getStringCellValue())
                        .side(row.getCell(2).getStringCellValue())
                        .price(Double.parseDouble(row.getCell(3).getStringCellValue()))
                        .amount(Double.parseDouble(row.getCell(4).getStringCellValue()))
                        .total(Double.parseDouble(row.getCell(4).getStringCellValue()))
                        .tradingType(TRADING_TYPE.SPOT)
                        .build();

                log.info("Kereskedés importálva: " + trade.toString());
                trades.add(trade);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static List<Trade> xlsxToTrades(InputStream fileInputStream, TRADING_TYPE tradingType) {
        val trades = new ArrayList<Trade>();

        try {
            val workbook = new XSSFWorkbook(fileInputStream);
            val sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Fejléc kihagyása
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();

                Trade trade = Trade.builder()
                        .date(LocalDateTime.parse(row.getCell(0).getStringCellValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .pair(row.getCell(1).getStringCellValue())
                        .side(row.getCell(2).getStringCellValue())
                        .price(Double.parseDouble(row.getCell(3).getStringCellValue()))
                        .amount(Double.parseDouble(row.getCell(4).getStringCellValue()))
                        .total(Double.parseDouble(row.getCell(5).getStringCellValue()))
                        .tradingType(tradingType)
                        .build();

                log.info("Kereskedés importálva: " + trade.toString());
                trades.add(trade);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return trades;
    }

}