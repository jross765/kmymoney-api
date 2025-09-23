package org.kmymoney.api.write.impl.hlp;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.generated.TRANSACTION;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionImpl;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableTransactionImpl;
import org.kmymoney.api.write.impl.KMyMoneyWritableTransactionSplitImpl;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTransactionManager extends org.kmymoney.api.read.impl.hlp.FileTransactionManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileTransactionManager.class);

	// ---------------------------------------------------------------

	public FileTransactionManager(KMyMoneyWritableFileImpl kmmFile) {
		super(kmmFile);
	}

	// ---------------------------------------------------------------

	/*
	 * Creates the writable version of the returned object.
	 */
	@Override
	protected KMyMoneyTransactionImpl createTransaction(final TRANSACTION jwsdpTrx) {
		KMyMoneyWritableTransactionImpl trx = new KMyMoneyWritableTransactionImpl(jwsdpTrx, kmmFile);
		LOGGER.debug("createTransaction: Generated new writable transaction: " + trx.getID());
		return trx;
	}

    @Override
	protected KMyMoneyTransactionSplitImpl createTransactionSplit(
			final SPLIT jwsdpTrxSplt,
			final KMyMoneyTransaction trx, // actually, should be KMyMoney*Writable*Transaction, 
			                               // but then the compiler is not happy...
			final boolean addSpltToAcct,
			final boolean addSpltToPye,
			final boolean addSpltToTags) {
    	if ( ! ( trx instanceof KMyMoneyWritableTransaction ) ) {
    		throw new IllegalArgumentException("transaction must be a writable one");
    	}
    	
    	KMyMoneyWritableTransactionSplitImpl splt = new KMyMoneyWritableTransactionSplitImpl(jwsdpTrxSplt, 
    																						 (KMyMoneyWritableTransaction) trx, 
                								           									 addSpltToAcct, addSpltToPye, addSpltToTags);
    	LOGGER.debug("createTransactionSplit: Generated new writable transaction split: " + splt.getID());
    	return splt;
    }

	// ---------------------------------------------------------------

	public void addTransaction(KMyMoneyTransaction trx) {
		addTransaction(trx, true);
	}

	public void addTransaction(KMyMoneyTransaction trx, boolean withSplt) {
		if ( trx == null ) {
			throw new IllegalArgumentException("null transaction given");
		}

		trxMap.put(trx.getID(), trx);

		if ( withSplt ) {
			for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
				addTransactionSplit(splt, false);
			}
		}

		LOGGER.debug("addTransaction: Added transaction to cache: " + trx.getID());
	}

	public void removeTransaction(KMyMoneyTransaction trx) {
		removeTransaction(trx, true);
	}

	public void removeTransaction(KMyMoneyTransaction trx, boolean withSplt) {
		if ( trx == null ) {
			throw new IllegalArgumentException("null transaction given");
		}

		if ( withSplt ) {
			for ( KMyMoneyTransactionSplit splt : trx.getSplits() ) {
				removeTransactionSplit(splt, false);
			}
		}

		trxMap.remove(trx.getID());

		LOGGER.debug("removeTransaction: Removed transaction from cache: " + trx.getID());
	}

	// ----------------------------
	
	public void removeTransaction_raw(final KMMTrxID trxID) {
		KMYMONEYFILE pRootElement = kmmFile.getRootElement();

		for ( int i = 0; i < pRootElement.getTRANSACTIONS().getTRANSACTION().size(); i++ ) {
			TRANSACTION jwsdpTrx = pRootElement.getTRANSACTIONS().getTRANSACTION().get(i);
			if ( jwsdpTrx.getId().equals(trxID.toString())) {
				pRootElement.getTRANSACTIONS().getTRANSACTION().remove(i);
				i--;
			}
		}
	}

	// ---------------------------------------------------------------

	public void addTransactionSplit(KMyMoneyTransactionSplit splt) {
		addTransactionSplit(splt, true);
	}

	public void addTransactionSplit(KMyMoneyTransactionSplit splt, boolean withTrx) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}

		trxSpltMap.put(splt.getQualifID(), splt);

		if ( withTrx ) {
			addTransaction(splt.getTransaction(), false);
		}
	}

	public void removeTransactionSplit(KMyMoneyTransactionSplit splt) {
		removeTransactionSplit(splt, false);
	}

	public void removeTransactionSplit(KMyMoneyTransactionSplit splt, boolean withTrx) {
		if ( splt == null ) {
			throw new IllegalArgumentException("null split given");
		}

		if ( withTrx ) {
			removeTransaction(splt.getTransaction(), false);
		}

		trxSpltMap.remove(splt.getQualifID());
	}

	// ----------------------------
	
	public void removeTransactionSplit_raw(final KMMTrxID trxID, final KMMSpltID spltID) {
		TRANSACTION trxRaw = getTransaction_raw(trxID);
		
		for ( int i = 0; i < trxRaw.getSPLITS().getSPLIT().size(); i++ ) {
			SPLIT jwsdpTrxSplt = trxRaw.getSPLITS().getSPLIT().get(i);
			if ( jwsdpTrxSplt.getId().equals(spltID.toString())) {
				trxRaw.getSPLITS().getSPLIT().remove(i);
				i--;
			}
		}
	}

}
