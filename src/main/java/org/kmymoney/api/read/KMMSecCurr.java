package org.kmymoney.api.read;

import java.math.BigInteger;

public class KMMSecCurr {

    // Cf. https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    public static enum Type {
    	
    	// ::MAGIC
        STOCK       ( 0 ),
        MUTUAL_FUND ( 1 ),
        BOND        ( 2 ),
        CURRENCY    ( 3 ),
        NONE        ( 4 );
    	
    	// ---

    	final public static int UNSET = 999;
    	
    	private BigInteger code = BigInteger.valueOf(UNSET);

    	// ---

    	Type(BigInteger code) {
    	    this.code = code;
    	}

    	Type(int code) {
    	    this.code = BigInteger.valueOf(code);
    	}

    	// ---

    	public BigInteger getCode() {
    	    return code;
    	}

    	// no typo!
    	public static Type valueOff(BigInteger code) {
    	    for ( Type type : values() ) {
    	    	if ( type.getCode().equals(code) ) {
    	    		return type;
    			}
    	    }

    	    return null;
    	}
    	
    	// no typo!
    	public static Type valueOff(int code) {
    	    return valueOff(BigInteger.valueOf(code));
    	}
    }
    
    // ----------------------------

    // ::TODO: Could not find explicit mapping to integers in KMyMoney's source code yet.
    // Only could validate indirectly by playing around with a test file.
    // *** Values only partially checked! ***
    // Cf.: 
    //  - xxx
    //  - https://lxr.kde.org/ident?_i=RoundingMethod
    //  - https://lxr.kde.org/ident?_i=AlkValue
    //  - https://lxr.kde.org/source/office/kmymoney/kmymoney/mymoney/mymoneysecurity.cpp#0246
    public static enum RoundingMethod {

    	// ::MAGIC
    	// ::CHECK These values are only partially checked (as indicated)
    	// The rest is just guesswork!
        NEVER     ( 0 ),
        FLOOR     ( 1 ), // checked
        CEIL      ( 2 ), // checked
        TRUNCATE  ( 3 ), // checked
        PROMOTE   ( 4 ),
        HALF_DOWN ( 5 ),
        HALF_UP   ( 6 ),
        ROUND     ( 7 ), // checked
        
        UNKNOWN   (99 );
    	
    	// ---

    	final public static int UNSET = 999;
    	
    	private BigInteger code = BigInteger.valueOf(UNSET);

    	// ---

    	RoundingMethod(BigInteger code) {
    	    this.code = code;
    	}

    	RoundingMethod(int code) {
    	    this.code = BigInteger.valueOf(code);
    	}

    	// ---

    	public BigInteger getCode() {
    	    return code;
    	}

    	// no typo!
    	public static RoundingMethod valueOff(BigInteger code) {
    	    for ( RoundingMethod type : values() ) {
    	    	if ( type.getCode().equals(code) ) {
    	    		return type;
    			}
    	    }

    	    return null;
    	}
    	
    	// no typo!
    	public static RoundingMethod valueOff(int code) {
    	    return valueOff(BigInteger.valueOf(code));
    	}
    }

}
