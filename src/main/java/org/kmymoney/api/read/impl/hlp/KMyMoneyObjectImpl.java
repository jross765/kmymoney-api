package org.kmymoney.api.read.impl.hlp;

import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper-Class used to implement functions all KMyMoney objects support.
 */
public class KMyMoneyObjectImpl implements KMyMoneyObject {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyObjectImpl.class);

	// -----------------------------------------------------------------

	protected final KMyMoneyFile kmmFile;

	// -----------------------------------------------------------------

	public KMyMoneyObjectImpl(final KMyMoneyFile kmmFile) {
		super();

		this.kmmFile = kmmFile;
	}

	// -----------------------------------------------------------------

	@Override
	public KMyMoneyFile getKMyMoneyFile() {
		return kmmFile;
	}

	// -----------------------------------------------------------------

	@Override
	public String toString() {
		return "KMyMoneyObjectImpl@" + hashCode();
	}

}
