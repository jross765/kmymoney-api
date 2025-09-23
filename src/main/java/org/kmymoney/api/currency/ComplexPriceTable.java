package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class ComplexPriceTable implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComplexPriceTable.class);

	private static final long serialVersionUID = -8548432873139328788L;

	// ---------------------------------------------------------------

	public interface ComplexPriceTableChangeListener {
		void conversionFactorChanged(final String currency, final FixedPointNumber factor);
	}

	// -----------------------------------------------------------

	private transient volatile List<ComplexPriceTableChangeListener> listeners = null;

	// -----------------------------------------------------------

	private Map<KMMQualifSecCurrID.Type, SimplePriceTable> namespace2CurrTab = null;

	// -----------------------------------------------------------

	public ComplexPriceTable() {
		namespace2CurrTab = new HashMap<KMMQualifSecCurrID.Type, SimplePriceTable>();

		addForNameSpace(KMMQualifSecCurrID.Type.CURRENCY, new SimpleCurrencyExchRateTable());
		addForNameSpace(KMMQualifSecCurrID.Type.SECURITY, new SimpleSecurityQuoteTable());
	}

	// -----------------------------------------------------------

	public void addComplexPriceTableChangeListener(final ComplexPriceTableChangeListener listener) {
		if ( listeners == null ) {
			listeners = new ArrayList<ComplexPriceTableChangeListener>();
		}
		listeners.add(listener);
	}

	public void removeComplexPriceTableChangeListener(final ComplexPriceTableChangeListener listener) {
		if ( listeners == null ) {
			listeners = new ArrayList<ComplexPriceTableChangeListener>();
		}
		listeners.remove(listener);
	}

	protected void firePriceTableChanged(final String curr, final FixedPointNumber factor) {
		if ( curr == null ) {
			throw new IllegalArgumentException("null currency given");
		}

		if ( curr.trim().equals("") ) {
			throw new IllegalArgumentException("empty currency given");
		}
		
		if ( factor == null ) {
			throw new IllegalArgumentException("null factor given");
		}
		
		if ( listeners != null ) {
			for ( ComplexPriceTableChangeListener listener : listeners ) {
				listener.conversionFactorChanged(curr, factor);
			}
		}
	}

	// ------------------------ support for propertyChangeListeners
	/// **
	// * support for firing PropertyChangeEvents
	// * (gets initialized only if we really have listeners)
	// */
	// protected volatile PropertyChangeSupport propertyChange = null;
	//
	/// **
	// * Add a PropertyChangeListener to the listener list.
	// * The listener is registered for all properties.
	// *
	// * @param listener The PropertyChangeListener to be added
	// */
	// public final void addPropertyChangeListener(
	// final PropertyChangeListener listener) {
	// if (propertyChange == null) {
	// propertyChange = new PropertyChangeSupport(this);
	// }
	// propertyChange.addPropertyChangeListener(listener);
	// }
	//
	/// **
	// * Add a PropertyChangeListener for a specific property. The listener
	// * will be invoked only when a call on firePropertyChange names that
	// * specific property.
	// *
	// * @param propertyName The name of the property to listen on.
	// * @param listener The PropertyChangeListener to be added
	// */
	// public final void addPropertyChangeListener(
	// final String propertyName,
	// final PropertyChangeListener listener) {
	// if (propertyChange == null) {
	// propertyChange = new PropertyChangeSupport(this);
	// }
	// propertyChange.addPropertyChangeListener(propertyName, listener);
	// }
	//
	/// **
	// * Remove a PropertyChangeListener for a specific property.
	// *
	// * @param propertyName The name of the property that was listened on.
	// * @param listener The PropertyChangeListener to be removed
	// */
	// public final void removePropertyChangeListener(
	// final String propertyName,
	// final PropertyChangeListener listener) {
	// if (propertyChange != null) {
	// propertyChange.removePropertyChangeListener(propertyName, listener);
	// }
	// }
	//
	/// **
	// * Remove a PropertyChangeListener from the listener list.
	// * This removes a PropertyChangeListener that was registered
	// * for all properties.
	// *
	// * @param listener The PropertyChangeListener to be removed
	// */
	// public synchronized void removePropertyChangeListener(
	// final PropertyChangeListener listener) {
	// if (propertyChange != null) {
	// propertyChange.removePropertyChangeListener(listener);
	// }
	// }
	//
	// -------------------------------------------------------

	/**
	 * Add a new name space with no conversion-factors.<br/>
	 * Will not overwrite an existing name space.
	 *
	 * @param nameSpace the new nameSpace to add.
	 */
	public void addForNameSpace(final KMMQualifSecCurrID.Type nameSpace) {
		if ( namespace2CurrTab.keySet().contains(nameSpace) ) {
			return;
		}

		if ( nameSpace == KMMQualifSecCurrID.Type.CURRENCY ) {
			SimpleCurrencyExchRateTable table = new SimpleCurrencyExchRateTable();
			table.clear();
			addForNameSpace(nameSpace, table);
		} else if ( nameSpace == KMMQualifSecCurrID.Type.SECURITY ) {
			SimpleSecurityQuoteTable table = new SimpleSecurityQuoteTable();
			table.clear();
			addForNameSpace(nameSpace, table);
		}
	}

	/**
	 * Add a new name space with an initial set of conversion-factors.
	 *
	 * @param nameSpace the new nameSpace to add.
	 * @param table     an initial set of conversion-factors.
	 */
	public void addForNameSpace(final KMMQualifSecCurrID.Type nameSpace, final SimplePriceTable table) {
		namespace2CurrTab.put(nameSpace, table);
		LOGGER.debug("addForNameSpace: Added new table for name space '" + nameSpace + "'");
	}

	// ---------------------------------------------------------------

	/**
	 * @return 
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
	public FixedPointNumber getConversionFactor(final KMMQualifSecCurrID.Type nameSpace, final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("empty code given");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			return null;
		}

		return table.getConversionFactor(code);
	}

	/**
	 * If the nameSpace does not exist yet, it is created.
	 * 
	 * @param nameSpace 
	 * @param code 
	 * @param pFactor 
	 *
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
	public void setConversionFactor(final KMMQualifSecCurrID.Type nameSpace, final String code,
			final FixedPointNumber pFactor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("empty code given");
		}

		if ( pFactor == null ) {
		    throw new IllegalArgumentException("argument <pFactor> is null");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			addForNameSpace(nameSpace);
			table = getByNamespace(nameSpace);
		}

		table.setConversionFactor(code, pFactor);

		firePriceTableChanged(code, pFactor);
	}

	// ---------------------------------------------------------------

	/**
	 * @param pValue 
	 * @param secCurrID 
	 * @return 
	 * @see SimplePriceTable#convertFromBaseCurrency(FixedPointNumber,
	 *      java.lang.String)
	 */
	public boolean convertFromBaseCurrency(final FixedPointNumber pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null )
			throw new IllegalArgumentException("null value given");

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null security/currency ID given"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());
		if ( table == null ) {
			return false;
		}

		return table.convertFromBaseCurrency(pValue, secCurrID.getCode());
	}
	
	/*
	public boolean convertFromBaseCurrency(FixedPointNumber pValue, final Currency curr) {

		SimplePriceTable table = getByNamespace(KMMQualifSecCurrID.Type.CURRENCY);
		if ( table == null ) {
			return false;
		}

		return table.convertFromBaseCurrency(pValue, curr.getCurrencyCode());
	}
	
	public boolean convertFromBaseCurrency(FixedPointNumber pValue, final KMMSecID secID) throws KMMIDNotSetException {

		SimplePriceTable table = getByNamespace(KMMQualifSecCurrID.Type.SECURITY);
		if ( table == null ) {
			return false;
		}

		return table.convertFromBaseCurrency(pValue, secID.get());
	}
	*/
	
	// ----------------------------

	public boolean convertToBaseCurrency(final FixedPointNumber pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null ) {
			throw new IllegalArgumentException("null value given"); 
		}
		
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null security/currency ID given"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("unset security/currency ID given"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());

		if ( table == null ) {
			return false;
		}

		return table.convertToBaseCurrency(pValue, secCurrID.getCode());
	}
	
	/*
	public boolean convertToBaseCurrency(FixedPointNumber pValue, final Currency curr) {

		SimplePriceTable table = getByNamespace(KMMQualifSecCurrID.Type.CURRENCY);

		if ( table == null ) {
			return false;
		}

		return table.convertToBaseCurrency(pValue, curr.getCurrencyCode());
	}

	public boolean convertToBaseCurrency(FixedPointNumber pValue, final KMMSecID secID) throws KMMIDNotSetException {

		SimplePriceTable table = getByNamespace(KMMQualifSecCurrID.Type.SECURITY);

		if ( table == null ) {
			return false;
		}

		return table.convertToBaseCurrency(pValue, secID.get());
	}
	*/

	// ---------------------------------------------------------------

	/**
	 * @return
	 */
	public List<KMMQualifSecCurrID.Type> getNameSpaces() {
		ArrayList<KMMQualifSecCurrID.Type> result = new ArrayList<KMMQualifSecCurrID.Type>(namespace2CurrTab.keySet());
		Collections.sort(result);
		return result;
	}

	/**
	 * @param type
	 * @return
	 */
	protected SimplePriceTable getByNamespace(KMMQualifSecCurrID.Type type) {
		return namespace2CurrTab.get(type);
	}

	/**
	 * @param tyoe
	 * @return 
	 */
	public List<String> getCurrencies(final KMMQualifSecCurrID.Type tyoe) {
		SimplePriceTable table = getByNamespace(tyoe);
		if ( table == null ) {
			return new ArrayList<String>();
		}
		return table.getCurrencies();
	}

	// ---------------------------------------------------------------

	/**
	 * @see SimplePriceTable#clear()
	 */
	public void clear() {
		for ( KMMQualifSecCurrID.Type nameSpace : namespace2CurrTab.keySet() ) {
			namespace2CurrTab.get(nameSpace).clear();
		}

		namespace2CurrTab.clear();
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		String result = "ComplexPriceTable [\n";

		for ( KMMQualifSecCurrID.Type nameSpace : getNameSpaces() ) {
			if ( nameSpace != KMMQualifSecCurrID.Type.UNSET ) {
				result += "=======================================\n";
				result += "Name space: " + nameSpace + "\n";
				result += "=======================================\n";
				result += getByNamespace(nameSpace).toString() + "\n";
			}
		}

		result += "]";

		return result;
	}

}
