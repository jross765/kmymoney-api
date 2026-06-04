package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.generated.ACCOUNT;
import org.kmymoney.api.generated.BUDGET;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.read.impl.KMyMoneyBudgetImpl;
import org.kmymoney.api.read.impl.aux.KMMBudgetAccountImpl;
import org.kmymoney.api.read.impl.aux.KMMBudgetPeriodImpl;
import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.ObjectCascadeException;
import org.kmymoney.api.write.aux.KMMWritableBudgetAccount;
import org.kmymoney.api.write.aux.KMMWritableBudgetPeriod;
import org.kmymoney.api.write.impl.aux.KMMWritableBudgetAccountImpl;
import org.kmymoney.api.write.impl.aux.KMMWritableBudgetPeriodImpl;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMBdgtID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyBudgetImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableBudgetImpl extends KMyMoneyBudgetImpl 
                                        implements KMyMoneyWritableBudget 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableBudgetImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    /**
     * Please use {@link KMyMoneyWritableFile#createWritableBudget(String)}
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
	public KMyMoneyWritableBudgetImpl(
			final BUDGET jwsdpPeer,
			final KMyMoneyWritableFileImpl file) {
    	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableBudget(String)}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected KMyMoneyWritableBudgetImpl(final KMyMoneyWritableFileImpl file) {
    	super(createBudget_int(file, file.getNewBudgetID()), file);
    }

    public KMyMoneyWritableBudgetImpl(final KMyMoneyBudgetImpl bdgt) {
    	super(bdgt.getJwsdpPeer(), bdgt.getKMyMoneyFile());
    }

    // ---------------------------------------------------------------

	/**
	 * Create a new split for a split found in the jaxb-data.
	 *
	 * @param jwsdpSplt the jaxb-data
	 * @return the new split-instance
	 */
	protected KMMWritableBudgetAccountImpl createBudgetAccount(
			final ACCOUNT jwsdpBdgtAcct
		    /* , final boolean addToAcct */ ) { // ::TODO
		KMMWritableBudgetAccountImpl bdgtAcct = 
				new KMMWritableBudgetAccountImpl(this, jwsdpBdgtAcct, getWritableKMyMoneyFile());
		
		if ( helper.getPropertyChangeSupport() != null ) {
			 helper.getPropertyChangeSupport().firePropertyChange("accounts", null, getWritableAccounts());
		}

		return bdgtAcct;
	}

	/**
	 * @see KMyMoneyWritableTransaction#createWritableSplit(KMyMoneyAccount)
	 */
	@Override
	public KMMWritableBudgetAccount createWritableAccount(
			final KMMAcctID acctID,
			final KMMBudgetAccount.Level lvl) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		KMMWritableBudgetAccountImpl bdgtAcct = new KMMWritableBudgetAccountImpl(this, acctID, lvl);
		addAccount(bdgtAcct);
		
		if ( helper.getPropertyChangeSupport() != null ) {
			 helper.getPropertyChangeSupport().firePropertyChange("accounts", null, getWritableAccounts());
		}
		
		return bdgtAcct;
	}

    /**
     * Creates a new budget and adds it to the given KMyMoney file Don't modify
     * the ID of the new transaction!
     *
     * @param file the file we will belong to
     * @param guid the ID we shall have
     * @return a new jwsdp-peer already entered into th jwsdp-peer of the file
     */
    protected static BUDGET createBudget_int(
    		final KMyMoneyWritableFileImpl file, 
    		final KMMBdgtID newID) {
		if ( file == null ) {
			throw new IllegalArgumentException("argument <file> is null");
		}

		if ( newID == null ) {
			throw new IllegalArgumentException("argument <newID> is null");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("argument <newID> is not set");
		}
    
        ObjectFactory factory = file.getObjectFactory();
    
        BUDGET jwsdpBdgt = factory.createBUDGET();
    
        jwsdpBdgt.setId(newID.toString());
        jwsdpBdgt.setName("no name given");
    
        file.getRootElement().getBUDGETS().getBUDGET().add(jwsdpBdgt);
        file.setModified(true);
    
        LOGGER.debug("createBudget_int: Created new budget (core): " + jwsdpBdgt.getId());
        
        return jwsdpBdgt;
    }

    // ---------------------------------------------------------------

