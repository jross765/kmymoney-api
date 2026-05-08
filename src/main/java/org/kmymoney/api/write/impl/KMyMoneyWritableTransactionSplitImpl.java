package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import java.util.Collection;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyTransactionSplitImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.IllegalTransactionSplitActionException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Transaction-Split that can be newly created or removed from it's transaction.
 */
public class KMyMoneyWritableTransactionSplitImpl extends KMyMoneyTransactionSplitImpl 
                                                  implements KMyMoneyWritableTransactionSplit 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableTransactionSplitImpl.class);

    // ---------------------------------------------------------------

	// Our helper to implement the KMyMoneyWritableObject-interface.
	private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

	// ---------------------------------------------------------------
	
	/**
	 * @param jwsdpPeer   the JWSDP-object we are facading.
	 * @param trx the transaction we belong to
     * @param addSpltToAcct 
	 * @param addSpltToPye 
	 * @param addSpltToTags 
	 */
	@SuppressWarnings("exports")
	public KMyMoneyWritableTransactionSplitImpl(
			final SPLIT jwsdpPeer,
			final KMyMoneyWritableTransaction trx, 
    		final boolean addSpltToAcct,
    		final boolean addSpltToPye,
    		final boolean addSpltToTags) {
		super(jwsdpPeer, trx, 
			  addSpltToAcct, addSpltToPye, addSpltToTags);
	}

    public KMyMoneyWritableTransactionSplitImpl(final KMyMoneyTransactionSplit splt) {
    	super(splt.getJwsdpPeer(), splt.getTransaction(), 
    		  true, true, true);
    }

    /**
	 * create a new split and and add it to the given transaction.
	 * @param trx transaction the transaction we will belong to
	 * @param acct     the account we take money (or other things) from or give
	 *                    it to
	 */
	public KMyMoneyWritableTransactionSplitImpl(
			final KMyMoneyWritableTransactionImpl trx,
			final KMyMoneyAccount acct) {
		super(createTransactionSplit_int(trx.getWritableFile(), 
				                         trx, acct, null, null,
				                         trx.getNewSplitID()),
		      trx,
		      true, false, false);

		// ::TODO ::CHECK
		// This is a workaround:
		// When super executes acct.addSplit(this), it adds an instance on
		// KMyMoneyTransactionSplitImpl that is != (KMyMoneyWritableTransactionSplitImpl) this;
		// thus we would get warnings about duplicate split-ids and could no longer
		// compare splits by instance.
//		 if( acct != null ) {
//			 ((KMyMoneyAccountImpl) acct)
//			 	.replaceTransactionSplit(acct.getTransactionSplitByID(getQualifID()),
//			 							 KMyMoneyWritableTransactionSplitImpl.this);
//		 }

		 trx.addSplit(this);
	}

    /**
	 * create a new split and and add it to the given transaction.
	 * @param trx transaction the transaction we will belong to
	 * @param acct     the account we take money (or other things) from or give
	 *                    it to
     * @param pye 
	 */
	public KMyMoneyWritableTransactionSplitImpl(
			final KMyMoneyWritableTransactionImpl trx,
			final KMyMoneyAccount acct,
			final KMyMoneyPayee pye,
			final Collection<KMyMoneyTag> tagList) {
		super(createTransactionSplit_int(trx.getWritableFile(), 
				                         trx, acct, pye, tagList,
				                         trx.getNewSplitID()),
		      trx,
		      true, 
		      ( pye     == null ? false : true ),
		      ( tagList == null ? false : true ) );

		// ::TODO ::CHECK
		// This is a workaround:
		// When super executes acct.addSplit(this), it adds an instance on
		// KMyMoneyTransactionSplitImpl that is != (KMyMoneyWritableTransactionSplitImpl) this;
		// thus we would get warnings about duplicate split-ids and could no longer
		// compare splits by instance.
//		 if( acct != null ) {
//			 ((KMyMoneyAccountImpl) acct)
//			 	.replaceTransactionSplit(acct.getTransactionSplitByID(getQualifID()),
//			 							 KMyMoneyWritableTransactionSplitImpl.this);
//		 }
//
//		 if( pye != null ) {
//			 ((KMyMoneyPayeeImpl) pye)
//			 	.replaceTransactionSplit(pye.getTransactionSplitByID(getQualifID()),
//			 							 KMyMoneyWritableTransactionSplitImpl.this);
//		 }

		 trx.addSplit(this);
	}

    public KMyMoneyWritableTransactionSplitImpl(final KMyMoneyTransactionSplitImpl splt) {
    	super(splt.getJwsdpPeer(), splt.getTransaction(), 
    		  true, true, true);
    }

    public KMyMoneyWritableTransactionSplitImpl(
    		final KMyMoneyTransactionSplitImpl split,
    		final boolean addSpltToAcct,
    		final boolean addSpltToPye,
    		final boolean addSpltToTags) {
    	super(split.getJwsdpPeer(), split.getTransaction(), 
    		  addSpltToAcct, addSpltToPye, addSpltToTags);
    }

	// ---------------------------------------------------------------
	
	/**
	 * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
	 * the ID of the new transaction!
	 */
	protected static SPLIT createTransactionSplit_int(
			final KMyMoneyWritableFileImpl file, 
			final KMyMoneyWritableTransactionImpl trx, 
			final KMyMoneyAccount acct, 
			final KMyMoneyPayee pye, 
			final Collection<KMyMoneyTag> tagList, 
			final KMMSpltID newID) {
		if ( trx == null ) {
			throw new IllegalArgumentException("argument <trx> is null");
		}

		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}

		// Sic: null is allowed!
