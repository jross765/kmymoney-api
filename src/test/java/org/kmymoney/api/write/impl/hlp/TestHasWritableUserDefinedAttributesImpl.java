package org.kmymoney.api.write.impl.hlp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.KMyMoneyTransaction;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.hlp.KVPListDoesNotContainKeyException;
import org.kmymoney.api.read.impl.hlp.TestHasUserDefinedAttributesImpl;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.api.write.KMyMoneyWritableSecurity;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMAcctID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.basetypes.simple.KMMTrxID;

import junit.framework.JUnit4TestAdapter;

public class TestHasWritableUserDefinedAttributesImpl {
	public static final KMMAcctID ACCT_1_ID = TestHasUserDefinedAttributesImpl.ACCT_1_ID;
	public static final KMMAcctID ACCT_2_ID = TestHasUserDefinedAttributesImpl.ACCT_2_ID;
	public static final KMMComplAcctID ACCT_3_ID = TestHasUserDefinedAttributesImpl.ACCT_3_ID;

	public static final KMMTrxID TRX_1_ID = TestHasUserDefinedAttributesImpl.TRX_1_ID;

	public static final KMMSecID SEC_2_ID = TestHasUserDefinedAttributesImpl.SEC_2_ID;
	public static final KMMSecID SEC_3_ID = TestHasUserDefinedAttributesImpl.SEC_3_ID;

