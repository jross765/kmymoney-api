package org.kmymoney.api.read.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;

import junit.framework.JUnit4TestAdapter;

public class TestFileCurrencyManager {

	// ---------------------------------------------------------------

	private KMyMoneyFileImplTestHelper kmmFile = null;

	private FileCurrencyManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFileCurrencyManager.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmInFileStream = null;
		try {
			kmmInFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME_IN);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImplTestHelper(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	
	@Test
	public void test01() throws Exception {
		mgr = kmmFile.getCurrencyManager();
		
		assertEquals(ConstTest.Stats.NOF_CURR, mgr.getNofEntriesCurrencyMap());
		assertEquals(ConstTest.Stats.NOF_CURR, mgr.getCurrencies().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmFile.getCurrencyManager();
		
		Collection<KMyMoneyCurrency> currColl = mgr.getCurrencies();
		KMMQualifCurrID currID = new KMMQualifCurrID("EUR");
		KMyMoneyCurrency curr = mgr.getCurrencyByQualifID(currID);
		assertTrue(currColl.contains(curr));
	}

}
