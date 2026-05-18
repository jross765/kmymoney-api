package org.kmymoney.api.read.aux;

import java.time.LocalDate;

import org.apache.commons.numbers.fraction.BigFraction;

public interface KMMBudgetPeriod {
	
    BigFraction getAmount();
    
    LocalDate getStart();

    String    getStartStr();

}