//		if ( pye == null ) {
//			throw new IllegalArgumentException("argument <pye> is null");
//		}

		// Sic: null is allowed!
//		if ( tagList == null ) {
//			throw new IllegalArgumentException("argument <tagList> is null");
//		}

		if ( newID == null ) {
			throw new IllegalArgumentException("argument <newID> is null");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("argument <newID> is not set");
		}

		// This is needed because transaction.addSplit() later
		// must have an already built list of splits.
		// Otherwise, it will create the list from the JAXB-Data
		// thus 2 instances of this KMyMoneyWritableTransactionSplitImpl
		// will exist. One created in getSplits() from this JAXB-Data
		// the other is this object.
		trx.getSplits();

		// ObjectFactory fact = file.getObjectFactory();

		SPLIT jwsdpSplt = file.createSplitType();
		
		jwsdpSplt.setId(newID.toString());
		
		jwsdpSplt.setAccount(acct.getID().toString());
		
		if ( pye != null ) {
			jwsdpSplt.setPayee(pye.getID().toString());
		}
		
		if ( tagList != null ) {
			for ( KMyMoneyTag tag : tagList ) {
				// ::TODO
				System.err.println("NOT IMPLEMENTED YET");
			}
		}
		
		jwsdpSplt.setShares(new FixedPointNumber().toKMyMoneyString());
		jwsdpSplt.setValue(new FixedPointNumber().toKMyMoneyString());
		
		// NO, not here but in the calling method:
		// trx.addSplit(new KMyMoneyWritableTransactionSplitImpl(jwsdpSplt, trx.getKMyMoneyFile(), trx));
		// No:
		// trx.getJwsdpPeer().getSPLITS().getSPLIT().add(jwsdpSplt);
		file.setModified(true);
    
        	LOGGER.debug("createTransactionSplit_int: Created new transaction split (core): " + jwsdpSplt.getId());
		
        	return jwsdpSplt;
	}

	// ---------------------------------------------------------------
	
	/**
	 * @see KMyMoneyTransactionSplitImpl#getTransaction()
	 */
	@Override
	public KMyMoneyWritableTransaction getTransaction() {
		return (KMyMoneyWritableTransaction) super.getTransaction();
	}

	/**
	 * remove this split from it's transaction.
	 */
	@Override
	public void remove() {
		getTransaction().remove(this);
	}

	/**
	 * @see KMyMoneyWritableTransactionSplit#setAccount(KMyMoneyAccount)
	 */
	@Override
	public void setAccountID(final KMMComplAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}
		
		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		if ( acctID.getType() == KMMComplAcctID.Type.SPECIAL ) {
			LOGGER.error("setAccountID: A transaction split's account may not be set to a special (top-level) account");
			throw new IllegalArgumentException("argument <acctID> is of type " + KMMComplAcctID.Type.SPECIAL);
		}
		
		setAccountID(acctID.getStdID());
	}

	@Override
	public void setAccountID(final KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}
		
		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}
		
		String oldAcctID = (getJwsdpPeer().getAccount() == null ? null : getJwsdpPeer().getAccount());
		jwsdpPeer.setAccount(acctID.toString());

		// No, the following is nonsense:
