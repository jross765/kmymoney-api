package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.aux.KMMAddress;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyInstitutionImpl {
	public static final KMMInstID INST_1_ID = new KMMInstID("I000001");
	public static final KMMInstID INST_2_ID = new KMMInstID("I000002");

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyInstitution inst = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyInstitutionImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.KMM_FILENAME);
		// System.err.println("KMyMoney test file resource: '" + kmmFileURL + "'");
		InputStream kmmFileStream = null;
		try {
			kmmFileStream = classLoader.getResourceAsStream(ConstTest.KMM_FILENAME);
		} catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			kmmFile = new KMyMoneyFileImpl(kmmFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		inst = kmmFile.getInstitutionByID(INST_1_ID);

		assertEquals(INST_1_ID, inst.getID());
		assertEquals("RaiBa", inst.getName());
		assertEquals("", inst.getSortCode());

		assertNotEquals(null, inst.getAddress()); // sic
		KMMAddress addr = inst.getAddress();
		assertEquals("", addr.getCity());
		assertEquals("", addr.getStreet());

		// Does not work after conversion V. 5.1.3 -> V. 5.2:
//		assertEquals("", inst.getBIC());
//		assertEquals("", inst.getURL());
	}

	@Test
	public void test01_2() throws Exception {
		inst = kmmFile.getInstitutionByID(INST_2_ID);

		assertEquals(INST_2_ID, inst.getID());
		assertEquals("My Gibberish Bank", inst.getName());
		assertEquals("666", inst.getSortCode());
		
		assertNotEquals(null, inst.getAddress());
		KMMAddress addr = inst.getAddress();
		assertEquals("Gibbon City", addr.getCity());
		assertEquals("Gibby St", addr.getStreet());
		
		assertEquals("TMBQBHDGXXX", inst.getBIC());
		assertEquals("gibberish.com", inst.getURL());
	}

}
