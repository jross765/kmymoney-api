package org.kmymoney.api.read.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.MappingException;
import xyz.schnorxoborx.base.numbers.FixedPointNumber;

/**
 * Implementation of KMyMoneyTransactionSplit that uses JWSDSP.
 */
public class KMyMoneyTransactionSplitImpl extends KMyMoneyObjectImpl
                                          implements KMyMoneyTransactionSplit 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyTransactionSplitImpl.class);

    // ---------------------------------------------------------------

    // the JWSDP-object we are facading.
    protected final SPLIT jwsdpPeer;
    
    // the transaction this split belongs to.
    protected final KMyMoneyTransaction myTrx;

    // ---------------------------------------------------------------

    /**
     * @param peer the JWSDP-object we are facading.
     * @param trx  the transaction this split belongs to
     * @param addSpltToAcct 
     * @param addSpltToPye 
     * @param addSpltToTags
     */
    @SuppressWarnings("exports")
    public KMyMoneyTransactionSplitImpl(
	    final SPLIT peer,
	    final KMyMoneyTransaction trx,
	    final boolean addSpltToAcct,
	    final boolean addSpltToPye,
	    final boolean addSpltToTags) {
    	super(trx.getKMyMoneyFile());
    	
    	this.jwsdpPeer = peer;
    	this.myTrx = trx;

    	if ( addSpltToAcct ) {
    		KMyMoneyAccount acct = getAccount();
    		if (acct == null) {
    			// Account is mandatory --> error
    			LOGGER.error("No such Account id='" + getAccountID() + "' for Transactions-Split with id '" + getQualifID()
			    	+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
			    	+ "' description '" + getTransaction().getMemo() + "'");
    		} else {
    			acct.addTransactionSplit(this);
    		}
    	}
    	
    	if ( addSpltToPye ) {
    		KMyMoneyPayee pye = getPayee();
    		if (pye == null) {
    			// Payee is optional --> just debug
    			LOGGER.debug("No such Payee id='" + getPayeeID() + "' for Transactions-Split with id '" + getQualifID() 
			    	+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
			    	+ "' description '" + getTransaction().getMemo() + "'");
    		} else {
    			pye.addTransactionSplit(this);
    		}
    	}

    	if ( addSpltToTags ) {
    		Collection<KMyMoneyTag> tagList = getTags();
    		if (tagList == null) {
    			// Tags are optional --> just debug
    			LOGGER.debug("No such Tag for Transactions-Split with id '" + getQualifID() 
			    	+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
			    	+ "' description '" + getTransaction().getMemo() + "'");
    		} else {
    			for ( KMyMoneyTag tag : tagList ) {
        			tag.addTransactionSplit(this);
    			}
    		}
    	}
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
    public SPLIT getJwsdpPeer() {
    	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyTransactionSplit#getID()
     */
    @Override
    public KMMSpltID getID() {
    	return new KMMSpltID(jwsdpPeer.getId());
    }

    @Override
    public KMMQualifSpltID getQualifID() {
    	return new KMMQualifSpltID(getTransactionID(), getID());
    }
    
    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyTransactionSplit#getAccountID()
     */
    @Override
    public KMMComplAcctID getAccountID() {
    	if ( jwsdpPeer.getAccount() == null )
    		return null;
    	
    	if ( jwsdpPeer.getAccount().trim().length() == 0 )
    		return null;
    	
    	return new KMMComplAcctID( jwsdpPeer.getAccount() );
    }

    /**
     * @see KMyMoneyTransactionSplit#getAccount()
     */
    @Override
    public KMyMoneyAccount getAccount() {
    	return myTrx.getKMyMoneyFile().getAccountByID(getAccountID());
    }

    // ----------------------------
    
    @Override
    public String getNumber() {
    	if ( jwsdpPeer.getNumber() == null )
    		return null;
    	
//    	if ( jwsdpPeer.getNumber().trim().length() == 0 )
//    		return null;
    	
    	return jwsdpPeer.getNumber();
    }
    
    // ----------------------------
    
    @Override
    public KMMPyeID getPayeeID() {
    	if ( jwsdpPeer.getPayee() == null )
    		return null;
    	
    	if ( jwsdpPeer.getPayee().trim().length() == 0 )
    		return null;
    	
    	return new KMMPyeID( jwsdpPeer.getPayee() );
    }
    
    @Override
    public KMyMoneyPayee getPayee() {
    	if ( getPayeeID() == null )
    		return null;
    	
    	return getKMyMoneyFile().getPayeeByID(getPayeeID()); 
    }

    // ----------------------------

    @Override
    public Collection<KMMTagID> getTagIDs()
    {
    	if ( jwsdpPeer.getTAG() == null )
    		return null;
		
    	ArrayList<KMMTagID> result = new ArrayList<KMMTagID>();
    	
    	for ( SPLIT.TAG elt : jwsdpPeer.getTAG() ) {
    		KMMTagID newID = new KMMTagID(elt.getId());
    		result.add(newID);
    	}
		
    	return result;
    }

    @Override
    public Collection<KMyMoneyTag> getTags() {
    	if ( getTagIDs() == null )
    		return null;
    	
    	Collection<KMyMoneyTag> result = new ArrayList<KMyMoneyTag>();
    	
    	for ( KMMTagID tagID : getTagIDs() ) {
        	KMyMoneyTag newTag = getKMyMoneyFile().getTagByID(tagID);
        	result.add(newTag);
    	}
    	
    	return result;
    }

    // ----------------------------

    @Override
    public KMMTrxID getTransactionID() {
    	return myTrx.getID();
    }

    /**
     * @see KMyMoneyTransactionSplit#getTransaction()
     */
    @Override
    public KMyMoneyTransaction getTransaction() {
    	return myTrx;
    }

    // ---------------------------------------------------------------

    @Override
    public Action getAction() {
    	try {
    		return Action.valueOff( getActionStr() );
    	} catch (Exception e) {
    		throw new MappingException("Could not map string '" + getActionStr() + "' to Action enum");
    	}
    }

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #getAction()} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #getAction()
     */
    public String getActionStr() {
    	if (getJwsdpPeer().getAction() == null) {
    		return "";
    	}

    	return getJwsdpPeer().getAction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReconState getReconState() {
    	try {
    		return ReconState.valueOff( getReconStateInt() );
    	} catch (Exception e) {
    		throw new MappingException("Could not map integer " + getReconStateInt() + " to State enum");
    	}
    }

    @Override
    @Deprecated
    public ReconState getState() {
    	return getReconState();
    }

    /**
     * <b>Using this method is discouraged.</b>
     * Use {@link #getReconState()} whenever possible/applicable instead.
     * 
     * @return
     * 
     * @see #getState()
     */
    public int getReconStateInt() {
    	if (jwsdpPeer.getReconcileflag() == null) {
    		return -1;
    	}

		return jwsdpPeer.getReconcileflag().intValue();
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyTransactionSplit#getValue()
     */
    @Override
    public FixedPointNumber getValue() {
    	return new FixedPointNumber(jwsdpPeer.getValue());
    }

    public BigFraction getValueRat() {
    	return BigFraction.parse(jwsdpPeer.getValue());
    }

    /**
     * @see KMyMoneyTransactionSplit#getValueFormatted()
     */
    @Override
    public String getValueFormatted() {
    	return getValueFormatted(Locale.getDefault());
    }

    /**
     * @see KMyMoneyTransactionSplit#getValueFormatted(java.util.Locale)
     */
    @Override
    public String getValueFormatted(final Locale lcl) {
		NumberFormat nf = getValueCurrencyFormat(lcl);
    	if ( getTransaction().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
    		nf.setCurrency(Currency.getInstance(getTransaction().getQualifSecCurrID().getCode()));
    		return nf.format(getValue().getBigDecimal());
    	} else {
    		return nf.format(getValue().getBigDecimal()) + " " + getTransaction().getQualifSecCurrID().toString();
    	}
    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyTransactionSplit#getShares()
     */
    @Override
    public FixedPointNumber getShares() {
    	return new FixedPointNumber(jwsdpPeer.getShares());
    }

    @Override
    public BigFraction getSharesRat() {
    	return BigFraction.parse(jwsdpPeer.getShares());
    }

    /**
     * The value is in the currency of the account!
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    @Override
    public String getSharesFormatted() {
    	return getSharesFormatted(Locale.getDefault());
    }

    /**
     * The value is in the currency of the account!
     *
     * @param lcl the locale to format to
     * @return the formatted number
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    @Override
    public String getSharesFormatted(final Locale lcl) {
		NumberFormat nf = getSharesCurrencyFormat(lcl);
		if ( getAccount().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			nf.setCurrency(new KMMQualifCurrID(getAccount().getQualifSecCurrID()).getCurrID().get());
			return nf.format(getShares().getBigDecimal());
		} else {
			return nf.format(getShares().getBigDecimal()) + " " + getAccount().getQualifSecCurrID().getCode().toString();
		}
    }

    // ---------------------------------------------------------------
    
    @Override
    public FixedPointNumber getPrice() {
    	if ( jwsdpPeer.getPrice() == null )
    		return null;
    	
    	if ( jwsdpPeer.getPrice().trim().length() == 0 )
    		return null;
    	
    	return new FixedPointNumber( jwsdpPeer.getPrice() );
    }
    
    @Override
    public BigFraction getPriceRat() {
    	if ( jwsdpPeer.getPrice() == null )
    		return null;
    	
    	if ( jwsdpPeer.getPrice().trim().length() == 0 )
    		return null;
    	
    	return BigFraction.parse(jwsdpPeer.getPrice());
    }

    @Override
    public String getPriceFormatted() {
    	return getPriceFormatted(Locale.getDefault());
    }

    @Override
    public String getPriceFormatted(Locale lcl) {
		NumberFormat nf = getPriceCurrencyFormat(lcl);
		if ( getTransaction().getQualifSecCurrID().getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			nf.setCurrency(new KMMQualifCurrID(getTransaction().getQualifSecCurrID()).getCurrID().get());
			return nf.format(getPrice().getBigDecimal());
		} else {
			return nf.format(getPrice().getBigDecimal()) + " " + getTransaction().getQualifSecCurrID().toString();
		}
    }

    // ---------------------------------------------------------------

    @Override
    public String getMemo() {
    	if (jwsdpPeer.getMemo() == null) {
    		return "";
    	}
    	return jwsdpPeer.getMemo();
    }
    
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
	
	/**
	 * @return the currency-format of the transaction
	 */
	public NumberFormat getValueCurrencyFormat() {
		return getValueCurrencyFormat(Locale.getDefault());
	}

    public NumberFormat getValueCurrencyFormat(Locale lcl) {
    	return ((KMyMoneyTransactionImpl) getTransaction()).getCurrencyFormat(lcl);
    }

    /**
     * @return The currencyFormat for the quantity to use when no locale is given.
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    protected NumberFormat getSharesCurrencyFormat() {
    	return getSharesCurrencyFormat(Locale.getDefault());
    }
    
    protected NumberFormat getSharesCurrencyFormat(Locale lcl) {
    	return ((KMyMoneyAccountImpl) getAccount()).getCurrencyFormat();
    }
    
    protected NumberFormat getPriceCurrencyFormat() {	
    	return getPriceCurrencyFormat(Locale.getDefault());
    }
    
    protected NumberFormat getPriceCurrencyFormat(Locale lcl) {
    	return getValueCurrencyFormat(lcl); // ::CHECK ::TODO
    }
    
    // ---------------------------------------------------------------

    public int compareTo(final KMyMoneyTransactionSplit otherSplt) {
		try {
			KMyMoneyTransaction otherTrans = otherSplt.getTransaction();
			int c = otherTrans.compareTo(getTransaction());
			if ( c != 0 ) {
				return c;
			}

			if ( ! otherSplt.getQualifID().equals(getQualifID()) ) {
				return otherSplt.getID().compareTo(getID());
			}

			if ( otherSplt != this ) {
				LOGGER.error("compareTo: Duplicate transaction-split-id! " + otherSplt.getQualifID() + "["
						+ otherSplt.getClass().getName() + "] and " + getQualifID() + "[" + getClass().getName() + "]\n"
						+ "split0=" + otherSplt.toString() + "\n" + "split1=" + toString() + "\n");
				IllegalStateException x = new IllegalStateException("DEBUG");
				x.printStackTrace();
			}

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
    }

    // ---------------------------------------------------------------

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyTransactionSplitImpl [");

		buffer.append("qualif-id=");
		buffer.append(getQualifID());

		// Part of qualif-id:
		// buffer.append(" transaction-id=");
		// buffer.append(getTransaction().getID());

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
