package org.kmymoney.api.write.impl.aux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyPayee;
import org.kmymoney.api.read.aux.KMMAddress;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.read.impl.aux.TestKMMAddressImpl;
import org.kmymoney.api.write.KMyMoneyWritableInstitution;
import org.kmymoney.api.write.KMyMoneyWritablePayee;
import org.kmymoney.api.write.aux.KMMWritableAddress;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMPyeID;

import junit.framework.JUnit4TestAdapter;

public class TestKMMWritableAddressImpl {
	
	public static final KMMPyeID PYE_2_ID = TestKMMAddressImpl.PYE_2_ID;
	public static final KMMPyeID PYE_3_ID = TestKMMAddressImpl.PYE_3_ID;

	public static final KMMInstID INST_1_ID = TestKMMAddressImpl.INST_1_ID;
	public static final KMMInstID INST_2_ID = TestKMMAddressImpl.INST_2_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMPyeID newID = null;

	// https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
	@SuppressWarnings("exports")
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	// -----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	@SuppressWarnings("exports")
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(TestKMMWritableAddressImpl.class);
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
	// Cf. TestKMMAddressImpl.test01_1/02_1
	//
	// Check whether the KMyMoneyWritablePayee objects returned by
	// KMyMoneyWritableFileImpl.getWritablePayeeByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getPayeeByID().

	@Test
	public void test01_1() throws Exception {
		KMyMoneyWritablePayee pye = kmmInFile.getWritablePayeeByID(PYE_3_ID);
		assertNotEquals(null, pye);
		assertEquals(PYE_3_ID, pye.getID());

		KMMAddress addr = pye.getAddress();
		assertNotEquals(null, addr);

		assertEquals("Krailbacher Gasse 123 a\n" + 
		             "Postfach ABC\n" + 
				     "Kennwort Kasperlpost", addr.getStreet());
		assertEquals("Österreich", addr.getState());
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
	public void test01_2() throws Exception {
		KMyMoneyWritableInstitution inst = kmmInFile.getWritableInstitutionByID(INST_2_ID);
		assertNotEquals(null, inst);
		assertEquals(INST_2_ID, inst.getID());

		KMMAddress addr = inst.getAddress();
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

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritablePayee objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		KMyMoneyWritablePayee pye = kmmInFile.getWritablePayeeByID(PYE_3_ID);
		assertNotEquals(null, pye);
		assertEquals(PYE_3_ID, pye.getID());

		KMMWritableAddress addr = pye.getWritableAddress();
		assertNotEquals(null, addr);

		// ----------------------------
		// Modify the object

		addr.setStreet("Judengasse 3");
		addr.setCity("Salzburg");
		addr.setZip("1234");
		addr.setZipCode("2345");
		addr.setPostCode("3456");
		addr.setTelephone("+43 - 12 - 37403273");

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(addr);

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

		test02_1_check_persisted(outFile);
	}

	@Test
	public void test02_2() throws Exception {
		// ::TODO
	}

	private void test02_1_check_memory(KMMWritableAddress addr) throws Exception {
		assertEquals("Judengasse 3", addr.getStreet()); // unchanged
		assertEquals("Salzburg", addr.getCity()); // changed
		assertEquals("1234", addr.getZip()); // changed
		assertEquals("2345", addr.getZipCode()); // changed
		assertEquals("3456", addr.getPostCode()); // changed
		assertEquals("Österreich", addr.getState()); // unchanged
		assertEquals("Österreich", addr.getCounty()); // unchanged
		assertEquals("Österreich", addr.getCountry()); // unchanged
		assertEquals("+43 - 12 - 37403273", addr.getTelephone()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		KMyMoneyPayee pye = kmmOutFile.getPayeeByID(PYE_3_ID);
		assertNotEquals(null, pye);
		assertEquals(PYE_3_ID, pye.getID());

		KMMAddress addr = pye.getAddress();
		assertNotEquals(null, addr);

		assertEquals("Judengasse 3", addr.getStreet()); // unchanged
		assertEquals("Salzburg", addr.getCity()); // changed
		assertEquals("1234", addr.getZip()); // changed
		assertEquals("2345", addr.getZipCode()); // changed
		assertEquals("3456", addr.getPostCode()); // changed
		assertEquals("Österreich", addr.getState()); // unchanged
		assertEquals("Österreich", addr.getCounty()); // unchanged
		assertEquals("Österreich", addr.getCountry()); // unchanged
		assertEquals("+43 - 12 - 37403273", addr.getTelephone()); // changed
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// Note: In fact, this section is not really necessary/redundant,
	// because address objects seem to *always* be generated; not only
	// when generated with the standard KMyMoney GUI, but also with this
	// library, and even with a fresh and naked payee object.
	// It is just, that, currently, we do not know whether it really 
	// *never ever* occurs that a payee object is generated without address
	// sub-object, under no circumstances conceivable. The author doubts it...
	// Cf. test cases in TestKmyMoneyWritablePayee, part 3 and
	// the comments in test03_1_1() below.

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		KMyMoneyWritablePayee pye = kmmInFile.getWritablePayeeByID(PYE_2_ID);
		assertNotEquals(null, pye);
		assertEquals(PYE_2_ID, pye.getID());

		KMMWritableAddress addr = pye.getWritableAddress();
		
		// ----------------------------

		addr.setStreet("Champs Élysées");
		addr.setCity("Paris");
		addr.setZipCode("75000");
		addr.setTelephone("+33 - 1 - 99 91 99 91");

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(addr);

		// ----------------------------
		// Now, check whether the created object can be written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_1_1_check_persisted(outFile);
	}

	private void test03_1_1_check_memory(KMMWritableAddress addr) throws Exception {
		assertEquals("Champs Élysées", addr.getStreet());
		assertEquals("Paris", addr.getCity());
		assertEquals("75000", addr.getZipCode());
		assertEquals("+33 - 1 - 99 91 99 91", addr.getTelephone());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		KMyMoneyPayee pye = kmmOutFile.getPayeeByID(PYE_2_ID);
		assertNotEquals(null, pye);
		assertEquals(PYE_2_ID, pye.getID());

		KMMAddress addr = pye.getAddress();
		assertNotEquals(null, addr);

		assertEquals("Champs Élysées", addr.getStreet());
		assertEquals("Paris", addr.getCity());
		assertEquals("75000", addr.getZipCode());
		assertEquals("+33 - 1 - 99 91 99 91", addr.getTelephone());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	// ::TODO

}
