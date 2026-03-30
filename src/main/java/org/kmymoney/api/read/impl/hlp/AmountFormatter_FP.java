package org.kmymoney.api.read.impl.hlp;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public class AmountFormatter_FP
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AmountFormatter_FP.class);
	
	// ---------------------------------------------------------------

	public static String formatAmount(KMyMoneyFile kmmFile, 
									  FixedPointNumber amt, KMMQualifSecCurrID secCurrID) {
		if ( amt == null ) {
			throw new IllegalArgumentException("argument <amt> is null");
		}
		
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}
		
		Locale lcl = Locale.getDefault();
		return formatAmount(kmmFile, 
							amt, secCurrID, lcl);
	}
	
	public static String formatAmount(KMyMoneyFile kmmFile,
									  FixedPointNumber amt, KMMQualifSecCurrID secCurrID, Locale lcl) {
		if ( amt == null ) {
			throw new IllegalArgumentException("argument <acct> is null");
		}
		
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("argument <secCurrID> is null");
		}
		
		if ( ! secCurrID.isSet() ) {
			throw new IllegalArgumentException("argument <secCurrID> is not set");
		}
		
		if ( lcl == null ) {
			throw new IllegalArgumentException("argument <lcl> is null");
		}
		
		NumberFormat nf = getCmdtyFormat(secCurrID, lcl);
    	if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			return nf.format(amt.getBigDecimal());
    	} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
    		KMMSecID secID = new KMMSecID(secCurrID.getCode());
			KMyMoneySecurity sec = kmmFile.getSecurityByID(secID);
			String secSymb = "(sec-symbol)";
			if ( sec.getSymbol() != null ) {
				secSymb = sec.getSymbol();
			} else if ( sec.getCode() != null ) {
				secSymb = sec.getCode();
			} else {
				secSymb = sec.toString();
			}
			nf = NumberFormat.getNumberInstance(lcl);
			return ( nf.format(amt.getBigDecimal()) + " " + secSymb );
		}
    	
    	return "ERROR"; // Compiler happy
	}
	
	// ----------------------------

	public static NumberFormat getCurrencyFormat(KMMQualifSecCurrID secCurrID) {
		return getCmdtyFormat(secCurrID, Locale.getDefault());
	}
	
	public static NumberFormat getCmdtyFormat(KMMQualifSecCurrID secCurrID, Locale lcl) {
		NumberFormat fmt = null;
		
		if ( secCurrID.getType() == KMMQualifSecCurrID.Type.CURRENCY ) {
			fmt = NumberFormat.getCurrencyInstance(lcl);
			Currency curr = Currency.getInstance( secCurrID.getCode() );
			fmt.setCurrency(curr);
		} else if ( secCurrID.getType() == KMMQualifSecCurrID.Type.SECURITY ) {
			fmt = NumberFormat.getNumberInstance(lcl);
		}

		return fmt;
	}

}
