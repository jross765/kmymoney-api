package org.kmymoney.api.read.impl.hlp.prc;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Map;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.impl.hlp.fil.FilePriceManager;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
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
			final Map<KMMPriceID, KMyMoneyPrice> prcMap) {
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
			final Map<KMMPriceID, KMyMoneyPrice> prcMap) {
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

    /*
	public static FixedPointNumber getLatestPrice(
			final KMMQualifSecCurrID.Type type, 
			final KMyMoneyFile kmmFile,
			final GncPricedb priceDB,
			final String pCmdtyId) {
		if ( kmmFile == null ) {
			throw new IllegalArgumentException("argument <kmmFile> is null");
		}

		if ( priceDB == null ) {
			throw new IllegalArgumentException("argument <priceDB> is null");
		}

		if ( pCmdtyId == null ) {
			throw new IllegalArgumentException("argument <pCmdtyId> is null");
		}
		
		if ( pCmdtyId.trim().equals("") ) {
			throw new IllegalArgumentException("argument <pCmdtyId> is empty");
		}
		
		return getLatestPrice(new GCshCmdtyCurrID(pCmdtySpace, pCmdtyId),
							  kmmFile, priceDB, 0);
	}
	*/

	// ----------------------------

	private static FixedPointNumber getLatestPrice(
			final KMMQualifSecCurrID secCurrID, 
			final KMyMoneyFile kmmFile,
			final Map<KMMPriceID, KMyMoneyPrice> prcMap,
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
				LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without from-currency: '"
						+ prc.toString() + "'");
				continue;
			}

			if ( toCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without to-currency: '"
						+ prc.toString() + "'");
				continue;
			}

			try {
				if ( prc.getDate() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without date: " + "'"
							+ prc.toString() + "'");
					continue;
				}

				if ( prc.getValue() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without price value: " + "'"
							+ prc.toString() + "'");
					continue;
				}

				if ( !fromSecCurr.getCode().equals(secCurrID.getCode()) ) {
					continue;
				}

				// BEGIN core
				if ( !toCurr.getCode().equals(kmmFile.getDefaultCurrencyID()) ) {
					if ( depth > maxRecursionDepth ) {
						LOGGER.warn("getLatestPrice: Ignoring price-quote that is not in "
								+ kmmFile.getDefaultCurrencyID() + " but in '" + toCurr + "'");
						continue;
					}
					factor = getLatestPrice(new KMMQualifCurrID(toCurr), 
											kmmFile, prcMap,
											depth + 1);
				}
				// END core

				LocalDate date = prc.getDate();

				if ( latestDate == null || latestDate.isBefore(date) ) {
					latestDate = date;
					latestQuote = prc.getValue();
					LOGGER.debug("getLatestPrice: pSecCurrID='" + secCurrID.toString() + "' converted " + latestQuote + " <= " + prc.getValue());
				}

			} catch (NumberFormatException e) {
				LOGGER.error("getLatestPrice: [NumberFormatException]: pSecCurrID='" + secCurrID.toString() + "'! Ignoring a bad price-quote '"
						+ prc.toString() + "'", e);
			} catch (NullPointerException e) {
				LOGGER.error("getLatestPrice: [NullPointerException]: pSecCurrID='" + secCurrID.toString() + "'! Ignoring a bad price-quote '"
						+ prc.toString() + "'", e);
			} catch (ArithmeticException e) {
				LOGGER.error("getLatestPrice: [ArithmeticException]: pSecCurrID='" + secCurrID.toString() + "'! Ignoring a bad price-quote '"
						+ prc.toString() + "'", e);
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

}
