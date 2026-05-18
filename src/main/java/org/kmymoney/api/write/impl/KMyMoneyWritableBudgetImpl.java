package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;

import org.kmymoney.api.generated.BUDGET;
import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.read.impl.KMyMoneyBudgetImpl;
import org.kmymoney.api.write.KMyMoneyWritableBudget;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
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
     * Please use ${@link KMyMoneyWritableFile#createWritableBudget()}.
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
     * Please use ${@link KMyMoneyWritableFile#createWritableBudget()}.
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
     * Creates a new Transaction and add's it to the given KMyMoney file Don't modify
     * the ID of the new transaction!
     *
     * @param file the file we will belong to
     * @param guid the ID we shall have
     * @return a new jwsdp-peer already entered into th jwsdp-peer of the file
     */
    protected static BUDGET createBudget_int(
    		final KMyMoneyWritableFileImpl file, 
    		final KMMBdgtID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("argument <newID> is null");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("argument <newID> is not set");
		}
    
        ObjectFactory factory = file.getObjectFactory();
    
        BUDGET jwsdpBdgt = file.createBudgetType();
    
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
     *
     * @see KMyMoneyWritableBudget#remove()
     */
    @Override
    public void remove() {
    	BUDGET peer = jwsdpPeer;
    	(getKMyMoneyFile()).getRootElement().getBUDGETS().getBUDGET().remove(peer);
    	(getKMyMoneyFile()).removeBudget(this);
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
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("name", oldName, name);
    	}
    }

//	@Override
//	public void setStart(LocalDate date) {
//		// TODO Auto-generated method stub
//		
//	}
    
    // ---------------------------------------------------------------

    @Override
    public String toString() {
		String result = "KMyMoneyWritableBudgetImpl ";

		result += "[id=" + getID();

		result += ", name='" + getName() + "'";
		result += ", start=" + getStart();
		result += ", version='" + getVersion() + "'";

		result += "]";

		return result;
    }

}