//	protected void setAddress(final KMMWritableAddressImpl addr) {
//		super.setAddress(addr);
//	}

    /**
     * Delete this Budget and remove it from the file.
     * @throws ObjectCascadeException 
     *
     * @see KMyMoneyWritableBudget#remove()
     */
    @Override
    public void remove() throws ObjectCascadeException {
    	if ( hasAccounts() ) {
    		throw new ObjectCascadeException();
    	}
    	
//    	for ( KMMBudgetAccount bdgtAcct : getAccounts() ) {
//    		if ( bdgtAcct.hasPeriods() ) {
//        		throw new ObjectCascadeException();
//    		}
//    	}

    	BUDGET peer = jwsdpPeer;
    	getKMyMoneyFile().getRootElement().getBUDGETS().getBUDGET().remove(peer);
    	getWritableKMyMoneyFile().removeBudget(this);
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

//    /**
//     * The KMyMoney file is the top-level class to contain everything.
//     *
//     * @return the file we are associated with
//     */
//    @Override
//    public KMyMoneyWritableFileImpl getKMyMoneyFile() {
//    	return (KMyMoneyWritableFileImpl) super.getKMyMoneyFile();
//    }

    // ---------------------------------------------------------------

    /**
     * @see KMyMoneyWritableBudget#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("argument <name> is null");
    	}

    	if ( name.isBlank() ) {
    		throw new IllegalArgumentException("argument <name> is blank");
    	}

    	String oldName = getName();
    	jwsdpPeer.setName(name);
    	getWritableKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("name", oldName, name);
    	}
    }

	@Override
	public void setStart(LocalDate startDate) {
		if ( startDate == null ) {
			throw new IllegalArgumentException("argument <datePosted> is null");
		}

    	LocalDate oldStartDate = getStart();
    	
		try {
	        // https://stackoverflow.com/questions/835889/java-util-date-to-xmlgregoriancalendar
			// https://simplesolution.dev/java-convert-localdate-to-calendar/
	        GregorianCalendar cal = new GregorianCalendar();
	        ZonedDateTime zonedDateTime = startDate.atStartOfDay(ZoneId.systemDefault());
	        Instant instant = zonedDateTime.toInstant();
	        Date date = Date.from(instant);
	        cal.setTime(date);
	        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
			jwsdpPeer.setStart(xmlCal);
		} catch ( DatatypeConfigurationException exc ) {
			throw new DateMappingException();
		}

		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("start", oldStartDate, startDate);
		}
	}

    // ---------------------------------------------------------------

	@Override
	public void clearAccounts() {
		if ( getAccounts() == null )
			return;
		
		if ( getAccounts().size() == 0 )
			return;
		
		// ---
		
		List<KMMBudgetAccount> oldAccts = getAccounts();
		
		for ( int i = jwsdpPeer.getACCOUNT().size(); i >= 0; i-- ) {
			jwsdpPeer.getACCOUNT().remove(i);
		}

		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("accounts", oldAccts, null);
		}
	}

	@Override
	public void removeAccount(final KMMBudgetAccount bdgtAcct) {
		if ( bdgtAcct == null ) {
			throw new IllegalArgumentException("argument <bdgtAcct> is null");
		}
		
		if ( getAccounts() == null )
			return;
		
		if ( getAccounts().size() == 0 )
			return;
		
		// ---
		
		List<KMMBudgetAccount> oldAccts = getAccounts();
		
		jwsdpPeer.getACCOUNT()
			.remove(((KMMBudgetAccountImpl) bdgtAcct).getJwsdpPeer());
		
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("accounts", oldAccts, getAccounts());
		}
	}

	public void addAccount(final KMMWritableBudgetAccountImpl bdgtAcct) {
		super.addAccount(bdgtAcct);		
	}

	@Override
	public List<KMMWritableBudgetAccount> getWritableAccounts() {
		List<KMMWritableBudgetAccount> result = new ArrayList<KMMWritableBudgetAccount>();
		
		for ( KMMBudgetAccount bdgtAcct : super.getAccounts() ) {
			KMMWritableBudgetAccount newBdgtAcct = new KMMWritableBudgetAccountImpl((KMMBudgetAccountImpl) bdgtAcct);
		    result.add(newBdgtAcct);
		}

		return result;
	}

	@Override
	public KMMWritableBudgetAccount getWritableAccount(KMMAcctID acctID) {
		if ( acctID == null ) {
			throw new IllegalArgumentException("argument <acctID> is null");
		}

		if ( ! acctID.isSet() ) {
			throw new IllegalArgumentException("argument <acctID> is not set");
		}

		return new KMMWritableBudgetAccountImpl( (KMMBudgetAccountImpl) getAccount(acctID) );
	}

    // ----------------------------

	@Override
	public void clearPeriods(KMMAcctID acctID) {
		List<KMMBudgetPeriod> oldPrds = getPeriods(acctID);
		
		for ( KMMWritableBudgetAccount bdgtAcct : getWritableAccounts() ) {
			bdgtAcct.clearPeriods();
		}
		
		getWritableKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("periods", oldPrds, getAccounts());
		}
	}

	@Override
	public List<KMMWritableBudgetPeriod> getWritablePeriods(final KMMAcctID acctID) {
		List<KMMWritableBudgetPeriod> result = new ArrayList<KMMWritableBudgetPeriod>();

		for ( KMMBudgetPeriod bdgtPrd : super.getPeriods(acctID) ) {
			KMMWritableBudgetPeriod newBdgtPrd = new KMMWritableBudgetPeriodImpl((KMMBudgetPeriodImpl) bdgtPrd);
		    result.add(newBdgtPrd);
		}

		return result;
	}

    // ---------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneyWritableBudgetImpl ";

		result += "[id=" + getID();

		result += ", name='" + getName() + "'";
		result += ", start=" + getStart();

		result += "]";

		return result;
    }

}
