package org.kmymoney.api.read.aux;

import java.util.List;

import org.kmymoney.api.read.KMyMoneyBudget;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

public interface KMMBudgetAccount extends KMyMoneyObject {
	
	public enum Level {
		// ::MAGIC
		MONTHLY        ( "monthly" ),
		YEARLY         ( "yearly" ),
		MONTH_BY_MONTH ( "monthbymonth" );
		
		// ---

		private String code = "UNSET";

		// ---

		Level(String code) {
			if ( code == null )
				throw new IllegalArgumentException("argument <code> is null");
			
			if ( code.isBlank() )
				throw new IllegalArgumentException("argument <code> is blank");
			
			this.code = code.trim();
		}
		
		// ---

		public String getCode() {
			return code;
		}

		// No typo!
		public static Level valueOff(String code) {
			if ( code == null ) {
				throw new IllegalStateException( "argument <code> is null" );
			}

			if ( code.isBlank() ) {
				throw new IllegalStateException( "argument <code> is blank" );
			}

			for ( Level val : values() ) {
				if ( val.getCode().equals( code.trim() ) ) {
					return val;
				}
			}

			return null;
		}
	}
	
	// ---------------------------------------------------------------
	
	KMyMoneyBudget getParent();

	// ---------------------------------------------------------------

    KMMAcctID getAcctID();
    
    Level getLevel();
    
    boolean includesSubAccounts();

	// ---------------------------------------------------------------

    List<KMMBudgetPeriod> getPeriods();

}
