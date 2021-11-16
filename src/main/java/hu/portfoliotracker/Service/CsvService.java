package hu.portfoliotracker.Service;

import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Repository.TradeRepository;
import hu.portfoliotracker.Utility.CSVHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class CsvService {

    @Autowired
    TradeRepository tradeRepository;

    @SneakyThrows
    public MultipartFile fileToMultipartFile() {

        MultipartFile multipartFile = new MockMultipartFile("test.csv", new FileInputStream(new ClassPathResource("csv/eth-export-trade-history.csv").getFile()));
        return multipartFile;
    }

    public void save(MultipartFile file) {
        try {
            List<Trade> trades = CSVHelper.csvToTrades(file.getInputStream());
            for (Trade t: trades) {
                log.info(t.toString());
            }
            tradeRepository.saveAll(trades);
        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }

}
