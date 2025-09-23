package org.kmymoney.api.write.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import junit.framework.JUnit4TestAdapter;

public class TestFilePayeeManager {

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImplTestHelper kmmInFile = null;

	private org.kmymoney.api.write.impl.hlp.FilePayeeManager mgr = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestFilePayeeManager.class);
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
			kmmInFile = new KMyMoneyWritableFileImplTestHelper(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	
	@Test
	public void test01() throws Exception {
		mgr = kmmInFile.getPayeeManager();
		
		assertEquals(ConstTest.Stats.NOF_PYE, mgr.getNofEntriesPayeeMap());
		assertEquals(ConstTest.Stats.NOF_PYE, mgr.getPayees().size());
	}

	@Test
	public void test02() throws Exception {
		mgr = kmmInFile.getPayeeManager();
		
		Collection<KMyMoneyPayee> pyeColl = mgr.getPayees();
		KMMPyeID pyeID = new KMMPyeID("P000001");
		KMyMoneyPayee pye = mgr.getPayeeByID(pyeID);
		assertTrue(pyeColl.contains(pye));
	}

}
