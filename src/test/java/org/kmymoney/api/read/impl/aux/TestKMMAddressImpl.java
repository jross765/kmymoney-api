package org.kmymoney.api.read.impl.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyInstitution;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyInstitutionImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyPayeeImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import junit.framework.JUnit4TestAdapter;

public class TestKMMAddressImpl {
	
	// -----------------------------------------------------------------

//	public static final KMMPyeID PYE_1_ID = TestKMyMoneyPayeeImpl.PYE_1_ID;
	public static final KMMPyeID PYE_2_ID = TestKMyMoneyPayeeImpl.PYE_2_ID;
	public static final KMMPyeID PYE_3_ID = TestKMyMoneyPayeeImpl.PYE_3_ID;
	
	public static final KMMInstID INST_1_ID = TestKMyMoneyInstitutionImpl.INST_1_ID;
	public static final KMMInstID INST_2_ID = TestKMyMoneyInstitutionImpl.INST_2_ID;
	
	// -----------------------------------------------------------------

	private KMyMoneyFile kmmFile = null;
	private KMMAddress addr = null;

	private KMyMoneyPayee pye3 = null;
	private KMyMoneyInstitution inst2 = null;
	
	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMMAddressImpl.class);
	}

	@Before
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL kmmFileURL = classLoader.getResource(Const.GCSH_FILENAME);
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
		
		// ---
		
		pye3 = kmmFile.getPayeeByID(PYE_3_ID);
		inst2 = kmmFile.getInstitutionByID(INST_2_ID);
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
		addr = pye3.getAddress();
		assertNotEquals(null, addr); 
		
		assertEquals("Krailbacher Gasse 123 a\n" + 
				     "Postfach ABC\n" + 
				     "Kennwort Kasperlpost", addr.getStreet());
		assertEquals("1136", addr.getZip());
		assertEquals("1136", addr.getZipCode());
		assertEquals("1136", addr.getPostCode());
		assertEquals("Wien", addr.getCity());
		assertEquals("Österreich", addr.getState());
		assertEquals("Österreich", addr.getCounty());
		assertEquals("Österreich", addr.getCountry());
		
		assertEquals("+43 - 12 - 277278279", addr.getTelephone());
	}

	@Test
	public void test02() throws Exception {
		addr = inst2.getAddress();
		assertNotEquals(null, addr);
		
		assertEquals("Gibby St", addr.getStreet());
		assertEquals("E23Q452", addr.getZip());
		assertEquals("E23Q452", addr.getZipCode());
		assertEquals("E23Q452", addr.getPostCode());
		assertEquals("Gibbon City", addr.getCity());
		assertEquals("", addr.getState());
		assertEquals("", addr.getCounty());
		assertEquals("", addr.getCountry());
		
		assertEquals("+1 - 44 - 1234", addr.getTelephone());
	}

}
