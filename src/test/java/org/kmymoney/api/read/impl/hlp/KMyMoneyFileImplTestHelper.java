package org.kmymoney.api.read.impl.hlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.kmymoney.api.read.impl.KMyMoneyFileImpl;

public class KMyMoneyFileImplTestHelper extends KMyMoneyFileImpl
{
	// ---------------------------------------------------------------

	public KMyMoneyFileImplTestHelper(final File pFile) throws IOException {
		super(pFile);
	}
	
	public KMyMoneyFileImplTestHelper(final InputStream is) throws IOException {
		super(is);
	}

	// ---------------------------------------------------------------
	// The methods in this section are For test purposes only

	public FilePayeeManager getPayeeManager() {
		return pyeMgr;
	}

	public FileSecurityManager getSecurityManager() {
		return secMgr;
	}

	public FileCurrencyManager getCurrencyManager() {
		return currMgr;
	}

	public FilePriceManager getPriceManager() {
		return prcMgr;
	}

}
