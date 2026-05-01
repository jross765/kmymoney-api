package org.kmymoney.api.pricedb;

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
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class ComplexPriceTable implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComplexPriceTable.class);

	private static final long serialVersionUID = -8548432873139328788L;

	// ---------------------------------------------------------------

	public interface ComplexPriceTableChangeListener {
	    @Deprecated
		void conversionFactorChanged(final String secCurrIDStr, final FixedPointNumber factor);
		void conversionFactorChanged(final String secCurrIDStr, final BigFraction factor);
	}

	// -----------------------------------------------------------

	private transient volatile List<ComplexPriceTableChangeListener> listeners = null;

	// -----------------------------------------------------------

	private Map<KMMQualifSecCurrID.Type, SimplePriceTable> secCurrType2PrcTab = null;

	// -----------------------------------------------------------

	public ComplexPriceTable() {
		secCurrType2PrcTab = new HashMap<KMMQualifSecCurrID.Type, SimplePriceTable>();

		addTabForType(KMMQualifSecCurrID.Type.CURRENCY);
		addTabForType(KMMQualifSecCurrID.Type.SECURITY);
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

	protected void firePriceTableChanged(final String secCurrIDStr, final FixedPointNumber factor) {
		if ( secCurrIDStr == null ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is null");
		}

		if ( secCurrIDStr.isBlank() ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is blank");
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
			LOGGER.error("firePriceTableChanged: Encountered factor <= 0 for currency '" + secCurrIDStr + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> is <= 0");
		}
		
		if ( listeners != null ) {
			for ( ComplexPriceTableChangeListener listener : listeners ) {
				listener.conversionFactorChanged(secCurrIDStr, factor);
			}
		}
	}

	protected void firePriceTableChanged(final String secCurrIDStr, final BigFraction factor) {
		if ( secCurrIDStr == null ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is null");
		}

		if ( secCurrIDStr.isBlank() ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is blank");
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
			LOGGER.error("firePriceTableChanged: Encountered factor <= 0 for currency '" + secCurrIDStr + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> is <= 0");
		}
		
		if ( listeners != null ) {
			for ( ComplexPriceTableChangeListener listener : listeners ) {
				listener.conversionFactorChanged(secCurrIDStr, factor);
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

	public void addTabForType(final KMMQualifSecCurrID.Type type) {
		if ( type == KMMQualifSecCurrID.Type.CURRENCY ) {
			SimpleCurrencyExchRateTable table = new SimpleCurrencyExchRateTable();
			addTabForType(type, table, false);
		} else {
			SimpleSecurityQuoteTable table = new SimpleSecurityQuoteTable();
			addTabForType(type, table, false);
		}
	}

	public void addTabForType(final KMMQualifSecCurrID.Type type, final SimplePriceTable table, boolean clear) {
		if ( table == null ) {
			throw new IllegalArgumentException("argument <table> is null");
		}

		if ( secCurrType2PrcTab == null ) {
			throw new IllegalStateException("Meta table is not set"); 
		}
		
		if ( secCurrType2PrcTab.keySet().contains(type) ) {
			return;
		}

		if ( clear ) {
			secCurrType2PrcTab.clear();
			LOGGER.debug("addTabForType: Cleared table for type " + type);
		}
		
		secCurrType2PrcTab.put(type, table);
		LOGGER.debug("addTabForType: Added new table for type " + type);
	}

	public List<KMMQualifSecCurrID.Type> getTabTypes() {
		if ( secCurrType2PrcTab == null ) {
			throw new IllegalStateException("Meta table is not set"); 
		}
		
		ArrayList<KMMQualifSecCurrID.Type> result = new ArrayList<KMMQualifSecCurrID.Type>(secCurrType2PrcTab.keySet());
		Collections.sort(result);
		return result;
	}

	protected SimplePriceTable getTabByType(final KMMQualifSecCurrID.Type type) {
		if ( secCurrType2PrcTab == null ) {
			throw new IllegalStateException("Meta table is not set"); 
		}
		
		return secCurrType2PrcTab.get(type);
	}

	// ---------------------------------------------------------------

	/**
	 * @param type 
	 * @param code 
	 * @return the factor to convert the price specified by the name-space-code-pair
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
    @Deprecated
	public FixedPointNumber getConversionFactor(final KMMQualifSecCurrID.Type type, final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.isBlank() ) {
			throw new IllegalArgumentException("argument <code> is blank");
		}

		if ( type == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMQualifCurrID currID = new KMMQualifCurrID(code);
			return getConversionFactor(currID);
		} else if ( type == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMQualifSecID secID = new KMMQualifSecID(code);
			return getConversionFactor(secID);
		}
		
		return null; // Compiler happy
	}

    @Deprecated
	public FixedPointNumber getConversionFactor(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("getConversionFactor: Cannot get simple conversion table for security/currency ID " + secCurrID);
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).getConversionFactor(currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).getConversionFactor(secID);
		}
		
		return null; // Compiler happy
	}

    @Deprecated
	public FixedPointNumber getConversionFactor(final Currency curr) {
		if ( curr == null ) {
			throw new IllegalArgumentException("argument <curr> is null");
		}

		KMMQualifCurrID currID = new KMMQualifCurrID(curr);
		return getConversionFactor(currID);
	}

	// ----------------------------

	public BigFraction getConversionFactorRat(final KMMQualifSecCurrID.Type type, final String code) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.isBlank() ) {
			throw new IllegalArgumentException("argument <code> is blank");
		}
		
		if ( type == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMQualifCurrID currID = new KMMQualifCurrID(code);
			return getConversionFactorRat(currID);
		} else {
			KMMQualifSecID secID = new KMMQualifSecID(code);
			return getConversionFactorRat(secID);
		}
	}

	public BigFraction getConversionFactorRat(final KMMQualifSecCurrID secCurrID) {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}

		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}

		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("getConversionFactorRat: Cannot get simple conversion table for security/currency ID " + secCurrID);
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).getConversionFactorRat(currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).getConversionFactorRat(secID);
		}
		
		return null; // Compiler happy
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
	 * @param type 
	 * @param code 
	 * @param factor 
	 *
	 * @see SimplePriceTable#setConversionFactor(java.lang.String, FixedPointNumber)
	 */
    @Deprecated
	public void setConversionFactor(final KMMQualifSecCurrID.Type type, final String code, final FixedPointNumber factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.isBlank() ) {
			throw new IllegalArgumentException("argument <code> is blank");
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(type, code);
		setConversionFactor(secCurrID, factor);
	}
	
	@Deprecated
	public void setConversionFactor(final String secCurrIDStr, final FixedPointNumber factor) {
		if ( secCurrIDStr == null ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is null");
		}

		if ( secCurrIDStr.isBlank() ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is blank");
		}

		KMMQualifSecCurrID secCurrID = KMMQualifSecCurrID.parse(secCurrIDStr);
		setConversionFactor(secCurrID, factor);
	}

    @Deprecated
	public void setConversionFactor(final KMMQualifSecCurrID secCurrID, final FixedPointNumber factor) {
		if ( secCurrID == null ) {
		    throw new IllegalArgumentException("argument <secCurrID> is null");
		}
	
		if ( ! secCurrID.isSet() ) {
		    throw new IllegalArgumentException("argument <secCurrID> is not set");
		}
	
		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			setConversionFactor(currID, factor);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			setConversionFactor(secID, factor);
		}

		firePriceTableChanged(secCurrID.toString(), factor);
	}
	
    @Deprecated
	public void setConversionFactor(final KMMSecID secID, final FixedPointNumber factor) {
		if ( secID == null ) {
		    throw new IllegalArgumentException("argument <secID> is null");
		}
	
		if ( ! secID.isSet() ) {
		    throw new IllegalArgumentException("argument <secID> is not set");
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
			LOGGER.error("setConversionFactor: Encountered factor <= 0 for security '" + secID + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> must be > 0");
		}

		SimplePriceTable table = getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		if ( table == null ) {
        	LOGGER.error("setConversionFactor: Cannot get simple conversion table for security/currency ID " + secID);
			return;
		}

		((SimpleSecurityQuoteTable) table).setConversionFactor(secID, factor);
		((SimpleSecurityQuoteTable) table).setConversionFactorRat(secID, factor.toBigFraction());

		firePriceTableChanged(secID.toString(), factor);
	}
	
	public void setConversionFactor(final KMMCurrID currID, final FixedPointNumber factor) {
		if ( currID == null ) {
		    throw new IllegalArgumentException("argument <currID> is null");
		}
	
		if ( ! currID.isSet() ) {
		    throw new IllegalArgumentException("argument <currID> is not set");
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
			LOGGER.error("setConversionFactor: Encountered factor <= 0 for currency '" + currID + "': " + factor);
			return;
			// throw new IllegalArgumentException("argument <factor> must be > 0");
		}

		SimplePriceTable table = getTabByType(KMMQualifSecCurrID.Type.CURRENCY);
		if ( table == null ) {
        	LOGGER.error("setConversionFactor: Cannot get simple conversion table for currency ID " + currID);
			return;
		}

		((SimpleCurrencyExchRateTable) table).setConversionFactor(currID, factor);
		((SimpleCurrencyExchRateTable) table).setConversionFactorRat(currID, factor.toBigFraction());

		firePriceTableChanged(currID.toString(), factor);
	}
	
    @Deprecated
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

	public void setConversionFactorRat(final KMMQualifSecCurrID.Type type, final String code,
			final BigFraction factor) {
		if ( code == null ) {
			throw new IllegalArgumentException("argument <code> is null");
		}

		if ( code.isBlank() ) {
			throw new IllegalArgumentException("argument <code> is blank");
		}
		
		KMMQualifSecCurrID secCurrID = new KMMQualifSecCurrID(type, code);
		setConversionFactorRat(secCurrID, factor);
	}

	@Deprecated
	public void setConversionFactorRat( final String secCurrIDStr, final BigFraction factor) {
		if ( secCurrIDStr == null ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is null");
		}

		if ( secCurrIDStr.isBlank() ) {
			throw new IllegalArgumentException("argument <secCurrIDStr> is blank");
		}
		
		KMMQualifSecCurrID secCurrID = KMMQualifSecCurrID.parse(secCurrIDStr);
		setConversionFactorRat(secCurrID, factor);
	}

	public void setConversionFactorRat(final KMMQualifSecCurrID secCurrID, final BigFraction factor) {
		if ( secCurrID == null ) {
		    throw new IllegalArgumentException("argument <secCurrID> is null");
		}
	
		if ( ! secCurrID.isSet() ) {
		    throw new IllegalArgumentException("argument <secCurrID> is not set");
		}
	
		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			setConversionFactorRat(currID, factor);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			setConversionFactorRat(secID, factor);
		}

		firePriceTableChanged(secCurrID.toString(), factor);
	}
	
	public void setConversionFactorRat(final KMMSecID secID, final BigFraction factor) {
		if ( secID == null ) {
		    throw new IllegalArgumentException("argument <secID> is null");
		}
	
		if ( ! secID.isSet() ) {
		    throw new IllegalArgumentException("argument <secID> is not set");
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

		SimplePriceTable table = getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		if ( table == null ) {
        	LOGGER.error("setConversionFactorRat: Cannot get simple conversion table for security ID " + secID);
			return;
		}

		((SimpleSecurityQuoteTable) table).setConversionFactor(secID, FixedPointNumber.of(factor));
		((SimpleSecurityQuoteTable) table).setConversionFactorRat(secID, factor);

		firePriceTableChanged(secID.toString(), factor);
	}

	public void setConversionFactorRat(final KMMCurrID currID, final BigFraction factor) {
		if ( currID == null ) {
		    throw new IllegalArgumentException("argument <secID> is null");
		}
	
		if ( ! currID.isSet() ) {
		    throw new IllegalArgumentException("argument <secID> is not set");
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

		SimplePriceTable table = getTabByType(KMMQualifSecCurrID.Type.SECURITY);
		if ( table == null ) {
        	LOGGER.error("setConversionFactorRat: Cannot get simple conversion table for security ID " + currID);
			return;
		}

		((SimpleCurrencyExchRateTable) table).setConversionFactor(currID, FixedPointNumber.of(factor));
		((SimpleCurrencyExchRateTable) table).setConversionFactorRat(currID, factor);

		firePriceTableChanged(currID.toString(), factor);
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
    @Deprecated
	public FixedPointNumber convertFromBaseCurrency(final FixedPointNumber pValue, final KMMQualifSecCurrID secCurrID) {
		if ( pValue == null )
			throw new IllegalArgumentException("argument <pValue> is null");

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertFromBaseCurrency: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).convertFromBaseCurrency(pValue, currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).convertFromBaseCurrency(pValue, secID);
		}
		
		return null; // Compiler happy
	}

    @Deprecated
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
		
		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertFromBaseCurrencyRat: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).convertFromBaseCurrencyRat(pValue, currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).convertFromBaseCurrencyRat(pValue, secID);
		}
		
		return null; // Compiler happy
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

    @Deprecated
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
		
		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertToBaseCurrency: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).convertToBaseCurrency(pValue, currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).convertToBaseCurrency(pValue, secID);
		}
		
		return null; // Compiler happy
	}

    @Deprecated
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
		if ( pValue == null ) {
			throw new IllegalArgumentException("argument <pValue> is null");
		}

		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null"); 
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set"); 
		}
		
		SimplePriceTable table = getTabByType(secCurrID.getType());
		if ( table == null ) {
        	LOGGER.error("convertToBaseCurrencyRat: Cannot get simple conversion table for value = " + pValue + " and code = '" + secCurrID + "'");
			return null;
		}

		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			KMMCurrID currID = new KMMCurrID(secCurrID.getCode());
			return ((SimpleCurrencyExchRateTable) table).convertToBaseCurrencyRat(pValue, currID);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			KMMSecID secID = new KMMSecID(secCurrID.getCode());
			return ((SimpleSecurityQuoteTable) table).convertToBaseCurrencyRat(pValue, secID);
		}
		
		return null; // Compiler happy
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

	/**
	 * @param tyoe
	 * @return 
	 */
	public List<String> getCodes(final KMMQualifSecCurrID.Type tyoe) {
		SimplePriceTable table = getTabByType(tyoe);
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
		if ( secCurrType2PrcTab == null ) {
			throw new IllegalStateException("Meta table is not set"); 
		}
		
		for ( KMMQualifSecCurrID.Type type : secCurrType2PrcTab.keySet() ) {
			secCurrType2PrcTab.get(type).clear();
		}

		secCurrType2PrcTab.clear();
	}

	// ---------------------------------------------------------------

	@Override
	public String toString() {
		String result = "ComplexPriceTable [\n";

		for ( KMMQualifSecCurrID.Type type : getTabTypes() ) {
			if ( type != KMMQualifSecCurrID.Type.UNSET ) {
				result += "=======================================\n";
				result += "Name space: " + type + "\n";
				result += "=======================================\n";
				result += getTabByType(type).toString() + "\n";
			}
		}

		result += "]";

		return result;
	}

}
