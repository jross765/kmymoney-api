package org.kmymoney.api.read.aux;

import java.time.LocalDate;
import java.util.Locale;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.hlp.KMyMoneyObject;

public interface KMMBudgetPeriod extends KMyMoneyObject {
	
	KMMBudgetAccount getParent();

	// ---------------------------------------------------------------

    LocalDate getStart();

    String    getStartStr();

    // ----------------------------
    
    BigFraction getAmount();
    
    String      getAmountFormatted();

    String      getAmountFormatted(Locale lcl);

}
