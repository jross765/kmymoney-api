package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.kmymoney.api.write.KMyMoneyWritablePricePair;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritablePriceImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritablePricePairImpl;
import org.kmymoney.base.basetypes.complex.KMMPriceID;
import org.kmymoney.base.basetypes.complex.KMMPricePairID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePriceManager extends org.kmymoney.api.read.impl.hlp.FilePriceManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FilePriceManager.class);

	// ---------------------------------------------------------------

	public FilePriceManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyPricePairImpl createPricePair(final PRICEPAIR jwsdpPrcPr) {
		KMyMoneyWritablePricePairImpl prcPr = new KMyMoneyWritablePricePairImpl(jwsdpPrcPr,
				  (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createPricePair: Generated new writable price pair: " + prcPr.getID());
		return prcPr;
	}

	// ----------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyPriceImpl createPrice(final KMyMoneyPricePair prcPr, final PRICE jwsdpPrc) {
		KMyMoneyWritablePriceImpl prc = new KMyMoneyWritablePriceImpl(
				new KMyMoneyWritablePricePairImpl((KMyMoneyPricePairImpl) prcPr), 
				jwsdpPrc, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createPrice: Generated new writable price: " + prc.getID());
		return prc;
	}

	protected KMyMoneyPriceImpl createPrice(final KMyMoneyWritablePricePair prcPr, final PRICE jwsdpPrc) {
		KMyMoneyWritablePriceImpl prc = new KMyMoneyWritablePriceImpl(
				prcPr, 
				jwsdpPrc, (KMyMoneyWritableFileImpl) kmmFile);
		LOGGER.debug("createPrice: Generated new writable price: " + prc.getID());
		return prc;
	}

	// ---------------------------------------------------------------

	public void addPricePair(KMyMoneyPricePair prcPr) {
		addPricePair(prcPr, true);
	}

	public void addPricePair(KMyMoneyPricePair prcPr, boolean withPrc) {
		if ( prcPr == null ) {
			throw new IllegalArgumentException("null price pair given");
		}

		prcPrMap.put(prcPr.getID(), prcPr);

		if ( withPrc ) {
			for ( KMyMoneyPrice prc : prcPr.getPrices() ) {
				addPrice(prc, false);
			}
		}

		LOGGER.debug("addPricePair: Added price pair to cache: " + prcPr.getID());
	}

	public void removePricePair(KMyMoneyPricePair prcPr) {
		removePricePair(prcPr, true);
	}

	public void removePricePair(KMyMoneyPricePair prcPr, boolean withPrc) {
		if ( prcPr == null ) {
			throw new IllegalArgumentException("null price pair given");
		}

		if ( withPrc ) {
			for ( KMyMoneyPrice prc : prcPr.getPrices() ) {
				removePrice(prc, false);
			}
		}

		prcPrMap.remove(prcPr.getID());

		LOGGER.debug("removePricePair: Removed price pair from cache: " + prcPr.getID());
	}

	// ---------------------------------------------------------------

	public void addPrice(KMyMoneyPrice prc) {
		addPrice(prc, true);
	}

	public void addPrice(KMyMoneyPrice prc, boolean withPrcPr) {
		if ( prc == null ) {
			throw new IllegalArgumentException("null price given");
		}

		prcMap.put(prc.getID(), prc);
		LOGGER.debug("addPrice: Added price to cache: " + prc.getID());

		if ( withPrcPr ) {
			addPricePair(prc.getParentPricePair(), false);
		}
	}

	public void removePrice(KMyMoneyPrice prc) {
		removePrice(prc, true);
	}

	public void removePrice(KMyMoneyPrice prc, boolean withPrcPr) {
		if ( prc == null ) {
			throw new IllegalArgumentException("null price given");
		}

		// remove price pair as well, if the removed price object 
		// was the last one.
		if ( withPrcPr ) {
			if ( prc.getParentPricePair().getPrices().size() == 0 ) {
				removePricePair(prc.getParentPricePair(), false);
			}
		}

		prcMap.remove(prc.getID());
		LOGGER.debug("removePrice: Removed price from cache: " + prc.getID());
	}

	// ----------------------------
	
	public void removePricePair_raw(final KMMPricePairID prcPairID) {
		for ( int i = 0; i < priceDB.getPRICEPAIR().size(); i++ ) {
			PRICEPAIR jwsdpPrcPr = priceDB.getPRICEPAIR().get(i); 
			if ( jwsdpPrcPr.getFrom().equals(prcPairID.getFromSecCurr().getCode()) &&
				 jwsdpPrcPr.getTo().equals(prcPairID.getToCurr().getCode()) ) {
				priceDB.getPRICEPAIR().remove(i);
				i--;
			}
		}
	}

	public void removePrice_raw(final KMMPriceID prcID) {
		removePrice_raw(prcID, true);
	}
	
	public void removePrice_raw(final KMMPriceID prcID, boolean withPrcPr) {
		KMMPricePairID prcPrID = prcID.getPricePairID();
		PRICEPAIR jwsdpPrcPr = getPricePair_raw(prcPrID);
		for ( int i = 0; i < jwsdpPrcPr.getPRICE().size(); i++ ) {
			PRICE jwsdpPrc = jwsdpPrcPr.getPRICE().get(i); 
			if ( jwsdpPrc.getDate().toString().equals(prcID.getDateStr()) ) {
				jwsdpPrcPr.getPRICE().remove(i);
				i--;
			}
		}
		
		// remove price pair as well, if the removed price object 
		// was the last one.
		if ( withPrcPr ) {
			PRICEPAIR jwsdpPrcPrNow = getPricePair_raw(prcPrID); // get it again, the other one is not up-to-date
			if ( jwsdpPrcPrNow.getPRICE().size() == 0 ) {
				removePricePair_raw(prcPrID);
			}
		}
	}

}
