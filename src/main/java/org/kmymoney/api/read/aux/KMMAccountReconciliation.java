package org.kmymoney.api.read.aux;

import java.time.LocalDate;

import org.apache.commons.numbers.fraction.BigFraction;
import org.kmymoney.api.read.hlp.KMyMoneyObject;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface KMMAccountReconciliation extends KMyMoneyObject {

	/**
	 * 
	 * @return
	 */
	public LocalDate getDate();

	/**
	 * 
	 * @return
	 */
	@Deprecated
	public FixedPointNumber getValue();

	public BigFraction      getValueRat();

}
