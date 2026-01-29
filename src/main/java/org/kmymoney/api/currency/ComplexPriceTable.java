package org.kmymoney.api.currency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
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
		void conversionFactorChanged(final String currency, final BigFraction factor);
	}

	// -----------------------------------------------------------

	private transient volatile List<ComplexPriceTableChangeListener> listeners = null;

	// -----------------------------------------------------------

	private Map<KMMQualifSecCurrID.Type, SimplePriceTable> secCurrType2PrcTab = null;

	// -----------------------------------------------------------

	public ComplexPriceTable() {
		secCurrType2PrcTab = new HashMap<KMMQualifSecCurrID.Type, SimplePriceTable>();

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

	protected void firePriceTableChanged(final String code, final FixedPointNumber factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}
		
		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}

		// CAUTION: One might think that this check is a good idea.
		// In fact, it is not, because it actually happens in real-life
		// KMyMoney files that you will have null-price entries.
		// (KMyMoney issues warnings, but tolerates them).
		// ==> No exception, but only warning 
		if ( factor.compareTo(FixedPointNumber.ZERO) <= 0 ) {
			LOGGER.error("firePriceTableChanged: Encountered factor <= 0 for currency '" + code + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> must be > 0");
		}
		
		if ( listeners != null ) {
			for ( ComplexPriceTableChangeListener listener : listeners ) {
				listener.conversionFactorChanged(code, factor);
			}
		}
	}

	protected void firePriceTableChanged(final String code, final BigFraction factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}
		
		if ( factor == null ) {
			throw new IllegalArgumentException("argument <factor> is null");
		}
		
		// CAUTION: One might think that this check is a good idea.
		// In fact, it is not, because it actually happens in real-life
		// KMyMoney files that you will have null-price entries.
		// (KMyMoney issues warnings, but tolerates them).
		// ==> No exception, but only warning 
		if ( factor.compareTo(BigFraction.ZERO) <= 0 ) {
			LOGGER.error("firePriceTableChanged: Encountered factor <= 0 for currency '" + code + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> must be > 0");
		}
		
		if ( listeners != null ) {
			for ( ComplexPriceTableChangeListener listener : listeners ) {
				listener.conversionFactorChanged(code, factor);
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
		if ( secCurrType2PrcTab.keySet().contains(nameSpace) ) {
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
		secCurrType2PrcTab.put(nameSpace, table);
		LOGGER.debug("addForNameSpace: Added new table for name space '" + nameSpace + "'");
	}

	// ---------------------------------------------------------------

	/**
	 * @param nameSpace 
	 * @param code 
	 * @return the factor to convert the price specified by the name-space-code-pair
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
	public FixedPointNumber getConversionFactor(final KMMQualifSecCurrID.Type nameSpace, final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			return null;
		}

		return table.getConversionFactor(code);
	}

	public FixedPointNumber getConversionFactor(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		return getConversionFactor(secCurrID.getType(), secCurrID.getCode());
	}

	public FixedPointNumber getConversionFactor(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMQualifCurrID currID = new KMMQualifCurrID(curr);
		return getConversionFactor(currID);
	}

	// ----------------------------

	public BigFraction getConversionFactorRat(final KMMQualifSecCurrID.Type nameSpace, final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			return null;
		}

		return table.getConversionFactorRat(code);
	}

	public BigFraction getConversionFactorRat(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		return getConversionFactorRat(secCurrID.getType(), secCurrID.getCode());
	}

	public BigFraction getConversionFactorRat(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMQualifCurrID currID = new KMMQualifCurrID(curr);
		return getConversionFactorRat(currID);
	}

	// ----------------------------

	/**
	 * If the nameSpace does not exist yet, it is created.
	 * 
	 * @param nameSpace 
	 * @param code 
	 * @param factor 
	 *
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
	public void setConversionFactor(final KMMQualifSecCurrID.Type nameSpace, final String code,
			final FixedPointNumber factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}

		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}

		// CAUTION: One might think that this check is a good idea.
		// In fact, it is not, because it actually happens in real-life
		// KMyMoney files that you will have null-price entries.
		// (KMyMoney issues warnings, but tolerates them).
		// ==> No exception, but only warning 
		if ( factor.compareTo(FixedPointNumber.ZERO) <= 0 ) {
			LOGGER.error("setConversionFactor: Encountered factor <= 0 for currency/security '" + nameSpace + ":" + code + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> must be > 0");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			addForNameSpace(nameSpace);
			table = getByNamespace(nameSpace);
		}

		table.setConversionFactor(code, factor);
		table.setConversionFactorRat(code, factor.toBigFraction());

		firePriceTableChanged(code, factor);
	}

	public void setConversionFactor(final KMMQualifSecCurrID secCurrID, final FixedPointNumber factor) {
		if ( secCurrID == null ) {
		    throw new IllegalArgumentException("argument <secCurrID> is null");
		}
	
		if ( ! secCurrID.isSet() ) {
		    throw new IllegalArgumentException("argument <secCurrID> is not set");
		}
	
		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}
	
		setConversionFactor(secCurrID.getType(), secCurrID.getCode(),
			            factor);
	}
	
	public void setConversionFactor(final Currency curr, final FixedPointNumber factor) {
		if ( curr == null ) {
		    throw new IllegalArgumentException("argument <curr> is null");
		}
	
		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}
	
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr);
		setConversionFactor(secCurrID, factor);
	}
	
	// ----------------------------

	public void setConversionFactorRat(final KMMQualifSecCurrID.Type nameSpace, final String code,
			final BigFraction factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.trim().equals("") ) {
			throw new IllegalArgumentException("argument <code> is empty");
		}

		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}
		
		// ::TODO ::CHECK
		// In the sister project, we had to remove this check (cf. comment there).
		// What about GnuCash?
		if ( factor.compareTo(BigFraction.ZERO) <= 0 ) {
			throw new IllegalArgumentException("argument <factor> must be > 0");
		}

		SimplePriceTable table = getByNamespace(nameSpace);
		if ( table == null ) {
			addForNameSpace(nameSpace);
			table = getByNamespace(nameSpace);
		}

		table.setConversionFactor(code, FixedPointNumber.of(factor));
		table.setConversionFactorRat(code, factor);

		firePriceTableChanged(code, factor);
	}

	public void setConversionFactorRat(final KMMQualifSecCurrID secCurrID, final BigFraction factor) {
		if ( secCurrID == null ) {
		    throw new IllegalArgumentException("argument <secCurrID> is null");
		}
	
		if ( ! secCurrID.isSet() ) {
		    throw new IllegalArgumentException("argument <secCurrID> is not set");
		}
	
		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}
	
		setConversionFactorRat(secCurrID.getType(), secCurrID.getCode(),
			            factor);
	}
	
	public void setConversionFactorRat(final Currency curr, final BigFraction factor) {
		if ( curr == null ) {
		    throw new IllegalArgumentException("argument <curr> is null");
		}
	
		if ( factor == null ) {
		    throw new IllegalArgumentException("argument <factor> is null");
		}
	
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr);
		setConversionFactorRat(secCurrID, factor);
	}
	
	// ---------------------------------------------------------------

	/**
	 * @param pValue 
	 * @param secCurrID 
	 * @return the price of the given security/currency in base currencies
	 * @see SimplePriceTable#convertFromBaseCurrency(FixedPointNumber,
	 *      java.lang.String)
	 */
	public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertFromBaseCurrency: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		return table.convertFromBaseCurrency(pValue, secCurrID.getCode());
	}

	public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber pValue, final Currency curr) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null"); 
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr); 
		return convertFromBaseCurrency(pValue, secCurrID);
	}

	// ----------------------------

	public BigFraction convertFromBaseCurrencyRat(final BigFraction pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertFromBaseCurrencyRat: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		return table.convertFromBaseCurrencyRat(pValue, secCurrID.getCode());
	}
	
	public BigFraction convertFromBaseCurrencyRat(final BigFraction pValue, final Currency curr) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null"); 
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr);
		return convertFromBaseCurrencyRat(pValue, secCurrID);
	}

	// ----------------------------

	public FixedPointNumber convertToBaseCurrency(final FixedPointNumber pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null ) {
			throw new IllegalArgumentException("argument <pValue> is null"); 
		}
		
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertToBaseCurrency: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		return table.convertToBaseCurrency(pValue, secCurrID.getCode());
	}

	public FixedPointNumber convertToBaseCurrency(final FixedPointNumber pValue, final Currency curr) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null"); 
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr);
		return convertToBaseCurrency(pValue, secCurrID);
	}

	// ----------------------------

	public BigFraction convertToBaseCurrencyRat(final BigFraction pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set"); 
		}
		
		SimplePriceTable table = getByNamespace(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertToBaseCurrencyRat: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		return table.convertToBaseCurrencyRat(pValue, secCurrID.getCode());
	}
	
	public BigFraction convertToBaseCurrencyRat(final BigFraction pValue, final Currency curr) {
		if ( pValue == null ) {
			throw new IllegalArgumentException("argument <pValue> is null"); 
		}
		
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null"); 
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(curr); 
		return convertToBaseCurrencyRat(pValue, secCurrID);
	}

	// ---------------------------------------------------------------

	public List<KMMQualifSecCurrID.Type> getNameSpaces() {
		ArrayList<KMMQualifSecCurrID.Type> result = new ArrayList<KMMQualifSecCurrID.Type>(secCurrType2PrcTab.keySet());
		Collections.sort(result);
		return result;
	}

	/**
	 * @param type
	 * @return
	 */
	protected SimplePriceTable getByNamespace(KMMQualifSecCurrID.Type type) {
		return secCurrType2PrcTab.get(type);
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
		
		return table.getCodes();
	}

	// ---------------------------------------------------------------

	/**
	 * @see SimplePriceTable#clear()
	 */
	public void clear() {
		for ( KMMQualifSecCurrID.Type nameSpace : secCurrType2PrcTab.keySet() ) {
			secCurrType2PrcTab.get(nameSpace).clear();
		}

		secCurrType2PrcTab.clear();
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
