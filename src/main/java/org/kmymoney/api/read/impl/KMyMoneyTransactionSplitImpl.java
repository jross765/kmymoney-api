package org.kmymoney.api.read.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.generated.PAIR;
import org.kmymoney.api.generated.SPLIT;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.hlp.AmountFormatter_FP;
import org.kmymoney.api.read.impl.hlp.HasUserDefinedAttributesImpl;
import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.base.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.base.basetypes.simple.KMMSpltID;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
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
			if ( acct == null ) {
				// Account is mandatory --> error
				LOGGER.error("No such Account id='" + getAccountID() + "' for Transaction-Split with id '" + getQualifID()
					+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
					+ "' description '" + getTransaction().getMemo() + "'");
			} else {
				((KMyMoneyAccountImpl) acct).addTransactionSplit(this);
			}
		}
		
		if ( addSpltToPye ) {
			KMyMoneyPayee pye = getPayee();
			if ( pye == null ) {
				// Payee is optional --> just debug
				LOGGER.debug("No such Payee id='" + getPayeeID() + "' for Transaction-Split with id '" + getQualifID() 
			    	+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
			    	+ "' description '" + getTransaction().getMemo() + "'");
			} else {
				((KMyMoneyPayeeImpl) pye).addTransactionSplit(this);
			}
		}

		if ( addSpltToTags ) {
			Collection<KMyMoneyTag> tagList = getTags();
			if ( tagList == null ) {
				// Tags are optional --> just debug
				LOGGER.debug("No such Tag for Transaction-Split with id '" + getQualifID() 
			    	+ "' description '" + getMemo() + "' in transaction with id '" + getTransaction().getID()
			    	+ "' description '" + getTransaction().getMemo() + "'");
			} else {
				for ( KMyMoneyTag tag : tagList ) {
					((KMyMoneyTagImpl) tag).addTransactionSplit(this);
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
	public KMMAcctID getAccountID() {
		if ( jwsdpPeer.getAccount() == null )
			return null;
		
		if ( jwsdpPeer.getAccount().isBlank() )
			return null;

		return new KMMAcctID( jwsdpPeer.getAccount() );
	}

	/**
	 * @see KMyMoneyTransactionSplit#getAccount()
	 */
	@Override
	public KMyMoneyAccount getAccount() {
		return myTrx.getKMyMoneyFile().getAccountByID(getAccountID());
	}

	// ----------------------------
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNumber() {
		if ( jwsdpPeer.getNumber() == null )
			return null;
		
    	if ( jwsdpPeer.getNumber().isBlank() )
    		return null;
		
		return jwsdpPeer.getNumber();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBankID() {
		if ( jwsdpPeer.getBankid() == null )
			return null;
		
    	if ( jwsdpPeer.getBankid().isBlank() )
    		return null;
		
		return jwsdpPeer.getBankid();
	}
	
	// ----------------------------
	
	@Override
	public KMMPyeID getPayeeID() {
		if ( jwsdpPeer.getPayee() == null )
			return null;
		
		if ( jwsdpPeer.getPayee().isBlank() )
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
	public Collection<KMMTagID> getTagIDs() {
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
	
	@Override
	public boolean hasTag(KMMTagID tagID) {
		if ( tagID == null ) {
			throw new IllegalArgumentException("argument <tagID> is null");
		}
		
		if ( ! tagID.isSet() ) {
			throw new IllegalArgumentException("argument <tagID> is not set");
		}

		return getTagIDs().contains(tagID);
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
	@Deprecated
	public FixedPointNumber getValue() {
		return new FixedPointNumber(jwsdpPeer.getValue());
	}

	@Override
	public BigFraction getValueRat() {
		return BigFraction.parse(jwsdpPeer.getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueFormatted() {
		return getValueFormatted(Locale.getDefault());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValueFormatted(final Locale lcl) {
		return AmountFormatter_FP.formatAmount( getKMyMoneyFile(),
												getValue(), getTransaction().getQualifSecCurrID(), lcl );
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public FixedPointNumber getShares() {
		return new FixedPointNumber(jwsdpPeer.getShares());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigFraction getSharesRat() {
		return BigFraction.parse(jwsdpPeer.getShares());
	}

	/**
	 * {@inheritDoc}
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
	 */
	@Override
	public String getSharesFormatted(final Locale lcl) {
		return AmountFormatter_FP.formatAmount( getKMyMoneyFile(),
												getShares(), getAccount().getQualifSecCurrID(), lcl );
	}

	// ---------------------------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public FixedPointNumber getPrice() {
		if ( jwsdpPeer.getPrice() == null )
			return null;
		
		if ( jwsdpPeer.getPrice().isBlank() )
			return null;
		
		return new FixedPointNumber( jwsdpPeer.getPrice() );
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BigFraction getPriceRat() {
		if ( jwsdpPeer.getPrice() == null )
			return null;
		
		if ( jwsdpPeer.getPrice().isBlank() )
			return null;
		
		return BigFraction.parse(jwsdpPeer.getPrice());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPriceFormatted() {
		return getPriceFormatted(Locale.getDefault());
	}

	@Override
	public String getPriceFormatted(Locale lcl) {
		return AmountFormatter_FP.formatAmount( getKMyMoneyFile(),
												getPrice(), getTransaction().getQualifSecCurrID(), lcl );
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMemo() {
		if (jwsdpPeer.getMemo() == null)
			return null;
		
		if ( jwsdpPeer.getMemo().isBlank() )
			return null;
		
		return jwsdpPeer.getMemo();
	}
	
    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
	public String getUserDefinedAttribute(final String name) {
		if ( name == null ) {
			throw new IllegalArgumentException("argument <name> is null");
		}

		if ( name.isBlank() ) {
			throw new IllegalArgumentException("argument <name> is blank");
		}

		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeCore(kvpList, name);
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public List<String> getUserDefinedAttributeKeys() {
		if ( jwsdpPeer.getKEYVALUEPAIRS() == null) {
			return null;
		}
		
		List<PAIR> kvpList = jwsdpPeer.getKEYVALUEPAIRS().getPAIR();
		return HasUserDefinedAttributesImpl.getUserDefinedAttributeKeysCore(kvpList);
	}

	// ---------------------------------------------------------------

	public int compareTo(final KMyMoneyTransactionSplit otherSplt) {
		try {
			KMyMoneyTransaction otherTrans = otherSplt.getTransaction();
			int c = otherTrans.compareTo( getTransaction() );
			if ( c != 0 ) {
				return c;
			}

			if ( ! otherSplt.getQualifID().equals( getQualifID() ) ) {
				return otherSplt.getID().compareTo(getID());
			}

			if ( otherSplt != this ) {
				LOGGER.error("compareTo: Duplicate transaction-split-id! " + otherSplt.getQualifID() + "["
						+ otherSplt.getClass().getName() + "] and " + getQualifID() + "[" + getClass().getName() + "]\n"
						+ "split0=" + otherSplt.toString() + "\n" + "split1=" + toString() + "\n");
				IllegalStateException exc = new IllegalStateException("DEBUG");
				exc.printStackTrace();
			}

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyTransactionSplitImpl [");

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

		buffer.append(", bank-id='");
		buffer.append(getBankID() + "'");

		buffer.append(", payee-id=");
		buffer.append(getPayeeID());

		buffer.append(", tag-ids=");
		buffer.append(getTagIDs());

		buffer.append(", value=");
		buffer.append(getValueFormatted());

		buffer.append(", shares=");
		buffer.append(getSharesFormatted());

		buffer.append(", price=");
		if ( getPrice() != null ) {
			buffer.append(getPriceFormatted());
		} else {
			buffer.append("(unset)");
		}

		buffer.append(", memo='");
		buffer.append(getMemo() + "'");

		buffer.append("]");
		return buffer.toString();
	}

	@Override
	public String toXMLString() {
		try {
			JAXBContext context = JAXBContext.newInstance(SPLIT.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter writer = new StringWriter();
			marshaller.marshal(jwsdpPeer, writer);
			return writer.toString();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to convert JWSDP peer to XML", e);
		}
	}

}
