package org.kmymoney.api.write.impl.hlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;

public class KMyMoneyWritableFileImplTestHelper extends KMyMoneyWritableFileImpl
{
	// ---------------------------------------------------------------

	public KMyMoneyWritableFileImplTestHelper(final File pFile) throws IOException {
		super(pFile);
	}
	
	public KMyMoneyWritableFileImplTestHelper(final InputStream is) throws IOException {
		super(is);
	}

	// ---------------------------------------------------------------
	// For test purposes only

	public org.kmymoney.api.write.impl.hlp.FilePayeeManager getPayeeManager() {
		return (org.kmymoney.api.write.impl.hlp.FilePayeeManager) pyeMgr;
	}

	public org.kmymoney.api.write.impl.hlp.FileSecurityManager getSecurityManager() {
		return (org.kmymoney.api.write.impl.hlp.FileSecurityManager) secMgr;
	}

	public org.kmymoney.api.write.impl.hlp.FileCurrencyManager getCurrencyManager() {
		return (org.kmymoney.api.write.impl.hlp.FileCurrencyManager) currMgr;
	}

	public org.kmymoney.api.write.impl.hlp.FilePriceManager getPriceManager() {
		return (org.kmymoney.api.write.impl.hlp.FilePriceManager) prcMgr;
	}

}
