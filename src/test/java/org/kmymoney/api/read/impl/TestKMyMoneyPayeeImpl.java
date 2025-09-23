package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.base.basetypes.simple.KMMPyeID;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.aux.KMMAddress;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyPayeeImpl {
	public static final KMMPyeID PYE_1_ID = new KMMPyeID("P000002"); // Gehalt
	public static final KMMPyeID PYE_2_ID = new KMMPyeID("P000003"); // Geldautomat
	public static final KMMPyeID PYE_3_ID = new KMMPyeID("P000005"); // Schnorzelmoeller

	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMyMoneyPayee pye = null;

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMyMoneyPayeeImpl.class);
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
		pye = kmmFile.getPayeeByID(PYE_1_ID);
		assertNotEquals(null, pye);

		assertEquals(PYE_1_ID, pye.getID());
		assertEquals("Gehalt", pye.getName());
		
		assertEquals(false, pye.hasTransactions());
	}

	@Test
	public void test01_2() throws Exception {
		pye = kmmFile.getPayeeByID(PYE_2_ID);
		assertNotEquals(null, pye);

		assertEquals(PYE_2_ID, pye.getID());
		assertEquals("Geldautomat", pye.getName());
		
		assertEquals(true, pye.hasTransactions());
		assertEquals(1, pye.getTransactions().size());
	}

	@Test
	public void test01_3() throws Exception {
		pye = kmmFile.getPayeeByID(PYE_3_ID);
		assertNotEquals(null, pye);

		assertEquals(PYE_3_ID, pye.getID());
		assertEquals("Fürchtegott Schnorzelmöller", pye.getName());
		assertEquals(null, pye.getDefaultAccountID());
		assertEquals("fuerchtegott.schnorzelmoeller@prater.at", pye.getEmail());
		assertEquals("", pye.getReference()); // sic, not null
		assertEquals("Pezi-Bär von der Urania kennt ihn gut", pye.getNotes());

		KMMAddress addr = pye.getAddress();
		assertNotEquals(null, addr);
		// Detailed test of the address: Cf. TestKMMAddressImpl
		
		assertEquals(false, pye.hasTransactions());
	}
}