	// ---------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestHasWritableUserDefinedAttributesImpl.class);
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
			kmmInFile = new KMyMoneyWritableFileImpl(kmmInFileStream);
		} catch (Exception exc) {
			System.err.println("Cannot parse KMyMoney in-file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------
	// PART 1: Read existing objects as modifiable ones
	// (and see whether they are fully symmetrical to their read-only
	// counterparts)
	// -----------------------------------------------------------------
	// Cf. TestHasUserDefinedAttributesImpl.test_xyz
	//
	// Check whether the user attributes returned by
	// HasWritableUserDefinedAttributesImpl.getXYZ() are actually
	// complete (as complete as returned be HasUserDefinedAttributesImpl.getXYZ().
	// -----------------------------------------------------------------

	// -----------------------------------------------------------------
	// Account
	// -----------------------------------------------------------------

	// No kvps
	@Test
	public void test_01_acct_01() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(null, acct.getUserDefinedAttributeKeys());
	}

	// One kvp
	@Test
	public void test_01_acct_02() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertNotEquals(null, acct.getUserDefinedAttributeKeys());
		assertEquals(1, acct.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_ACCT_IBAN, acct.getUserDefinedAttributeKeys().get(0));
		assertEquals("DE01 1234 2345 3456 4567 99", acct.getUserDefinedAttribute(ConstTest.KVP_KEY_ACCT_IBAN));
	}

	// -----------------------------------------------------------------
	// Transaction
	// -----------------------------------------------------------------

	// No kvps
	@Test
	public void test_01_trx_01() throws Exception {
		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(null, trx.getUserDefinedAttributeKeys());
	}

	//    // One kvp
	//    @Test
	//    public void test_01_trx_02() throws Exception {
	//    	KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_2_ID);
	//    	assertNotEquals(null, trx);
	//    	
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys());
	//    	assertEquals(1, trx.getUserDefinedAttributeKeys().size());
	//    	assertEquals(ConstTest.SLOT_KEY_TRX_DATE_POSTED, trx.getUserDefinedAttributeKeys().get(0));
	//    	assertEquals("2023-07-01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED));
	//    }
	//    
	//    // Several kvps
	//    @Test
	//    public void test_01_trx_03() throws Exception {
	//    	KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_3_ID);
	//    	assertNotEquals(null, trx);
	//    	
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys());
	//    	assertEquals(2, trx.getUserDefinedAttributeKeys().size());
	//    	assertEquals(ConstTest.SLOT_KEY_ASSOC_URI, trx.getUserDefinedAttributeKeys().get(0));
	//    	assertEquals(ConstTest.SLOT_KEY_TRX_DATE_POSTED, trx.getUserDefinedAttributeKeys().get(1));
	//    	assertEquals("https://my.transaction.link.01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_ASSOC_URI));
	//    	assertEquals("2023-10-01", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED));
	//    }

	// -----------------------------------------------------------------
	// Transaction Split
	// -----------------------------------------------------------------

	// ::TODO
	// There are none with slots

	// -----------------------------------------------------------------
	// Security
	// -----------------------------------------------------------------

	// No kvps
	// No such case

	// One kvp
	@Test
	public void test_01_sec_02() throws Exception {
		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByID(SEC_2_ID);
		assertNotEquals(null, sec);

		assertNotEquals(null, sec.getUserDefinedAttributeKeys());
		assertEquals(1, sec.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, sec.getUserDefinedAttributeKeys().get(0));
		assertEquals("DE0007100000", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	// Several kvps
	@Test
	public void test_01_sec_03() throws Exception {
		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByID(SEC_3_ID);
		assertNotEquals(null, sec);

		assertNotEquals(null, sec.getUserDefinedAttributeKeys());
		assertEquals(2, sec.getUserDefinedAttributeKeys().size());
		assertEquals(ConstTest.KVP_KEY_SEC_ONLINE_SOURCE, sec.getUserDefinedAttributeKeys().get(0));
		assertEquals(ConstTest.KVP_KEY_SEC_SECURITY_ID, sec.getUserDefinedAttributeKeys().get(1));
		assertEquals("Finanztreff", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_ONLINE_SOURCE));
		assertEquals("DE0007164600", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID));
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the values accessed by HasWritableUserDefinedAttributesImpl
	// can actually be modified -- both in memory and persisted in file.
	// -----------------------------------------------------------------

	// -----------------------------------------------------------------
	// Account
	// -----------------------------------------------------------------

	// ----------------------------
	// No slots

	@Test
	public void test_02_acct_01() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID.toString(), acct.getID().toString());

		// ----------------------------
		// Modify the object

		try {
			acct.setUserDefinedAttribute("abc", "def"); // illegal call, because does not exist
			assertEquals(0, 1);
		} catch ( KVPListDoesNotContainKeyException exc ) {
			acct.addUserDefinedAttribute("abc", "http://bore.dom");
		}

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test_02_acct_01_check_memory(acct);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test_02_acct_01_check_persisted(outFile);
	}

	private void test_02_acct_01_check_memory(KMyMoneyWritableAccount acct) throws Exception {
		assertEquals(ACCT_1_ID.toString(), acct.getID().toString()); // unchanged
		assertNotEquals(null, acct.getUserDefinedAttributeKeys()); // changed
		assertEquals(1, acct.getUserDefinedAttributeKeys().size()); // changed
		assertEquals("http://bore.dom", acct.getUserDefinedAttribute("abc")); // changed
	}

	private void test_02_acct_01_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyAccount acct = kmmOutFile.getAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID.toString(), acct.getID().toString()); // unchanged
		assertNotEquals(null, acct.getUserDefinedAttributeKeys()); // changed
		assertEquals(1, acct.getUserDefinedAttributeKeys().size()); // changed
		assertEquals("http://bore.dom", acct.getUserDefinedAttribute("abc")); // changed
	}

	// ----------------------------
	// One or more slots

	@Test
	public void test_02_acct_02() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_2_ID.toString(), acct.getID().toString());

		// ----------------------------
		// Modify the object

		try {
			acct.setUserDefinedAttribute("abc", "def"); // illegal call, because does not exist
			assertEquals(0, 1);
		} catch ( KVPListDoesNotContainKeyException exc ) {
			assertEquals(0, 0);
			acct.setUserDefinedAttribute(ConstTest.KVP_KEY_ACCT_IBAN, "Snoopy and Woodstock");
		}

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test_02_acct_02_check_memory(acct);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test_02_acct_02_check_persisted(outFile);
	}

	private void test_02_acct_02_check_memory(KMyMoneyWritableAccount acct) throws Exception {
		assertEquals(ACCT_2_ID.toString(), acct.getID().toString()); // unchanged
		assertNotEquals(null, acct.getUserDefinedAttributeKeys()); // unchanged
		assertEquals(1, acct.getUserDefinedAttributeKeys().size()); // unchanged
		assertEquals("Snoopy and Woodstock", acct.getUserDefinedAttribute(ConstTest.KVP_KEY_ACCT_IBAN)); // changed
	}

	private void test_02_acct_02_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyAccount acct = kmmOutFile.getAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_2_ID.toString(), acct.getID().toString()); // unchanged
		assertNotEquals(null, acct.getUserDefinedAttributeKeys()); // unchanged
		assertEquals(1, acct.getUserDefinedAttributeKeys().size()); // unchanged
		assertEquals("Snoopy and Woodstock", acct.getUserDefinedAttribute(ConstTest.KVP_KEY_ACCT_IBAN)); // changed
	}

	// ----------------------------
	// Top-level account

	@Test
	public void test_02_acct_03() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_3_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_3_ID.toString(), acct.getID().toString());

		// ----------------------------
		// Modify the object

		try {
			acct.setUserDefinedAttribute("abc", "def"); // illegal call, because is top-level account
			assertEquals(0, 1);
		} catch ( UnsupportedOperationException exc ) {
			assertEquals(0, 0);
		}
	}

	// -----------------------------------------------------------------
	// Transaction
	// -----------------------------------------------------------------

	// ----------------------------
	// No slots

	@Test
	public void test_02_trx_01() throws Exception {
		KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID());

		// ----------------------------
		// Modify the object

		try {
			trx.setUserDefinedAttribute("abc", "def"); // illegal call, because does not exist
			assertEquals(0, 1);
		} catch ( KVPListDoesNotContainKeyException exc ) {
			trx.addUserDefinedAttribute("abc", "http://bore.dom");
		}

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test_02_trx_01_check_memory(trx);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test_02_trx_01_check_persisted(outFile);
	}

	private void test_02_trx_01_check_memory(KMyMoneyWritableTransaction trx) throws Exception {
		assertEquals(TRX_1_ID, trx.getID()); // unchanged
		assertNotEquals(null, trx.getUserDefinedAttributeKeys()); // changed
		assertEquals(1, trx.getUserDefinedAttributeKeys().size()); // changed
		assertEquals("http://bore.dom", trx.getUserDefinedAttribute("abc")); // changed
	}

	private void test_02_trx_01_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneyTransaction trx = kmmOutFile.getTransactionByID(TRX_1_ID);
		assertNotEquals(null, trx);

		assertEquals(TRX_1_ID, trx.getID()); // unchanged
		assertNotEquals(null, trx.getUserDefinedAttributeKeys()); // changed
		assertEquals(1, trx.getUserDefinedAttributeKeys().size()); // changed
		assertEquals("http://bore.dom", trx.getUserDefinedAttribute("abc")); // changed
	}

	// ----------------------------
	// One or more slots

	// ::TODO / Still with errors:

	//    @Test
	//    public void test_02_trx_02() throws Exception {
	//    	KMyMoneyWritableTransaction trx = kmmInFile.getWritableTransactionByID(TRX_2_ID);
	//    	assertNotEquals(null, trx);
	//
	//    	assertEquals(TRX_2_ID, trx.getID());
	//
	//    	// ----------------------------
	//    	// Modify the object
	//
	//    	try {
	//        	trx.setUserDefinedAttribute("abc", "def"); // illegal call, because does not exist
	//        	assertEquals(0, 1);
	//    	} catch ( KVPListDoesNotContainKeyException exc ) {
	//        	trx.setUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED, "2024-01-05");
	//    	}
	//
	//    	// ----------------------------
	//    	// Check whether the object can has actually be modified
	//    	// (in memory, not in the file yet).
	//
	//    	test_02_trx_02_check_memory(trx);
	//
	//    	// ----------------------------
	//    	// Now, check whether the modified object can be written to the
	//    	// output file, then re-read from it, and whether is is what
	//    	// we expect it is.
	//
	//    	File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
	//    	// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
	//    	// + outFile.getPath() + "'");
	//    	outFile.delete(); // sic, the temp. file is already generated (empty),
	//    			          // and the KMyMoney file writer does not like that.
	//    	kmmInFile.writeFile(outFile);
	//
	//    	test_02_trx_02_check_persisted(outFile);
	//    }
	//    
	//    private void test_02_trx_02_check_memory(KMyMoneyWritableTransaction trx) throws Exception {
	//    	assertEquals(TRX_2_ID, trx.getID()); // unchanged
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys()); // unchanged
	//    	assertEquals(1, trx.getUserDefinedAttributeKeys().size()); // unchanged
	//    	assertEquals("Fri Jan 05 00:00:00 CET 2024", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED)); // changed
	//    }
	//
	//	private void test_02_trx_02_check_persisted(File outFile) throws Exception {
	//		kmmOutFile = new KMyMoneyFileImpl(outFile);
	//
	//		KMyMoneyTransaction trx = kmmOutFile.getTransactionByID(TRX_2_ID);
	//		assertNotEquals(null, trx);
	//
	//    	assertEquals(TRX_2_ID, trx.getID()); // unchanged
	//    	assertNotEquals(null, trx.getUserDefinedAttributeKeys()); // unchanged
	//    	assertEquals(1, trx.getUserDefinedAttributeKeys().size()); // unchanged
	//    	// sic:
	//    	assertEquals("Fri Jan 05 00:00:00 CET 2024", trx.getUserDefinedAttribute(ConstTest.SLOT_KEY_TRX_DATE_POSTED)); // changed
	//	}

	// -----------------------------------------------------------------
	// Transaction Split
	// -----------------------------------------------------------------

	// ::TODO
	// There are none with slots

	// -----------------------------------------------------------------
	// Security
	// -----------------------------------------------------------------

	// ----------------------------
	// No slots

	// No such data

	// ----------------------------
	// One or more slots

	@Test
	public void test_02_sec_02() throws Exception {
		KMyMoneyWritableSecurity sec = kmmInFile.getWritableSecurityByID(SEC_2_ID);
		assertNotEquals(null, sec);

		assertEquals(SEC_2_ID, sec.getID());

		// ----------------------------
		// Modify the object

		try {
			sec.setUserDefinedAttribute("abc", "def"); // illegal call, because does not exist
			assertEquals(0, 1);
		} catch ( KVPListDoesNotContainKeyException exc ) {
			assertEquals(0, 0);
			sec.setUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID, "KUXYZPP082");
		}

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test_02_sec_02_check_memory(sec);

		// ----------------------------
		// Now, check whether the modified object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test_02_sec_02_check_persisted(outFile);
	}

	private void test_02_sec_02_check_memory(KMyMoneyWritableSecurity sec) throws Exception {
		assertEquals(SEC_2_ID, sec.getID()); // unchanged
		assertNotEquals(null, sec.getUserDefinedAttributeKeys()); // unchanged
		assertEquals(1, sec.getUserDefinedAttributeKeys().size()); // unchanged
		assertEquals("KUXYZPP082", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID)); // changed
	}

	private void test_02_sec_02_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);

		KMyMoneySecurity sec = kmmOutFile.getSecurityByID(SEC_2_ID);
		assertNotEquals(null, sec);

		assertEquals(SEC_2_ID, sec.getID()); // unchanged
		assertNotEquals(null, sec.getUserDefinedAttributeKeys()); // unchanged
		assertEquals(1, sec.getUserDefinedAttributeKeys().size()); // unchanged
		assertEquals("KUXYZPP082", sec.getUserDefinedAttribute(ConstTest.KVP_KEY_SEC_SECURITY_ID)); // changed
	}

}
