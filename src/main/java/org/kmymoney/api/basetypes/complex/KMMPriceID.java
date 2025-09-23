package org.kmymoney.api.basetypes.complex;

import java.time.LocalDate;

import org.kmymoney.api.read.KMyMoneyPricePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KMyMoney has no IDs for the price objects (neither on the price-pair level
 * nor on the price level).
 * <br>
 * I cannot understand this -- how can you possibly work with hundreds, thousands 
 * or even tens of thousands of prices without properly identifying them?
 * <br>
 * Anyway: this fact is the reason why we here have a price object pseudo-ID: 
 * The tuple ( from-currency, to-currency, date ).
 */
public class KMMPriceID extends org.kmymoney.base.basetypes.complex.KMMPriceID
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMMPriceID.class);

    // ---------------------------------------------------------------

	public KMMPriceID(KMyMoneyPricePair prcPr, String dateStr) {
		super(prcPr.getFromSecCurrStr(), prcPr.getToCurrStr(), dateStr);
	}
    
	public KMMPriceID(KMyMoneyPricePair prcPr, LocalDate date) {
		super(prcPr.getFromSecCurrStr(), prcPr.getToCurrStr(), 
			  DATE_FORMAT.format(date));
	}
    
}
