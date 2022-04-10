package hu.portfoliotracker.Service;

import hu.portfoliotracker.DTO.BalanceDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.User;
import hu.portfoliotracker.Repository.TradeRepository;
import hu.portfoliotracker.Utility.ImportHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
public class ImportService {

    @Autowired
    TradeRepository tradeRepository;

    @SneakyThrows
    public MultipartFile fileToMultipartFile() {

        MultipartFile multipartFile = new MockMultipartFile("test.csv", new FileInputStream(new ClassPathResource("csv/eth-export-trade-history.csv").getFile()));
        return multipartFile;
    }

    public boolean importFromFile(MultipartFile file, TRADING_TYPE tradingType) {
        log.info("Kereskedések importálása");
        long startTime = System.nanoTime();
        long stopTime;
        long elpasedTime;

        val extenstion = FilenameUtils.getExtension(file.getOriginalFilename());
        switch (extenstion) {
            case "csv":
                saveCsv(file, tradingType);
                stopTime = System.nanoTime();
                elpasedTime = stopTime - startTime;
                log.info("Kereskedések importálásának vége: " + String.valueOf(elpasedTime / 1000000000) + " seconds");
                return true;


            case "xlsx":
                saveXlsx(file, tradingType);
                stopTime = System.nanoTime();
                elpasedTime = stopTime - startTime;
                log.info("Kereskedések importálásának vége: " + String.valueOf(elpasedTime / 1000000000) + " seconds");
                return true;

            default:
                log.error("Hiba történt a fájl beolvasása során");
                return false;

        }
    }

    public void saveCsv(MultipartFile file, TRADING_TYPE tradingType) {
        try {
            List<Trade> trades = ImportHelper.csvToTrades(file.getInputStream(), tradingType);
            for (Trade t: trades) {
                log.info(t.toString());
            }
            tradeRepository.saveAll(trades);
        } catch (IOException e) {
            throw new RuntimeException("Hiba a CSV fájl beolvasása során: " + e.getMessage());
        }
    }


    public void saveXlsx(MultipartFile file, TRADING_TYPE tradingType) {
        try {
            List<Trade> trades = ImportHelper.xlsxToTrades(file.getInputStream(), tradingType);

            tradeRepository.saveAll(trades);
        } catch (IOException e) {
            throw new RuntimeException("Hiba az XLSX fájl beolvasása során: " + e.getMessage());
        }
    }

}
