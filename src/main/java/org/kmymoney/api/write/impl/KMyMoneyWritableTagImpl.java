package org.kmymoney.api.write.impl;

import java.beans.PropertyChangeSupport;

import org.kmymoney.api.generated.ObjectFactory;
import org.kmymoney.api.generated.TAG;
import org.kmymoney.api.read.KMyMoneyTransactionSplit;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyTagImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.api.write.impl.hlp.KMyMoneyWritableObjectImpl;
import org.kmymoney.base.basetypes.simple.KMMTagID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyTagImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableTagImpl extends KMyMoneyTagImpl 
                                     implements KMyMoneyWritableTag 
{

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableTagImpl.class);

    // ---------------------------------------------------------------

    // Our helper to implement the KMyMoneyWritableObject-interface.
    private final KMyMoneyWritableObjectImpl helper = new KMyMoneyWritableObjectImpl(getWritableKMyMoneyFile(), this);

    // ---------------------------------------------------------------

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableTag()}.
     *
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
	public KMyMoneyWritableTagImpl(
			final TAG jwsdpPeer,
			final KMyMoneyWritableFileImpl file) {
    	super(jwsdpPeer, file);
    }

    /**
     * Please use ${@link KMyMoneyWritableFile#createWritableTag()}.
     *
     * @param file the file we belong to
     * @param id   the ID we shall have
     */
    protected KMyMoneyWritableTagImpl(final KMyMoneyWritableFileImpl file) {
    	super(createTag_int(file, file.getNewTagID()), file);
    }

	public KMyMoneyWritableTagImpl(final KMyMoneyTagImpl tag, final boolean addSplits) {
		super(tag.getJwsdpPeer(), tag.getKMyMoneyFile());

		if ( addSplits ) {
		    for ( KMyMoneyTransactionSplit splt : ((KMyMoneyFileImpl) tag.getKMyMoneyFile()).getTransactionSplits_readAfresh() ) {
		    	if ( splt.getTagIDs() != null ) { // Caution: Tags are optional for split, as opposed to account
		    									  // Cf. KMyMoneyWritableAccountImpl
		    		for ( KMMTagID tagID : splt.getTagIDs() ) {
			    		if ( tagID.equals(tag.getID()) ) {
				    		super.addTransactionSplit(splt);
					    // NO:
//						    addTransactionSplit(new KMyMoneyTransactionSplitImpl(splt.getJwsdpPeer(), splt.getTransaction(), 
//				                                false, false));
				    	}
		    		}
		    	}
		    }
		}
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
    protected static TAG createTag_int(
    		final KMyMoneyWritableFileImpl file, 
    		final KMMTagID newID) {
		if ( newID == null ) {
			throw new IllegalArgumentException("null ID given");
		}

		if ( ! newID.isSet() ) {
			throw new IllegalArgumentException("unset ID given");
		}
    
        ObjectFactory factory = file.getObjectFactory();
    
        TAG jwsdpTag = file.createTagType();
    
        jwsdpTag.setId(newID.toString());
        jwsdpTag.setName("no name given");
    
        file.getRootElement().getTAGS().getTAG().add(jwsdpTag);
        file.setModified(true);
    
        LOGGER.debug("createTag_int: Created new tag (core): " + jwsdpTag.getId());
        
        return jwsdpTag;
    }

    // ---------------------------------------------------------------

//	protected void setAddress(final KMMWritableAddressImpl addr) {
//		super.setAddress(addr);
//	}
//
    /**
     * Delete this Tag and remove it from the file.
     *
     * @see KMyMoneyWritableTag#remove()
     */
    @Override
    public void remove() {
    	TAG peer = jwsdpPeer;
    	(getKMyMoneyFile()).getRootElement().getTAGS().getTAG().remove(peer);
    	(getKMyMoneyFile()).removeTag(this);
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
     * @see KMyMoneyWritableTag#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
    	if ( name == null ) {
    		throw new IllegalArgumentException("null name given!");
    	}

    	if ( name.trim().length() == 0 ) {
    		throw new IllegalArgumentException("empty name given!");
    	}

    	String oldName = getName();
    	jwsdpPeer.setName(name);
    	getKMyMoneyFile().setModified(true);

    	PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
    	if ( propertyChangeSupport != null) {
    		propertyChangeSupport.firePropertyChange("name", oldName, name);
    	}
    }

    /**
     * @param notes user-defined notes about the customer (may be null)
     * @see KMyMoneyWritableCustomer#setNotes(String)
     */
    @Override
    public void setNotes(final String notes) {
		if ( notes == null ) {
			throw new IllegalArgumentException("null notes given");
		}
		
		if ( notes.isEmpty() ) {
			throw new IllegalArgumentException("empty notes given");
		}

		// Caution: empty string allowed here
//	if ( notes.trim().length() == 0 ) {
//	    throw new IllegalArgumentException("empty notesgiven!");
//	}

		String oldNotes = getNotes();
		jwsdpPeer.setNotes(notes);
		getKMyMoneyFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = helper.getPropertyChangeSupport();
		if ( propertyChangeSupport != null ) {
			propertyChangeSupport.firePropertyChange("notes", oldNotes, notes);
		}
    }

	@Override
	public void setColor(String clr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClosed(boolean val) {
		// TODO Auto-generated method stub
		
	}

    // -----------------------------------------------------------------

    @Override
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("KMyMoneyWritableTagImpl [");

		buffer.append("id=");
		buffer.append(getID());

		buffer.append(", name='");
		buffer.append(getName() + "'");

		buffer.append(", notes='");
		buffer.append(getNotes() + "'");

		buffer.append(", color='");
		buffer.append(getColor() + "'");

		buffer.append("]");
		return buffer.toString();
    }

}
