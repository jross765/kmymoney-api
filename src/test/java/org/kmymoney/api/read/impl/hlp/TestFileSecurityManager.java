package org.kmymoney.api.read.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.base.basetypes.simple.KMMSecID;

import junit.framework.JUnit4TestAdapter;

public class TestFileSecurityManager {

	// ---------------------------------------------------------------

	private KMyMoneyFileImplTestHelper kmmFile = null;

	private FileSecurityManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFileSecurityManager.class);
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
		mgr = kmmFile.getSecurityManager();
		
		assertEquals(ConstTest.Stats.NOF_SEC, mgr.getNofEntriesSecurityMap());
		assertEquals(ConstTest.Stats.NOF_SEC, mgr.getSecurities().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmFile.getSecurityManager();
		
		Collection<KMyMoneySecurity> secColl = mgr.getSecurities();
		KMMSecID secID = new KMMSecID("E000001");
		KMyMoneySecurity sec = mgr.getSecurityByID(secID);
		assertTrue(secColl.contains(sec));
	}

}