//		if ( ! isCurrencyMatching() ) {
//			LOGGER.error("setAccountID: Transaction Split: " + getQualifID() + ": New account's security/currency is not the same as transaction's security/currency");
//			LOGGER.error("Reverting change");
//			jwsdpPeer.setAccount(oldAcctID);
//			throw new IllegalStateException("Transaction Split: " + getQualifID() + ": New account's security/currency is not the same as transaction's security/currency");
//		}
		
		getWritableKMyMoneyFile().setModified(true);

		if ( oldAcctID == null || 
			 ! oldAcctID.equals(acctID.toString()) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("accountID", oldAcctID, acctID.toString());
			}
		}
	}

	/**
	 * @see KMyMoneyWritableTransactionSplit#setAccount(KMyMoneyAccount)
	 */
	@Override
	public void setAccount(final KMyMoneyAccount acct) {
		if ( acct == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		setAccountID(acct.getID());
	}

	/**
	 * @return true if the currency of transaction and account match
     * 
     * ::CHECK: What could we possibly need that for?
     * 
     * It is definitely *not* the case that a transaction's split's
     * account's security/currency has to be the same as its resp.
     * transaction's security/currency -- there are many real-world 
     * instances where that is not / must not be the case (a dividend 
     * transaction, for example).
     * 
     * This (or a similar) wrong assumption obviously was in one of
     * the previous maintainers' mind, because such a check was
     * coded into this lib before the current maintainer took over
     * (and it took him a while to see the error).
     * 
     * It is, however, true that this condition must hold for at
     * least *one* of a transaction's splits. But this is, by definition,
     * a check to be performed on a transaction object, not a 
     * single split object. For such a check, this could be a helper
     * function.
	 */
	private boolean isSecCurrMatching() {
		KMyMoneyAccount acct = getAccount();
		if ( acct == null ) {
			throw new IllegalStateException("account is null");
		}
		
		KMyMoneyWritableTransaction trx = getTransaction();
		if ( trx == null ) {
			throw new IllegalStateException("transaction is null");
		}
		
		KMMQualifSecCurrID acctSecCurrID = acct.getQualifSecCurrID();
		if ( acctSecCurrID == null ) {
			throw new IllegalStateException("account's security/currency is null");
		}		
		if ( ! acctSecCurrID.isSet() ) {
			throw new IllegalStateException("account's security/currency is not set");
		}
		
		KMMQualifSecCurrID trxSecCurrID = trx.getQualifSecCurrID();
		if ( trxSecCurrID == null ) {
			throw new IllegalStateException("transaction's security/currency is null");
		}
		if ( ! trxSecCurrID.isSet() ) {
			throw new IllegalStateException("transaction's security/currency is not set");
		}
		
		return acctSecCurrID.toString().equals( trxSecCurrID.toString() );
	}

	/**
	 * @see KMyMoneyWritableTransactionSplit#setShares(FixedPointNumber)
	 */
	@Override
	@Deprecated
	public void setShares(final FixedPointNumber shr) {
		if ( shr == null ) {
			throw new IllegalArgumentException("argument <shr> is null");
		}

		FixedPointNumber oldShr = getShares();
		jwsdpPeer.setShares(shr.toKMyMoneyString());
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldShr == null || ! oldShr.equals(shr) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("shares", oldShr, shr);
			}
		}
	}

	@Override
	public void setShares(final BigFraction shr) {
		if ( shr == null ) {
			throw new IllegalArgumentException("argument <shr> is null");
		}

		BigFraction oldShr = getSharesRat();
		jwsdpPeer.setShares(shr.toString().replaceAll("\\s", ""));
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldShr == null || ! oldShr.equals(shr) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("shares", oldShr, shr);
			}
		}
	}

	/**
	 * @see KMyMoneyWritableTransactionSplit#setValue(FixedPointNumber)
	 */
	@Override
	@Deprecated
	public void setValue(final FixedPointNumber val) {
		if ( val == null ) {
			throw new IllegalArgumentException("argument <val> is null");
		}
		
		FixedPointNumber oldVal = getValue();
		jwsdpPeer.setValue(val.toKMyMoneyString());
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldVal == null || ! oldVal.equals(val) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("value", oldVal, val);
			}
		}
	}

	@Override
	public void setValue(final BigFraction val) {
		if ( val == null ) {
			throw new IllegalArgumentException("argument <val> is null");
		}

		BigFraction oldVal = getValueRat();
		jwsdpPeer.setValue(val.toString().replaceAll("\\s", ""));
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldVal == null || ! oldVal.equals(val) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("value", oldVal, val);
			}
		}
	}

	@Override
	@Deprecated
	public void setPrice(final FixedPointNumber prc) {
		if ( prc == null ) {
			throw new IllegalArgumentException("argument <prc> is null");
		}
		
		if ( prc.compareTo(FixedPointNumber.ZERO) <= 0 ) {
			throw new IllegalArgumentException("argument <prc> is <= 0");
		}
		
		FixedPointNumber oldPrc = getPrice();
		jwsdpPeer.setPrice(prc.toKMyMoneyString());
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldPrc == null || ! oldPrc.equals(prc) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("price", oldPrc, prc);
			}
		}
	}

	@Override
	public void setPrice(final BigFraction prc) {
		if ( prc == null ) {
			throw new IllegalArgumentException("argument <prc> is null");
		}
		
		if ( prc.compareTo(BigFraction.ZERO) <= 0 ) {
			throw new IllegalArgumentException("argument <prc> is <= 0");
		}
		
		BigFraction oldPrc = getPriceRat();
		jwsdpPeer.setPrice(prc.toString().replaceAll("\\s", ""));
		getWritableKMyMoneyFile().setModified(true);
		
		if ( oldPrc == null || ! oldPrc.equals(prc) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("price", oldPrc, prc);
			}
		}
	}

	@Override
	public void setNumber(final String num) {
		if ( num == null ) {
			throw new IllegalArgumentException("argument <num> is null");
		}
		
		if ( num.isBlank() ) {
			throw new IllegalArgumentException("argument <num> is blank");
		}
	
    	String oldNumber = getNumber();
    	jwsdpPeer.setNumber(num);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("number", oldNumber, num);
    	}
	}
	
	// ---------------------------------------------------------------

	@Override
	public void setPayeeID(KMMPyeID pyeID) {
		if ( pyeID == null ) {
			throw new IllegalArgumentException("argument <pyeID> is null");
		}
		
		String old = (getJwsdpPeer().getPayee() == null ? null : getJwsdpPeer().getPayee());
		jwsdpPeer.setPayee(pyeID.toString());
		getWritableKMyMoneyFile().setModified(true);

		if ( old == null || 
			 ! old.equals(pyeID.toString()) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("payeeID", old, pyeID.toString());
			}
		}
	}

	@Override
	public void setPayee(KMyMoneyPayee pye) {
		if ( pye == null ) {
			throw new IllegalArgumentException("argument <pye> is null");
		}
		
		setPayeeID(pye.getID());
	}

	// ---------------------------------------------------------------

	@Override
	public void clearTags() {
		if ( getTagIDs() == null )
			return;
		
		if ( getTagIDs().size() == 0 )
			return;
		
		for ( int i = jwsdpPeer.getTAG().size(); i >= 0; i-- ) {
			jwsdpPeer.getTAG().remove(i);
		}
	}

	@Override
	public void addTagID(KMMTagID tagID) {
		if ( tagID == null ) {
			throw new IllegalArgumentException("argument <tagID> is null");
		}
		
		if ( ! tagID.isSet() ) {
			throw new IllegalArgumentException("argument <tagID> is not set");
		}
		
		ObjectFactory fact = getKMyMoneyFile().getObjectFactory();
		SPLIT.TAG jwsdpSpltTag = fact.createSPLITTAG();
		jwsdpSpltTag.setId(tagID.toString());
		
		jwsdpPeer.getTAG().add(jwsdpSpltTag);
	}

	@Override
	public void addTag(KMyMoneyTag tag) {
		if ( tag == null ) {
			throw new IllegalArgumentException("argument <tag> is null");
		}
		
		addTagID(tag.getID());
	}

	// ---------------------------------------------------------------

	/**
	 * Set the description-text.
	 *
	 * @param desc the new description
	 */
	@Override
	public void setMemo(final String desc) {
		if ( desc == null ) {
			throw new IllegalArgumentException("argument <desc> is null. Please use the empty string instead of null for an empty description");
		}

		// Yes, empty desc is valid
//		if ( desc.isBlank() ) {
//			throw new IllegalArgumentException("argument <desc> is blank");
//		}

		String old = getJwsdpPeer().getMemo();
		jwsdpPeer.setMemo(desc);
		getWritableKMyMoneyFile().setModified(true);

		if ( old == null || ! old.equals(desc) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("description", old, desc);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAction(final Action act) throws IllegalTransactionSplitActionException {
		setActionStr(act.getCode());
	}
	
	/**
     * <b>Using this method is discouraged.</b>
     * Use {@link #setAction(org.kmymoney.api.read.KMyMoneyTransactionSplit.Action)} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #setAction(org.kmymoney.api.read.KMyMoneyTransactionSplit.Action)
	 */
	public void setActionStr(final String actStr) throws IllegalTransactionSplitActionException {
		if ( actStr == null ) {
			throw new IllegalArgumentException("argument <actStr> is null");
		}

		if ( actStr.isBlank() ) {
			throw new IllegalArgumentException("argument <actStr> is null");
		}

		String old = getJwsdpPeer().getAction();
		jwsdpPeer.setAction(actStr);
		getWritableKMyMoneyFile().setModified(true);

		if ( old == null || 
			 ! old.equals(actStr) ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				helper.getPropertyChangeSupport().firePropertyChange("splitAction", old, actStr);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReconState(final ReconState stat) {
		setReconStateInt(stat.getIndex());
	}
	
	@Override
	@Deprecated
	public void setState(ReconState stat) {
		setReconState(stat);
	}

	/**
     * <b>Using this method is discouraged.</b>
     * Use {@link #setReconState(org.kmymoney.api.read.KMyMoneyTransactionSplit.ReconState)} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #setReconState(org.kmymoney.api.read.KMyMoneyTransactionSplit.ReconState)
	 */
	public void setReconStateInt(final int statInt) {
		if ( statInt < 0 ) { // sic, 0 is allowed
			throw new IllegalArgumentException("argument <statInt> is blank");
		}

		BigInteger old = getJwsdpPeer().getReconcileflag();
		jwsdpPeer.setReconcileflag(BigInteger.valueOf(statInt));
		getWritableKMyMoneyFile().setModified(true);

		if ( old == null || 
			 old.intValue() != statInt ) {
			if ( helper.getPropertyChangeSupport() != null ) {
				 helper.getPropertyChangeSupport().firePropertyChange("splitReconState", old, statInt);
			}
		}
	}

	// ---------------------------------------------------------------

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public KMyMoneyWritableFileImpl getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

	/**
	 * The KMyMoney file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public KMyMoneyWritableFileImpl getKMyMoneyFile() {
		return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
	}

    // ---------------------------------------------------------------

    @Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyWritableTransactionSplitImpl [");

		buffer.append("qualif-id=");
		buffer.append(getQualifID());

		buffer.append(", action=");
		try {
			buffer.append(getAction());
		} catch (Exception e) {
			buffer.append("ERROR");
		}

		buffer.append(", recon-state=");
		try {
			buffer.append(getReconState());
		} catch (Exception e) {
			buffer.append("ERROR");
		}

		// Redundant, because the qualif-ID above already contains it:
//		buffer.append(", transaction-id=");
//		buffer.append(getTransaction().getID());

		buffer.append(", account-id=");
		buffer.append(getAccountID());

		buffer.append(", number='");
		buffer.append(getNumber() + "'");

		buffer.append(", payee-id=");
		buffer.append(getPayeeID());

		buffer.append(", memo='");
		buffer.append(getMemo() + "'");

		buffer.append(", value=");
		buffer.append(getValue());

		buffer.append(", shares=");
		buffer.append(getShares());

		buffer.append(", price=");
		buffer.append(getPrice());

		buffer.append("]");
		return buffer.toString();
    }

}
