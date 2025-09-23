package org.kmymoney.api.read.impl.hlp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.Const;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.generated.PRICES;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class FilePriceManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FilePriceManager.class);

	public static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

	private static final int RECURS_DEPTH_MAX = 5; // ::MAGIC

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected PRICES                                 priceDB  = null;
	protected Map<KMMPricePairID, KMyMoneyPricePair> prcPrMap = null;
	protected Map<KMMPriceID, KMyMoneyPrice>         prcMap   = null;

	// ---------------------------------------------------------------

	public FilePriceManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		prcPrMap = new HashMap<KMMPricePairID, KMyMoneyPricePair>();
		prcMap   = new HashMap<KMMPriceID, KMyMoneyPrice>();

		initPriceDB(pRootElement);
		List<PRICEPAIR> prices = priceDB.getPRICEPAIR();
		for ( PRICEPAIR jwsdpPrcPr : prices ) {
			String fromCurr = jwsdpPrcPr.getFrom();
			String toCurr = jwsdpPrcPr.getTo();
			KMMPricePairID currPair = new KMMPricePairID(fromCurr, toCurr);
			KMyMoneyPricePair pricePair = createPricePair(jwsdpPrcPr);
			prcPrMap.put(currPair, pricePair);
			for ( PRICE jwsdpPrc : jwsdpPrcPr.getPRICE() ) {
				XMLGregorianCalendar cal = jwsdpPrc.getDate();
				if ( cal != null ) {
					LocalDate date = LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
					String dateStr = date.toString();
					KMMPriceID priceID = new KMMPriceID(fromCurr, toCurr, dateStr);
					KMyMoneyPriceImpl price = createPrice(pricePair, jwsdpPrc);
					prcMap.put(priceID, price);
				} else {
					LOGGER.error("init: Found Price without or with invalid date: (" + fromCurr + "/" + toCurr + ")");
				}
			}
		}

		LOGGER.debug("init: No. of entries in price pair map: " + prcPrMap.size());
		LOGGER.debug("init: No. of entries in price map: " + prcMap.size());
	}

	private void initPriceDB(final KMYMONEYFILE pRootElement) {
		priceDB = pRootElement.getPRICES();
	}

	protected KMyMoneyPricePairImpl createPricePair(final PRICEPAIR jwsdpPrcPr) {
		KMyMoneyPricePairImpl prcPr = new KMyMoneyPricePairImpl(jwsdpPrcPr, kmmFile);
		LOGGER.debug("createPricePair: Generated new price pair: " + prcPr.getID());
		return prcPr;
	}

	protected KMyMoneyPriceImpl createPrice(final KMyMoneyPricePair prcPr, final PRICE jwsdpPrc) {
		KMyMoneyPriceImpl prc = new KMyMoneyPriceImpl(prcPr, jwsdpPrc, kmmFile);
		LOGGER.debug("createPrice: Generated new price: " + prc.getID());
		return prc;
	}

	// ---------------------------------------------------------------

	public PRICES getPriceDB() {
		return priceDB;
	}
	
	// ----------------------------

	public KMyMoneyPricePair getPricePairByID(KMMPricePairID prcPrID) {
		if ( prcPrID == null ) {
			throw new IllegalArgumentException("argument <prcPrID> is null");
		}

		if ( ! prcPrID.isSet() ) {
			throw new IllegalArgumentException("argument <prcPrID> is not set");
		}

		if ( prcPrMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}
		
		// CAUTION: The following line does not work reliably in all relevant cases.
		// That has to do with the intricacies of the definition of KMMCurrPair
		// (cf. test cases for KMMCurrPair):
		// return prcPrMap.get(prcPrID);
		
		// Instead:
		for ( KMMPricePairID elt : prcPrMap.keySet() ) {
			if ( elt.toString().equals(prcPrID.toString()) ) { // <-- important: toString()
				return prcPrMap.get(elt);
			}
		}
		
		return null;
	}

	public Collection<KMyMoneyPricePair> getPricePairs() {
		if ( prcPrMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		// Caution: Yes, sorting necessary.
		// Cf. comment in FileAccountManager.getAccounts().
		ArrayList<KMyMoneyPricePair> temp = new ArrayList<KMyMoneyPricePair>(prcPrMap.values());
		Collections.sort(temp);
		return Collections.unmodifiableCollection(temp);
	}

	// ----------------------------

	public KMyMoneyPrice getPriceByID(KMMPriceID prcID) {
		if ( prcID == null ) {
			throw new IllegalArgumentException("argument <prcID> is null");
		}

		if ( ! prcID.isSet() ) {
			throw new IllegalArgumentException("argument <prcID> is not set");
		}

		if ( prcMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyPrice retval = prcMap.get(prcID);
		if ( retval == null ) {
			LOGGER.warn("getPriceByID: No Price with ID '" + prcID + "'. We know " + prcMap.size() + " prices.");
		}

		return retval;
	}

	// ---------------------------------------------------------------
	
	public KMyMoneyPrice getPriceBySecIDDate(final KMMSecID secID, final LocalDate date) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}
		
		KMMQualifSecID qualifID = new KMMQualifSecID(secID);
		return getPriceByQualifSecIDDate(qualifID, date);
	}
	
	public KMyMoneyPrice getPriceByQualifSecIDDate(final KMMQualifSecID secID, final LocalDate date) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}
		
		return getPriceByQualifSecCurrIDDate(secID, date);
	}
	
	public KMyMoneyPrice getPriceByCurrDate(final Currency curr, final LocalDate date) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}
		
		KMMQualifCurrID qualifID = new KMMQualifCurrID(curr);
		return getPriceByQualifCurrIDDate(qualifID, date);
	}

	public KMyMoneyPrice getPriceByQualifCurrIDDate(final KMMQualifCurrID currID, final LocalDate date) {
		if ( currID == null ) {
			throw new IllegalArgumentException("argument <currID> is null");
		}
		
		return getPriceByQualifSecCurrIDDate(currID, date);
	}

	// ----------------------------
	// The core function for the price retrieval by sec/curr-ID and date.
	// About the two variants cf. the following comments
	// Apart from this deviation, all the getPriceBy(Qualif)Sec(Curr)IDDate() 
	// functions are almost perfectly symmetrical to their resp. siblings 
	// in the sister project/module "JGnuCashLib/gnncash-api", which
	// also is s.t. that we want to achieve (thus, a second reason
	// we were reluctant to introduce the second variant).

	public KMyMoneyPrice getPriceByQualifSecCurrIDDate(final KMMQualifSecCurrID qualifID, final LocalDate date) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("argument <qualifID> is null");
		}
		
		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("argument <qualifID> is not set");
		}
		
		// clean but inefficient
		// return getPriceByQualifSecCurrIDDate_Var1(qualifID, date);
		// dirty but efficient
		return getPriceByQualifSecCurrIDDate_Var2(qualifID, date);
	}

	// ----------------------------
	// This is the "clean" but inefficient variant of the core function above, 
	// i.e. the ones that does *not* make use of the semantics 
	// in the KMyMoney-Price-IDs (s.t. that you generally should not do, 
	// not just in this particular case -- we consider semantics in IDs 
	// a bad design decision, although one seen quite often in the wild).
	
	@SuppressWarnings("unused")
	private KMyMoneyPrice getPriceByQualifSecCurrIDDate_Var1(final KMMQualifSecCurrID qualifID, final LocalDate date) {
		for ( KMyMoneyPrice prc : getPricesByQualifSecCurrID(qualifID) ) {
			if ( prc.getDate().equals(date) ) {
				return prc;
			}
		}
		
		return null;
	}

	// ---------------------------------------------------------------
	// This is the "dirty" but efficient variant of the core function above, 
	// i.e. the one that *does* make use of the semantics 
	// in the KMyMoney-Price-IDs (s.t. that you generally should not do, 
	// not just in this particular case. But we will make and exception here,
	// because things are as they are in KMyMoney, we cannot change it, so
	// why not make use of this not-so-good design decision of the KMyMoney 
	// developers?). Just make sure to keep the clean variant.
	
	private KMyMoneyPrice getPriceByQualifSecCurrIDDate_Var2(final KMMQualifSecCurrID qualifID, final LocalDate date) {
		KMMQualifCurrID toCurrID = new KMMQualifCurrID(Const.DEFAULT_CURRENCY);
		KMMPriceID prcID = new KMMPriceID(qualifID, toCurrID, date); 
		
		return getPriceByID(prcID);
	}
	
	// ---------------------------------------------------------------

	public List<KMyMoneyPrice> getPrices() {
		if ( prcMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		ArrayList<KMyMoneyPrice> temp = new ArrayList<KMyMoneyPrice>(prcMap.values());
		Collections.sort(temp);
		
		return Collections.unmodifiableList(temp);
	}

	public List<KMyMoneyPrice> getPricesBySecID(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("argument <secID> is null");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("argument <secID> is not set");
		}
		
		KMMQualifSecID qualifID = new KMMQualifSecID(secID);
		return getPricesByQualifSecCurrID(qualifID);
	}

	public List<KMyMoneyPrice> getPricesByCurr(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}
		
		KMMQualifCurrID qualifID = new KMMQualifCurrID(curr);
		return getPricesByQualifSecCurrID(qualifID);
	}

	public List<KMyMoneyPrice> getPricesByQualifSecCurrID(final KMMQualifSecCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("argument <qualifID> is null");
		}
		
		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("argument <qualifID> is not set");
		}
		
		List<KMyMoneyPrice> result = new ArrayList<KMyMoneyPrice>();

		for ( KMyMoneyPrice prc : getPrices() ) {
			if ( prc.getFromSecCurrQualifID().toString().equals(qualifID.toString()) ) {
				result.add(prc);
			}
		}
		
		Collections.sort(result, Collections.reverseOrder()); // descending, i.e. youngest first
		return Collections.unmodifiableList(result);
	}

	// ---------------------------------------------------------------

	public FixedPointNumber getLatestPrice(final String secCurrIDStr) {
		if ( secCurrIDStr == null ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is null");
		}

		if ( secCurrIDStr.trim().equals("") ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is empty");
		}

		if ( secCurrIDStr.startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) ) {
			return getLatestPrice(new KMMQualifSecID(secCurrIDStr));
		} else {
			return getLatestPrice(new KMMQualifCurrID(secCurrIDStr));
		}
	}

	public FixedPointNumber getLatestPrice(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		return getLatestPrice(secCurrID, 0);
	}

	// ----------------------------

	/**
	 * @param pCmdtySpace the name space for pCmdtyId
	 * @param pCmdtyId    the currency-name
	 * @param depth       used for recursion. Always call with '0' for aborting
	 *                    recursive quotes (quotes to other then the base- currency)
	 *                    we abort if the depth reached 6.
	 * @return the latest price-quote in the KMyMoney file in the default-currency
	 * @see {@link KMyMoneyFile#getLatestPrice(String, String)}
	 * @see #getDefaultCurrencyID()
	 */
	private FixedPointNumber getLatestPrice(final KMMQualifSecCurrID secCurrID, final int depth) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		// System.err.println("depth: " + depth);

		LocalDate latestDate = null;
		FixedPointNumber latestQuote = null;
		FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the quote is not to our base-currency
		final int maxRecursionDepth = RECURS_DEPTH_MAX;

		for ( KMyMoneyPrice prc : prcMap.values() ) {
			KMMQualifSecCurrID fromSecCurr = prc.getParentPricePairID().getFromSecCurr();
			KMMQualifCurrID toCurr = prc.getParentPricePairID().getToCurr();

			if ( fromSecCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney-file contains price-quotes without from-currency: '"
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
					factor = getLatestPrice(new KMMQualifCurrID(toCurr), depth + 1);
				}
				// END core

				LocalDate date = prc.getDate();

				if ( latestDate == null || latestDate.isBefore(date) ) {
					latestDate = date;
					latestQuote = prc.getValue();
					LOGGER.debug("getLatestPrice: pSecCurrId='" + secCurrID.toString() + "' converted " + latestQuote
							+ " <= " + prc.getValue());
				}

			} catch (NumberFormatException e) {
				LOGGER.error("getLatestPrice: [NumberFormatException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			} catch (NullPointerException e) {
				LOGGER.error("getLatestPrice: [NullPointerException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			} catch (ArithmeticException e) {
				LOGGER.error("getLatestPrice: [ArithmeticException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			}
		} // for

		LOGGER.debug("getLatestPrice: pSecCurrId='" + secCurrID.toString() + "'= " + latestQuote + " from " + latestDate);

		if ( latestQuote == null ) {
			return null;
		}

		if ( factor == null ) {
			factor = new FixedPointNumber(1);
		}

		return factor.multiply(latestQuote);
	}

	@SuppressWarnings("unused")
	private FixedPointNumber getLatestPrice_readAfresh(final KMMQualifSecCurrID secCurrID, final int depth) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		// System.err.println("depth: " + depth);

		LocalDate latestDate = null;
		FixedPointNumber latestQuote = null;
		FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the quote is not to our base-currency
		final int maxRecursionDepth = RECURS_DEPTH_MAX;

		for ( PRICEPAIR pricePair : priceDB.getPRICEPAIR() ) {
			String fromSecCurr = pricePair.getFrom();
			String toCurr = pricePair.getTo();
			// System.err.println("pricepair: " + fromCurr + " -> " + toCurr);

			if ( fromSecCurr == null ) {
				LOGGER.warn("getLatestPrice_readAfresh: KMyMoney-file contains price-quotes without from-currency: '"
						+ pricePair.toString() + "'");
				continue;
			}

			if ( toCurr == null ) {
				LOGGER.warn("getLatestPrice_readAfresh: KMyMoney file contains price-quotes without to-currency: '"
						+ pricePair.toString() + "'");
				continue;
			}

			for ( PRICE prc : pricePair.getPRICE() ) {
				if ( prc == null ) {
					LOGGER.warn(
							"getLatestPrice_readAfresh: KMyMoney file contains null price-quotes - there may be a problem with JWSDP");
					continue;
				}

				try {
					if ( prc.getDate() == null ) {
						LOGGER.warn("getLatestPrice_readAfresh: KMyMoney file contains price-quotes without date: "
								+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")");
						continue;
					}

					if ( prc.getPrice() == null ) {
						LOGGER.warn(
								"getLatestPrice_readAfresh: KMyMoney file contains price-quotes without price value: "
										+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")");
						continue;
					}

					if ( !fromSecCurr.equals(secCurrID.getCode()) ) {
						continue;
					}

					// BEGIN core
					if ( toCurr.startsWith(KMMQualifSecCurrID.PREFIX_SECURITY) ) {
						// is security
						if ( depth > maxRecursionDepth ) {
							LOGGER.warn(
									"getLatestPrice_readAfresh: Ignoring price-quote that is not in an ISO4217-currency"
											+ " but in '" + toCurr + "'");
							continue;
						}
						factor = getLatestPrice(new KMMQualifSecID(toCurr), depth + 1);
					} else {
						// is currency
						if ( !toCurr.equals(kmmFile.getDefaultCurrencyID()) ) {
							if ( depth > maxRecursionDepth ) {
								LOGGER.warn("getLatestPrice_readAfresh: Ignoring price-quote that is not in "
										+ kmmFile.getDefaultCurrencyID() + " but in '" + toCurr + "'");
								continue;
							}
							factor = getLatestPrice(new KMMQualifCurrID(toCurr), depth + 1);
						}
					}
					// END core

					XMLGregorianCalendar dateCal = prc.getDate();
					LocalDate date = LocalDate.of(dateCal.getYear(), dateCal.getMonth(), dateCal.getDay());

					if ( latestDate == null || latestDate.isBefore(date) ) {
						latestDate = date;
						latestQuote = new FixedPointNumber(prc.getPrice());
						LOGGER.debug("getLatestPrice_readAfresh: pSecCurrId='" + secCurrID.toString() + "' converted "
								+ latestQuote + " <= " + prc.getPrice());
					}

				} catch (NumberFormatException e) {
					LOGGER.error("getLatestPrice_readAfresh: [NumberFormatException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				} catch (NullPointerException e) {
					LOGGER.error("getLatestPrice_readAfresh: [NullPointerException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				} catch (ArithmeticException e) {
					LOGGER.error("getLatestPrice_readAfresh: [ArithmeticException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				}
			} // for price
		} // for pricepair

		LOGGER.debug("getLatestPrice_readAfresh: pSecCurrId='" + secCurrID.toString() + "'= " + latestQuote + " from "
				+ latestDate);

		if ( latestQuote == null ) {
			return null;
		}

		if ( factor == null ) {
			factor = new FixedPointNumber(1);
		}

		return factor.multiply(latestQuote);
	}

	// ----------------------------
	
	private List<PRICEPAIR> getPricePairs_raw() {
		List<PRICEPAIR> result = new ArrayList<PRICEPAIR>();

		for ( PRICEPAIR jwsdpPrcPr : priceDB.getPRICEPAIR() ) {
			result.add(jwsdpPrcPr);
		}

		return result;
	}

	protected PRICEPAIR getPricePair_raw(final KMMPricePairID prcPairID) {
		for ( PRICEPAIR jwsdpPrcPr : getPricePairs_raw() ) {
			if ( jwsdpPrcPr.getFrom().equals(prcPairID.getFromSecCurr().getCode()) &&
				 jwsdpPrcPr.getTo().equals(prcPairID.getToCurr().getCode()) ) {
				return jwsdpPrcPr;
			}
		}
		
		return null;
	}

	// ----------------------------
	
	@SuppressWarnings("unused")
	private List<PRICE> getPrices_raw(final KMMPricePairID prcPairID) {
		List<PRICE> result = new ArrayList<PRICE>();

		PRICEPAIR prcPr = getPricePair_raw(prcPairID);
		for ( PRICE jwsdpPrc : prcPr.getPRICE() ) {
			result.add(jwsdpPrc);
		}

		return result;
	}

	protected PRICE getPrice_raw(final KMMPriceID prcID) {
		KMMPricePairID prcPrID = prcID.getPricePairID();
		PRICEPAIR jwsdpPrcPr = getPricePair_raw(prcPrID);
		for ( PRICE jwsdpPrc : jwsdpPrcPr.getPRICE() ) {
			if ( jwsdpPrc.getDate().toString().equals(prcID.getDateStr()) ) {
				return jwsdpPrc;
			}
		}
		
		return null;
	}

	// ---------------------------------------------------------------

	public int getNofEntriesPricePairMap() {
		return prcPrMap.size();
	}

	public int getNofEntriesPriceMap() {
		return prcMap.size();
	}

}
