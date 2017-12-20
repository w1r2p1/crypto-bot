package net.achfrag.crypto.bot.portfolio;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Tick;

import net.achfrag.crypto.bot.CurrencyEntry;
import net.achfrag.trading.crypto.bitfinex.BitfinexApiBroker;
import net.achfrag.trading.crypto.bitfinex.entity.APIException;
import net.achfrag.trading.crypto.bitfinex.entity.BitfinexCurrencyPair;
import net.achfrag.trading.crypto.bitfinex.entity.BitfinexOrderType;
import net.achfrag.trading.crypto.bitfinex.entity.Wallet;

public class BasePortfolioManager extends PortfolioManager {
	
	/**
	 * The Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(BasePortfolioManager.class);


	public BasePortfolioManager(final BitfinexApiBroker bitfinexApiBroker) {
		super(bitfinexApiBroker);
	}

	/**
	 * Get the used wallet type 
	 * @return
	 */
	@Override
	protected String getWalletType() {
		return Wallet.WALLET_TYPE_EXCHANGE;
	}
	
	/**
	 * Get the used order type
	 * @return 
	 */
	@Override
	protected BitfinexOrderType getOrderType() {
		return BitfinexOrderType.EXCHANGE_STOP;
	}
	
	/**
	 * Get the position size for the symbol
	 * @param symbol
	 * @return
	 * @throws APIException 
	 */
	@Override
	protected double getOpenPositionSizeForCurrency(final String currency) throws APIException {
		final Wallet wallet = getWalletForCurrency(currency);
		return wallet.getBalance();
	}
	
	/**
	 * Calculate the amount of open positions
	 * @param entries
	 * @return
	 */
	@Override
	protected int calculateTotalPositionsForCapitalAllocation(
			final Map<BitfinexCurrencyPair, CurrencyEntry> entries, 
			final Map<BitfinexCurrencyPair, Double> exits) {		
		
		// Executed orders are moved to an extra wallet.
		// So we need only to split the balance of the USD wallet 
		return entries.size();
	}
	
	/**
	 * Get the investment rate
	 */
	@Override
	protected double getInvestmentRate() {
		return 0.9;
	}

	@Override
	protected double getTotalPortfolioValueInUSD() throws APIException {
		final List<Wallet> wallets = getAllWallets();
				
		double totalValue = 0;
		for(final Wallet wallet : wallets) {
			if(wallet.getCurreny().equals("USD")) {
				totalValue = totalValue + wallet.getBalance();
			} else {
				final String symbol = "t" + wallet.getCurreny() + "USD";
				final Tick lastTick = bitfinexApiBroker.getTickerManager().getLastTick(symbol);
				
				if(lastTick != null) {
					final double rate = lastTick.getClosePrice().toDouble();
					final double value = rate * wallet.getBalance();
					totalValue = totalValue + value;
				} else {
					logger.error("Unable to find tick for {}", symbol);
				}
			}
		}
		
		return totalValue;
	}

}
