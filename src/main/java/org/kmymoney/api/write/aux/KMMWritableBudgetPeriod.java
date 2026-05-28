package org.kmymoney.api.write.aux;

import java.time.LocalDate;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.aux.KMMBudgetPeriod;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;

public interface KMMWritableBudgetPeriod extends KMMBudgetPeriod,
                                                 KMyMoneyWritableObject
{
	
    void setStart(LocalDate date);

    void setAmount(BigFraction amt);

}
