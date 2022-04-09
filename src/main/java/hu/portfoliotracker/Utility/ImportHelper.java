package hu.portfoliotracker.Utility;

import hu.portfoliotracker.Controller.AuthController;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ImportHelper {

    public static final String UTF8_BOM = "\uFEFF";
    public static String TYPE = "text/csv";
    static String[] HEADERs = { "Date(UTC)", "Market", "Type", "Price", "Amount", "Total" };

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    @SneakyThrows
    public static List<Trade> csvToTrades(InputStream is, TRADING_TYPE tradingType) {
        val trades = new ArrayList<Trade>();
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        switch (tradingType){
            case SPOT:
                try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                     CSVParser csvParser = new CSVParser(fileReader,
                             CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

                    Iterable<CSVRecord> csvRecords = csvParser.getRecords();

                    for (CSVRecord csvRecord : csvRecords) {
                        Trade trade = Trade.builder()
                                .user(user)
                                .date(LocalDateTime.parse(csvRecord.get("Date(UTC)"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .pair(csvRecord.get("Market"))
                                .side(csvRecord.get("Type"))
                                .price(new BigDecimal(csvRecord.get("Price")))
                                .amount(new BigDecimal(Long.parseLong(csvRecord.get("Amount"))))
                                .total(new BigDecimal(Long.parseLong(csvRecord.get("Total"))))
                                .tradingType(tradingType)
                                .build();

                        trades.add(trade);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
                }
                break;

            default:
                try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
                     CSVParser csvParser = new CSVParser(fileReader,
                             CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

                    Iterable<CSVRecord> csvRecords = csvParser.getRecords();

                    for (CSVRecord csvRecord : csvRecords) {

                        Trade trade = Trade.builder()
                                .user(user)
                                .date(LocalDateTime.parse(csvRecord.get(0), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .pair(csvRecord.get(1))
                                .side(csvRecord.get(2))
                                // Szimbólumok törlése a mezők végéről
                                .price(new BigDecimal(csvRecord.get(3).replaceAll("[^0-9.]", "")))
                                .amount(new BigDecimal(csvRecord.get(4).replaceAll("[^0-9.]", "")))
                                .total(new BigDecimal(csvRecord.get(5).replaceAll("[^0-9.]", "")))
                                .tradingType(tradingType)
                                .build();

                        trades.add(trade);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
                }
                break;
        }
        return trades;
    }


    public static List<Trade> xlsxToTrades(InputStream fileInputStream, TRADING_TYPE tradingType) {
        val trades = new ArrayList<Trade>();
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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

                // TODO: Deviza valuták figyelmen kívül hagyása
                Trade trade = Trade.builder()
                        .user(user)
                        .date(LocalDateTime.parse(row.getCell(0).getStringCellValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .pair(row.getCell(1).getStringCellValue())
                        .side(row.getCell(2).getStringCellValue())
                        .price(new BigDecimal(row.getCell(3).getStringCellValue()))
                        .amount(new BigDecimal(row.getCell(4).getStringCellValue()))
                        .total(new BigDecimal(row.getCell(5).getStringCellValue()))
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

    private static boolean isContainBOM(InputStream is) throws IOException {

        boolean result = false;

        byte[] bom = new byte[3];
        try (is) {

            // read 3 bytes of a file.
            is.read(bom);

            // BOM encoded as ef bb bf
            String content = new String(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(content)) {
                result = true;
            }

        }

        return result;
    }


    private static InputStream removeBom(InputStream is) throws IOException {

        if (isContainBOM(is)) {

            byte[] bytes = new byte[is.available()];

            ByteBuffer bb = ByteBuffer.wrap(bytes);

            System.out.println("Found BOM!");

            byte[] bom = new byte[3];
            // get the first 3 bytes
            bb.get(bom, 0, bom.length);

            // remaining
            byte[] contentAfterFirst3Bytes = new byte[bytes.length - 3];
            bb.get(contentAfterFirst3Bytes, 0, contentAfterFirst3Bytes.length);

            System.out.println("Remove the first 3 bytes, and overwrite the file!");

            // override the same path
            //Files.write(path, contentAfterFirst3Bytes);
            InputStream targetStream = new ByteArrayInputStream(contentAfterFirst3Bytes);

            return targetStream;

        } else {
            System.out.println("This file doesn't contains UTF-8 BOM!");
        }

        return InputStream.nullInputStream();
    }

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

}