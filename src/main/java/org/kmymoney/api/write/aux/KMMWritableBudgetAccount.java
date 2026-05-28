package org.kmymoney.api.write.aux;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.aux.KMMBudgetAccount;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.base.basetypes.simple.KMMAcctID;

public interface KMMWritableBudgetAccount extends KMMBudgetAccount,
                                                  KMyMoneyWritableObject
{
	
    void setAcctID(KMMAcctID acctID);
    
    void setLevel(Level lvl);
    
    void setIncludesSubAccounts(boolean val);

	// ---------------------------------------------------------------
    
    void clearPeriods();
    
    void removePeriod(KMMBudgetPeriod bdgtPrd);

    KMMWritableBudgetPeriod createWritablePeriod(LocalDate startDate, BigFraction amt);
    
	List<KMMWritableBudgetPeriod> getWritablePeriods();

}
