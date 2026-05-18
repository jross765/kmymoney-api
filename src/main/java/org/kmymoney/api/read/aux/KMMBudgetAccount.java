package org.kmymoney.api.read.aux;

import java.util.List;

import org.kmymoney.base.basetypes.simple.KMMAcctID;

public interface KMMBudgetAccount {
	
	public enum Level {
		MONTHLY,
		YEARLY,
		MONTH_BY_MONTH
	}
	
	// ---------------------------------------------------------------

    KMMAcctID getAcctID();
    
    Level getLevel();
    
    boolean includesSubAccounts();

	// ---------------------------------------------------------------

    List<KMMBudgetPeriod> getPeriods();

}
