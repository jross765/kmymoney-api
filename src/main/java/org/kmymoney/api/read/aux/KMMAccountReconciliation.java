package org.kmymoney.api.read.aux;

import java.time.LocalDate;

import xyz.schnorxoborx.base.numbers.FixedPointNumber;

public interface KMMAccountReconciliation {

	/**
	 * 
	 * @return
	 */
	public LocalDate getDate();

	/**
	 * 
	 * @return
	 */
	public FixedPointNumber getValue();

}
