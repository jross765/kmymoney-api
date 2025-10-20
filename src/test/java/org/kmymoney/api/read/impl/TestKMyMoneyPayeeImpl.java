package org.kmymoney.api.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyPayeeImpl {
	public static final KMMPyeID PYE_1_ID = new KMMPyeID("P000002"); // Gehalt
	public static final KMMPyeID PYE_2_ID = new KMMPyeID("P000003"); // Geldautomat
	public static final KMMPyeID PYE_3_ID = new KMMPyeID("P000005"); // Schnorzelmoeller
	public static final KMMPyeID PYE_4_ID = new KMMPyeID("P000009"); // Hubers Laden

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
		assertEquals("", pye.getEmail());
		assertEquals("", pye.getReference());
		assertEquals("asdf", pye.getIDPattern());
		assertEquals("sdfg", pye.getURLTemplate());
		assertEquals(false, pye.getMatchingEnabled());
		
		assertEquals(false, pye.hasTransactions());
	}

	@Test
	public void test01_2() throws Exception {
		pye = kmmFile.getPayeeByID(PYE_2_ID);
		assertNotEquals(null, pye);

		assertEquals(PYE_2_ID, pye.getID());
		assertEquals("Geldautomat", pye.getName());
		assertEquals("", pye.getEmail());
		assertEquals("", pye.getReference());
		assertEquals(null, pye.getIDPattern());
		assertEquals(null, pye.getURLTemplate());
		assertEquals(false, pye.getMatchingEnabled());
		
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
		assertEquals(null, pye.getIDPattern());
		assertEquals(null, pye.getURLTemplate());
		assertEquals("Pezi-Bär von der Urania kennt ihn gut", pye.getNotes());
		assertEquals(false, pye.getMatchingEnabled());

		KMMAddress addr = pye.getAddress();
		assertNotEquals(null, addr);
		// Detailed test of the address: Cf. TestKMMAddressImpl
		
		assertEquals(false, pye.hasTransactions());
	}
	
	@Test
	public void test01_4() throws Exception {
		pye = kmmFile.getPayeeByID(PYE_4_ID);
		assertNotEquals(null, pye);

		assertEquals(PYE_4_ID, pye.getID());
		assertEquals("Hubers Laden", pye.getName());
		assertEquals("", pye.getEmail());
		assertEquals("", pye.getReference());
		assertEquals(null, pye.getIDPattern());
		assertEquals(null, pye.getURLTemplate());
 		assertEquals(true, pye.getMatchingEnabled());
		assertEquals(true, pye.getMatchIgnoreCase());
		assertEquals(true, pye.getUsingMatchKey());
		assertEquals(2, pye.getMatchKeys().size());
		assertEquals("Läden in der Gegend", pye.getMatchKeys().get(0));
		assertEquals("Kiosk Sesamstrasse", pye.getMatchKeys().get(1));

		assertEquals(false, pye.hasTransactions());
	}

}
