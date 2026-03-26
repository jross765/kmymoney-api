package org.kmymoney.api.read.impl.hlp.prc;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.hlp.fil.FilePriceManager;
import org.kmymoney.base.basetypes.complex.KMMPrcID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class PriceHelper_FP {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PriceHelper_FP.class);
    
	// ---------------------------------------------------------------

    private static final int RECURS_DEPTH_MAX = FilePriceManager.RECURS_DEPTH_MAX;
    
	// ---------------------------------------------------------------

    public static FixedPointNumber getLatestPrice(
			final KMMQualifSecCurrID secCurrID,
			final KMyMoneyFile kmmFile,
			final Map<KMMPrcID, KMyMoneyPrice> prcMap) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		if ( kmmFile == null ) {
			throw new IllegalArgumentException("argument <kmmFile> is null");
		}

		if ( prcMap == null ) {
			throw new IllegalArgumentException("argument <prcMap> is null");
		}

		return getLatestPrice(secCurrID,
							  kmmFile, prcMap, 0);
	}

    public static FixedPointNumber getLatestPrice(
			final Currency curr,
			final KMyMoneyFile kmmFile,
			final Map<KMMPrcID, KMyMoneyPrice> prcMap) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}
		
		if ( kmmFile == null ) {
			throw new IllegalArgumentException("argument <kmmFile> is null");
		}

		if ( prcMap == null ) {
			throw new IllegalArgumentException("argument <prcMap> is null");
		}

		return getLatestPrice(new KMMQualifCurrID(curr),
							  kmmFile, prcMap, 0);
	}

	// ----------------------------

	private static FixedPointNumber getLatestPrice(
			final KMMQualifSecCurrID secCurrID, 
			final KMyMoneyFile kmmFile,
			final Map<KMMPrcID, KMyMoneyPrice> prcMap,
			final int depth) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		if ( kmmFile == null ) {
			throw new IllegalArgumentException("argument <kmmFile> is null");
		}

		if ( prcMap == null ) {
			throw new IllegalArgumentException("argument <prcMap> is null");
		}

		if ( depth < 0 ) {
			throw new IllegalArgumentException("argument <depth> is < 0");
		}

		LocalDate latestDate = null;
		FixedPointNumber latestQuote = null;
		FixedPointNumber factor = FixedPointNumber.ONE.copy(); // factor is used if the quote is not to our base-currency
		final int maxRecursionDepth = RECURS_DEPTH_MAX;

		for ( KMyMoneyPrice prc : prcMap.values() ) {
			KMMQualifSecCurrID fromSecCurr = prc.getParentPricePairID().getFromSecCurr();
			KMMQualifCurrID toCurr = prc.getParentPricePairID().getToCurr();

			if ( fromSecCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without from-security/currency: '" + prc.toString() + "'");
				continue;
			}

			if ( toCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without to-currency: '" + prc.toString() + "'");
				continue;
			}

			try {
				if ( prc.getDate() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without date: '" + prc.toString() + "'");
					continue;
				}

				if ( prc.getValue() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without price value: '" + prc.toString() + "'");
					continue;
				}

				if ( ! fromSecCurr.getCode().equals( secCurrID.getCode() ) ) {
					continue;
				}

				// BEGIN core
				if ( toCurr.getType() != KMMQualifSecCurrID.Type.CURRENCY ) {
					// is security
					if ( depth > maxRecursionDepth ) {
						LOGGER.warn("getLatestPrice: Ignoring price-quote that is not an ISO4217 currency: '" + toCurr.toString() + "'");
						continue;
					}
					factor = getLatestPrice(new KMMQualifSecID(toCurr.getCode()), 
											kmmFile, prcMap, 
											depth + 1);
				} else {
					// is currency
					if ( ! toCurr.getCode().equals( kmmFile.getDefaultCurrencyID().get().getCurrencyCode() ) ) {
						if ( depth > maxRecursionDepth ) {
							LOGGER.warn("getLatestPrice: Ignoring price-quote that is not in default currency " + kmmFile.getDefaultCurrencyID() +
									" but in '" + toCurr.toString() + "'");
							continue;
						}
						factor = getLatestPrice(new KMMQualifCurrID(toCurr), 
												kmmFile, prcMap,
												depth + 1);
					}
				}
				// END core

				LocalDate date = prc.getDate();

				if ( latestDate == null || latestDate.isBefore(date) ) {
					latestDate = date;
					latestQuote = prc.getValue();
					LOGGER.debug("getLatestPrice: pSecCurrID='" + secCurrID.toString() + "' converted " + latestQuote + " <= " + prc.getValue());
				}

			} catch (Exception e) {
				LOGGER.error("getLatestPrice: pSecCurrID='" + secCurrID.toString() + "'! Ignoring a bad price-quote '" + prc.toString() + "'", e);
			}
		} // for

		LOGGER.debug("getLatestPrice: pSecCurrID='" + secCurrID.toString() + "' = " + latestQuote + " from " + latestDate);

		if ( latestQuote == null ) {
			return null;
		}

		if ( factor == null ) {
			factor = FixedPointNumber.ONE.copy();
		}

		return factor.multiply(latestQuote);
	}

	// ---------------------------------------------------------------
	// Helpers -- balance pre-computed
	
	public static String formatValue(KMyMoneyPriceImpl prc, FixedPointNumber val) {
		Locale lcl = Locale.getDefault();
		return formatValue(prc, val, lcl);
	}
	
	public static String formatValue(KMyMoneyPriceImpl prc, FixedPointNumber val, Locale lcl) {
		NumberFormat nf = prc.getToCurrencyFormat(lcl);
		nf.setCurrency(Currency.getInstance(prc.getToCurrencyQualifID().getCode()));
		return nf.format(val.getBigDecimal());
	}

}
