package org.kmymoney.api.read.impl;

import org.kmymoney.api.read.KMMSecCurr;
import org.kmymoney.api.read.UnknownSecurityTypeException;

public class KMMSecCurrImpl {

    // Internal values cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    // 
    // CAUTION: Do *not* change them!
    static final int TYPE_STOCK       = 0;
    static final int TYPE_MUTUAL_FUND = 1;
    static final int TYPE_BOND        = 2;
    static final int TYPE_CURRENCY    = 3;
    static final int TYPE_NONE        = 4;
    

    // ---------------------------------------------------------------

    public static KMMSecCurr.Type getType(int typeVal) {
	
	if ( typeVal == KMMSecCurrImpl.TYPE_STOCK )
	    return KMMSecCurr.Type.STOCK;
	else if ( typeVal == KMMSecCurrImpl.TYPE_MUTUAL_FUND )
	    return KMMSecCurr.Type.MUTUAL_FUND;
	else if ( typeVal == KMMSecCurrImpl.TYPE_BOND )
	    return KMMSecCurr.Type.BOND;
	else if ( typeVal == KMMSecCurrImpl.TYPE_CURRENCY )
	    return KMMSecCurr.Type.CURRENCY;
	else if ( typeVal == KMMSecCurrImpl.TYPE_NONE )
	    return KMMSecCurr.Type.NONE;
	else
	    throw new UnknownSecurityTypeException();
    }

}
