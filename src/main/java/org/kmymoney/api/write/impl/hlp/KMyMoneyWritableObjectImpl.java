package org.kmymoney.api.write.impl.hlp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.kmymoney.api.read.impl.hlp.KMyMoneyObjectImpl;
import org.kmymoney.api.write.KMyMoneyWritableFile;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of KMyMoneyObjectImpl to allow read-write access instead of
 * read-only access.
 */
public class KMyMoneyWritableObjectImpl extends KMyMoneyObjectImpl
                                        implements KMyMoneyWritableObject 
{

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyWritableObjectImpl.class);

	// ---------------------------------------------------------------

	private Object obj;

	/**
	 * support for firing PropertyChangeEvents. (gets initialized only if we really
	 * have listeners)
	 */
	private volatile PropertyChangeSupport myPtyChg = null;

	// ---------------------------------------------------------------

	public KMyMoneyWritableObjectImpl(final KMyMoneyWritableFile myFile) {
		super(myFile);
		// TODO implement constructor for KMyMoneyWritableObjectHelper
	}

	/**
	 * @param myFile 
	 * @param obj the object we are helping with
	 */
	public KMyMoneyWritableObjectImpl(final KMyMoneyWritableFile myFile, final Object obj) {
		super(myFile);
		this.obj = obj;
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public KMyMoneyWritableFile getWritableKMyMoneyFile() {
		return (KMyMoneyWritableFile) getKMyMoneyFile();
	}

	// ---------------------------------------------------------------

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	public PropertyChangeSupport getPropertyChangeSupport() {
		return myPtyChg;
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPtyChg == null ) {
			myPtyChg = new PropertyChangeSupport(this);
		}
		
		myPtyChg.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will be
	 * invoked only when a call on firePropertyChange names that specific property.
	 *
	 * @param ptyName  The name of the property to listen on.
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final String ptyName, final PropertyChangeListener listener) {
		if ( ptyName == null )
			throw new IllegalArgumentException("null property name given");
		
		if ( ptyName.isEmpty() )
			throw new IllegalArgumentException("empty property name given");

		if ( myPtyChg == null ) {
			myPtyChg = new PropertyChangeSupport(this);
		}
		
		myPtyChg.addPropertyChangeListener(ptyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param ptyName  The name of the property that was listened on.
	 * @param listener The PropertyChangeListener to be removed
	 */
	public final void removePropertyChangeListener(final String ptyName, final PropertyChangeListener listener) {
		if ( ptyName == null )
			throw new IllegalArgumentException("null property name given");
		
		if ( ptyName.isEmpty() )
			throw new IllegalArgumentException("empty property name given");

		if ( myPtyChg != null ) {
			myPtyChg.removePropertyChangeListener(ptyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPtyChg != null ) {
			myPtyChg.removePropertyChangeListener(listener);
		}
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		return "KMyMoneyWritableObjectImpl@" + hashCode();
	}

}
