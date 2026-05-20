package org.kmymoney.api.read.aux;

import java.time.LocalDate;

import org.apache.commons.numbers.fraction.BigFraction;

public interface KMMBudgetPeriod {
	
	KMMBudgetAccount getParent();

	// ---------------------------------------------------------------

    LocalDate getStart();

    String    getStartStr();

    // ----------------------------
    
    BigFraction getAmount();
    
    String      getAmountFormatted();

}
