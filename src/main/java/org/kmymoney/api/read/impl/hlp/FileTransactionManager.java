package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransactionManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileTransactionManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	protected Map<KMMTrxID, KMyMoneyTransaction>             trxMap;
	protected Map<KMMQualifSpltID, KMyMoneyTransactionSplit> trxSpltMap;

	// ---------------------------------------------------------------

	public FileTransactionManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		init1(pRootElement);
		init2(pRootElement);
	}

	private void init1(final KMYMONEYFILE pRootElement) {
		trxMap = new HashMap<KMMTrxID, KMyMoneyTransaction>();

		for ( KMyMoneyTransactionImpl trx : getTransactions_readAfresh() ) {
			trxMap.put(trx.getID(), trx);
		}

		LOGGER.debug("init1: No. of entries in transaction map: " + trxMap.size());
	}

	private void init2(final KMYMONEYFILE pRootElement) {
		trxSpltMap = new HashMap<KMMQualifSpltID, KMyMoneyTransactionSplit>();

		for ( KMyMoneyTransaction trx : trxMap.values() ) {
			try {
				List<KMyMoneyTransactionSplit> spltList = null;
				if ( kmmFile instanceof KMyMoneyWritableFileImpl ) {
					// CAUTION: As opposed to the code in the sister project,
					// the second and third arg here has to be set to "true", else the
					// whole shebang will not work.
					// Cannot explain this...
					spltList = ((KMyMoneyTransactionImpl) trx).getSplits(true, true, true);
				} else {
					spltList = ((KMyMoneyTransactionImpl) trx).getSplits(true, true, true);
				}
				for ( KMyMoneyTransactionSplit splt : spltList ) {
					trxSpltMap.put(splt.getQualifID(), splt);
				}
			} catch (RuntimeException e) {
				LOGGER.error("init2: [RuntimeException] Problem in " + getClass().getName() + ".init2: "
						+ "ignoring illegal Transaction entry with id=" + trx.getID(), e);
//		System.err.println("init2: ignoring illegal Transaction entry with id: " + trx.getID());
//		System.err.println("  " + e.getMessage());
			}
		} // for trx

		LOGGER.debug("init2: No. of entries in transaction split map: " + trxSpltMap.size());
	}

	// ----------------------------

	protected KMyMoneyTransactionImpl createTransaction(final TRANSACTION jwsdpTrx) {
		KMyMoneyTransactionImpl trx = new KMyMoneyTransactionImpl(jwsdpTrx, kmmFile);
		LOGGER.debug("createTransaction: Generated new transaction: " + trx.getID());
		return trx;
	}

	protected KMyMoneyTransactionSplitImpl createTransactionSplit(
			final SPLIT jwsdpTrxSplt,
			final KMyMoneyTransaction trx, 
			final boolean addSpltToAcct,
			final boolean addSpltToPye,
			final boolean addSpltToTags) {
		KMyMoneyTransactionSplitImpl splt = new KMyMoneyTransactionSplitImpl(jwsdpTrxSplt, trx, 
																			 addSpltToAcct, addSpltToPye, addSpltToTags);
		LOGGER.debug("createTransactionSplit: Generated new transaction split: " + splt.getQualifID());
		return splt;
	}

	// ---------------------------------------------------------------

	public KMyMoneyTransaction getTransactionByID(final KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("null transaction ID given");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("unset transaction ID given");
		}

		if ( trxMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyTransaction retval = trxMap.get(trxID);
		if ( retval == null ) {
			LOGGER.warn("getTransactionByID: No Transaction with ID '" + trxID + "'. We know " + trxMap.size() + " transactions.");
		}

		return retval;
	}

	public List<? extends KMyMoneyTransaction> getTransactions() {
		if ( trxMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}
		
		ArrayList<KMyMoneyTransaction> temp = new ArrayList<KMyMoneyTransaction>(trxMap.values());
		Collections.sort(temp);
		
		return Collections.unmodifiableList(temp);
	}

	// ----------------------------

	public List<KMyMoneyTransactionSplit> getTransactionSplitsBySecID(final KMMSecID secID) {
		if ( secID == null ) {
			throw new IllegalArgumentException("null security ID given");
		}
		
		if ( ! secID.isSet() ) {
			throw new IllegalArgumentException("unset security ID given");
		}
		
		KMMQualifSecID qualifID = new KMMQualifSecID(secID);
		return getTransactionSplitsByQualifSecID(qualifID);
	}

	public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecID(final KMMQualifSecID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null quailf. security ID given");
		}
		
		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset quailf. security ID given");
		}
		
		return getTransactionSplitsByQualifSecCurrID(qualifID);
	}

	public List<KMyMoneyTransactionSplit> getTransactionSplitsByCurr(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null currency given");
		}
		
		KMMQualifCurrID qualifID = new KMMQualifCurrID(curr);
		return getTransactionSplitsByQualifCurrID(qualifID);
	}

	public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifCurrID(final KMMQualifCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null qualif. currency ID given");
		}
		
		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset qualif. currency ID given");
		}
		
		return getTransactionSplitsByQualifSecCurrID(qualifID);
	}

	public List<KMyMoneyTransactionSplit> getTransactionSplitsByQualifSecCurrID(final KMMQualifSecCurrID qualifID) {
		if ( qualifID == null ) {
			throw new IllegalArgumentException("null qualif. security/currendcy ID given");
		}
		
		if ( ! qualifID.isSet() ) {
			throw new IllegalArgumentException("unset qualif. security/currendcy ID given");
		}
		
		List<KMyMoneyTransactionSplit> result = new ArrayList<KMyMoneyTransactionSplit>();

		for ( KMyMoneyTransactionSplit splt : trxSpltMap.values() ) {
			if ( splt.getAccount().getQualifSecCurrID().toString().equals(qualifID.toString()) ) {
				KMyMoneyTransactionSplit newSplt = kmmFile.getTransactionSplitByID(splt.getQualifID());
				result.add(newSplt);
			}
		}

		return result;
	}

	// ---------------------------------------------------------------

	public KMyMoneyTransactionSplit getTransactionSplitByID(final KMMQualifSpltID spltID) {
		if ( spltID == null ) {
			throw new IllegalArgumentException("null split ID given");
		}

		if ( ! spltID.isSet() ) {
			throw new IllegalArgumentException("unset split ID given");
		}

		if ( trxSpltMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneyTransactionSplit retval = trxSpltMap.get(spltID);
		if ( retval == null ) {
			LOGGER.warn("getTransactionSplitByID: No Transaction-Split with ID '" + spltID + "'. We know " + trxSpltMap.size() + " transaction splits.");
		}

		return retval;
	}

	public List<KMyMoneyTransactionImpl> getTransactions_readAfresh() {
		List<KMyMoneyTransactionImpl> result = new ArrayList<KMyMoneyTransactionImpl>();

		for ( TRANSACTION jwsdpTrx : getTransactions_raw() ) {
			try {
				KMyMoneyTransactionImpl trx = createTransaction(jwsdpTrx);
				result.add(trx);
			} catch (RuntimeException e) {
				LOGGER.error("getTransactions_readAfresh: [RuntimeException] Problem in " + getClass().getName()
						+ ".getTransactions_readAfresh: " + "ignoring illegal Transaction entry with id="
						+ jwsdpTrx.getId(), e);
//		System.err.println("getTransactions_readAfresh: ignoring illegal Transaction entry with id: " + jwsdpTrx.getTrnID().getValue());
//		System.err.println("  " + e.getMessage());
			}
		}

		return result;
	}

	private List<TRANSACTION> getTransactions_raw() {
		List<TRANSACTION> result = new ArrayList<TRANSACTION>();

		for ( TRANSACTION jwsdpTrx : kmmFile.getRootElement().getTRANSACTIONS().getTRANSACTION() ) {
			result.add(jwsdpTrx);
		}

		return result;
	}

	// ----------------------------

	public List<KMyMoneyTransactionSplit> getTransactionSplits() {
		if ( trxSpltMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		ArrayList<KMyMoneyTransactionSplit> temp = new ArrayList<KMyMoneyTransactionSplit>(trxSpltMap.values());
		Collections.sort(temp);
		
		return Collections.unmodifiableList(temp);
	}

	public List<KMyMoneyTransactionSplitImpl> getTransactionSplits_readAfresh() {
		List<KMyMoneyTransactionSplitImpl> result = new ArrayList<KMyMoneyTransactionSplitImpl>();

		for ( KMyMoneyTransaction trx : getTransactions_readAfresh() ) {
			for ( SPLIT jwsdpTrxSplt : getTransactionSplits_raw(trx.getID()) ) {
				try {
					KMyMoneyTransactionSplitImpl splt = createTransactionSplit(jwsdpTrxSplt, trx,
																			   false, false, false);
					result.add(splt);
				} catch (RuntimeException e) {
					LOGGER.error("getTransactionSplits_readAfresh(1): [RuntimeException] Problem in "
							+ "ignoring illegal Transaction Split entry with id="
							+ trx.getID() + ":" + jwsdpTrxSplt.getId(), e);
//			System.err.println("getTransactionSplits_readAfresh(1): ignoring illegal Transaction Split entry with id: " + jwsdpTrxSplt.getSplitID().getValue());
//			System.err.println("  " + e.getMessage());
				}
			} // for jwsdpTrxSplt
		} // for trx

		return result;
	}

	public List<KMyMoneyTransactionSplitImpl> getTransactionSplits_readAfresh(final KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("null transaction ID given");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("unset transaction ID given");
		}

		List<KMyMoneyTransactionSplitImpl> result = new ArrayList<KMyMoneyTransactionSplitImpl>();

		for ( KMyMoneyTransaction trx : getTransactions_readAfresh() ) {
			if ( trx.getID().equals(trxID) ) {
				for ( SPLIT jwsdpTrxSplt : getTransactionSplits_raw(trx.getID()) ) {
					try {
						KMyMoneyTransactionSplitImpl splt = createTransactionSplit(jwsdpTrxSplt, trx, 
																				   true, true, true);
						result.add(splt);
					} catch (RuntimeException e) {
						LOGGER.error("getTransactionSplits_readAfresh(2): [RuntimeException] Problem in "
								+ "ignoring illegal Transaction Split entry with id="
								+ trx.getID() + ":" + jwsdpTrxSplt.getId(), e);
//			System.err.println("getTransactionSplits_readAfresh(2): ignoring illegal Transaction Split entry with id: " + jwsdpTrxSplt.getSplitID().getValue());
//			System.err.println("  " + e.getMessage());
					}
				} // for jwsdpTrxSplt
			} // if
		} // for trx

		return result;
	}

	@SuppressWarnings("unused")
	private List<SPLIT> getTransactionSplits_raw(final TRANSACTION jwsdpTrx) {
		List<SPLIT> result = new ArrayList<SPLIT>();

		for ( SPLIT jwsdpTrxSplt : jwsdpTrx.getSPLITS().getSPLIT() ) {
			result.add(jwsdpTrxSplt);
		}

		return result;
	}

	private List<SPLIT> getTransactionSplits_raw(final KMMTrxID trxID) {
		if ( trxID == null ) {
			throw new IllegalArgumentException("null transaction ID given");
		}

		if ( ! trxID.isSet() ) {
			throw new IllegalArgumentException("unset transaction ID given");
		}

		List<SPLIT> result = new ArrayList<SPLIT>();

		for ( TRANSACTION jwsdpTrx : getTransactions_raw() ) {
			if ( jwsdpTrx.getId().equals(trxID.toString()) ) {
				for ( SPLIT jwsdpTrxSplt : jwsdpTrx.getSPLITS().getSPLIT() ) {
					result.add(jwsdpTrxSplt);
				}
			}
		}

		return result;
	}

	// ---------------------------------------------------------------
	
	protected TRANSACTION getTransaction_raw(final KMMTrxID trxID) {
		for ( TRANSACTION jwsdpTrx : getTransactions_raw() ) {
			if ( jwsdpTrx.getId().equals(trxID.toString())) {
				return jwsdpTrx;
			}
		}
		
		return null;
	}

	// ---------------------------------------------------------------

	public int getNofEntriesTransactionMap() {
		return trxMap.size();
	}

	public int getNofEntriesTransactionSplitMap() {
		return trxSpltMap.size();
	}

}
