package hu.portfoliotracker.Service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerStatistics;
import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BinanceService {

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("Acp6nGjh0OKrJDhaPxcQN0AIlEnjiIweIQ7ugXNOEh5pyldpwf3YQmM8LRRqrzeP", "A7YmeLWN5NvYqZGUc3CBZ4WGBTun67hZtxBrXrbGbbFLBPy7k6DAdKbFYuzfF0cR");
    BinanceApiRestClient client = factory.newRestClient();

    public double getLastPrice(CURRENCY_PAIR pair) {
        TickerStatistics tickerStatistics = client.get24HrPriceStatistics(pair.name());
        log.info(pair.name() + " latest price: " + tickerStatistics.getLastPrice());
        return Double.parseDouble(tickerStatistics.getLastPrice());
    }

}
