package org.kmymoney.api.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kmymoney.api.ConstTest;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.TestKMyMoneyAccountImpl;
import org.kmymoney.api.read.impl.aux.KMMFileStats;
import org.kmymoney.api.write.KMyMoneyWritableAccount;
import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestKMyMoneyWritableAccountImpl {
	private static final KMMComplAcctID ACCT_1_ID = TestKMyMoneyAccountImpl.ACCT_1_ID;
	private static final KMMComplAcctID ACCT_2_ID = TestKMyMoneyAccountImpl.ACCT_2_ID;
	private static final KMMComplAcctID ACCT_3_ID = TestKMyMoneyAccountImpl.ACCT_3_ID;
	private static final KMMComplAcctID ACCT_4_ID = TestKMyMoneyAccountImpl.ACCT_4_ID;
	private static final KMMComplAcctID ACCT_9_ID = TestKMyMoneyAccountImpl.ACCT_9_ID;

	// Top-level accounts
	private static final KMMComplAcctID ACCT_10_ID = TestKMyMoneyAccountImpl.ACCT_10_ID;
	private static final KMMComplAcctID ACCT_11_ID = TestKMyMoneyAccountImpl.ACCT_11_ID;
	private static final KMMComplAcctID ACCT_12_ID = TestKMyMoneyAccountImpl.ACCT_12_ID;
	private static final KMMComplAcctID ACCT_13_ID = TestKMyMoneyAccountImpl.ACCT_13_ID;
	private static final KMMComplAcctID ACCT_14_ID = TestKMyMoneyAccountImpl.ACCT_14_ID;
	
	private static final KMMInstID INST_1_ID = TestKMyMoneyAccountImpl.INST_1_ID;
	private static final KMMInstID INST_2_ID = TestKMyMoneyAccountImpl.INST_2_ID;

	// -----------------------------------------------------------------

	private KMyMoneyWritableFileImpl kmmInFile = null;
	private KMyMoneyFileImpl kmmOutFile = null;

	private KMMFileStats kmmInFileStats = null;
	private KMMFileStats kmmOutFileStats = null;

	private KMMComplAcctID newID = null;

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
		return new JUnit4TestAdapter(TestKMyMoneyWritableAccountImpl.class);
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
	// Cf. TestKMyMoneyAccountImpl.test01_xyz
	//
	// Check whether the KMyMoneyWritableAccount objects returned by
	// KMyMoneyWritableFileImpl.getWritableAccountByID() are actually
	// complete (as complete as returned be KMyMoneyFileImpl.getAccountByID().

	@Test
	public void test01_1() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.CHECKING, acct.getType());
		assertEquals(INST_1_ID, acct.getInstitutionID());
		assertEquals("RaiBa", acct.getInstitution().getName());
		assertEquals("Giro RaiBa", acct.getName());
		assertEquals("Anlagen:Barvermögen:Giro RaiBa", acct.getQualifiedName());
		assertEquals("Girokonto 1", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals("A000002", acct.getParentAccountID().toString());
		assertEquals(0, acct.getChildren().size());

		assertEquals(11674.50, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(11674.50, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(17, acct.getTransactions().size());
		assertEquals("T000000000000000001", acct.getTransactions().get(0).getID().toString());
		assertEquals("T000000000000000002", acct.getTransactions().get(1).getID().toString());
	}

	@Test
	public void test01_2() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_2_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_2_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INVESTMENT, acct.getType());
		assertEquals(INST_1_ID, acct.getInstitutionID());
		assertEquals("RaiBa", acct.getInstitution().getName());
		assertEquals("Depot RaiBa", acct.getName());
		assertEquals("Anlagen:Finanzanlagen:Depot RaiBa", acct.getQualifiedName());
		assertEquals("Aktiendepot 1", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals("A000061", acct.getParentAccountID().toString());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(3, acctList.size());
		assertEquals("A000064", acctList.get(0).getID().toString());
		assertEquals("A000063", acctList.get(1).getID().toString());

		// ::TODO
		assertEquals(0.0, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(3773.0, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		// ::TODO
		assertEquals(0, acct.getTransactions().size());
		//    assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getID());
		//    assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(1).getID());
	}

	@Test
	public void test01_3() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_3_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_3_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INCOME, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Gehalt", acct.getName());
		assertEquals("Einnahme:Gehalt", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(ACCT_12_ID, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(2, acctList.size());
		assertEquals("A000050", acctList.get(0).getID().toString());
		assertEquals("A000051", acctList.get(1).getID().toString());

		// ::CHECK: Really negative?
		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-6500.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_4() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_4_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_4_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.STOCK, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("DE0007100000 Mercedes-Benz Group AG", acct.getName());
		assertEquals("Anlagen:Finanzanlagen:Depot RaiBa:DE0007100000 Mercedes-Benz Group AG", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("SECURITY:E000002", acct.getQualifSecCurrID().toString());

		assertEquals(ACCT_2_ID, acct.getParentAccountID());
		assertEquals(0, acct.getChildren().size());

		assertEquals(34, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(34, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(1980.50, acct.getBalance(LocalDate.now(), Currency.getInstance("EUR")).doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(1980.50, acct.getBalanceRecursive(LocalDate.now(), Currency.getInstance("EUR")).doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(2, acct.getTransactions().size());
	}

	// -----------------------------------------------------------------
	// Top-level accounts

	@Test
	public void test01_10() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_10_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_10_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.ASSET, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Anlagen", acct.getName());
		assertEquals("Anlagen", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(2, acctList.size());
		assertEquals("A000002", acctList.get(0).getID().toString());
		assertEquals("A000061", acctList.get(1).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(15597.50, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_11() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_11_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_11_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.LIABILITY, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Verbindlichkeiten", acct.getName());
		assertEquals("Verbindlichkeiten", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());
		assertEquals(1, acct.getChildren().size());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(1, acctList.size());
		assertEquals("A000058", acctList.get(0).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_12() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_12_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_12_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.INCOME, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Einnahme", acct.getName());
		assertEquals("Einnahme", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(4, acctList.size());
		assertEquals("A000049", acctList.get(0).getID().toString());
		assertEquals("A000052", acctList.get(1).getID().toString());
		assertEquals("A000068", acctList.get(2).getID().toString());
		assertEquals("A000053", acctList.get(3).getID().toString());

		// ::CHECK: Really negative?
		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(-16500.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_13() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_13_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_13_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.EXPENSE, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Ausgabe", acct.getName());
		assertEquals("Ausgabe", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(16, acctList.size());
		assertEquals("A000072", acctList.get(0).getID().toString());
		assertEquals("A000006", acctList.get(1).getID().toString());
		assertEquals("A000011", acctList.get(2).getID().toString());
		// etc.
		// assertEquals("A000xyz", ((KMyMoneyAccount) acctArr[3]).getID().toString());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(920.5, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	@Test
	public void test01_14() throws Exception {
		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_14_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_14_ID, acct.getID());
		assertEquals(KMyMoneyAccount.Type.EQUITY, acct.getType());
		assertEquals(null, acct.getInstitutionID());
		assertEquals(null, acct.getInstitution());
		assertEquals("Eigenkapital", acct.getName());
		assertEquals("Eigenkapital", acct.getQualifiedName());
		assertEquals("", acct.getMemo());
		assertEquals("CURRENCY:EUR", acct.getQualifSecCurrID().toString());

		assertEquals(null, acct.getParentAccountID());

		List<KMyMoneyAccount> acctList = acct.getChildren();
		assertEquals(0, acctList.size());

		assertEquals(0.00, acct.getBalance().doubleValue(), ConstTest.DIFF_TOLERANCE);
		assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), ConstTest.DIFF_TOLERANCE);

		assertEquals(0, acct.getTransactions().size());
	}

	// -----------------------------------------------------------------
	// PART 2: Modify existing objects
	// -----------------------------------------------------------------
	// Check whether the KMyMoneyWritableAccount objects returned by
	// can actually be modified -- both in memory and persisted in file.

	@Test
	public void test02_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID, acct.getID());

		// ----------------------------
		// Modify the object

		acct.setInstitutionID(INST_2_ID);
		acct.setName("Giro d'Italia");
		acct.setMemo("My favorite account");

		// ----------------------------
		// Check whether the object can has actually be modified
		// (in memory, not in the file yet).

		test02_1_check_memory(acct);

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
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_10_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_10_ID, acct.getID());

		// ----------------------------
		// Try modifications -- all of them forbidden, because
		// the account is a top-level account

		try {
			acct.setType(KMyMoneyAccount.Type.EQUITY); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		try {
			acct.setCurrency("EUR"); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		try {
			acct.setInstitutionID(INST_2_ID); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		try {
			acct.setName("Wurstel con crauti"); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		try {
			acct.setMemo("Bingo bango bongo"); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
		try {
			acct.setUserDefinedAttribute("abc", "xyz"); // illegal call for top-level account
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		
	}

	private void test02_1_check_memory(KMyMoneyWritableAccount acct) throws Exception {
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		assertEquals(ACCT_1_ID, acct.getID()); // unchanged
		assertEquals(INST_2_ID, acct.getInstitutionID()); // changed
		assertEquals("Giro d'Italia", acct.getName()); // changed
		assertEquals("My favorite account", acct.getMemo()); // changed
	}

	private void test02_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyAccount acct = kmmOutFile.getAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);

		assertEquals(ACCT_1_ID, acct.getID()); // unchanged
		assertEquals(INST_2_ID, acct.getInstitutionID()); // changed
		assertEquals("Giro d'Italia", acct.getName()); // changed
		assertEquals("My favorite account", acct.getMemo()); // changed
	}

	// -----------------------------------------------------------------
	// PART 3: Create new objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 3.1: High-Level
	// ------------------------------

	@Test
	public void test03_1_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = 
				kmmInFile
					.createWritableAccount(KMyMoneyAccount.Type.EXPENSE,
										   Currency.getInstance(kmmInFile.getDefaultCurrencyID()),
										   ACCT_13_ID,
										   "Various expenses");
		acct.setInstitutionID(INST_2_ID);
		acct.setMemo("All the stuff that does not fit into the other expenses accounts");

		// ----------------------------
		// Check whether the object can has actually be created
		// (in memory, not in the file yet).

		test03_1_1_check_memory(acct);

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

	private void test03_1_1_check_memory(KMyMoneyWritableAccount acct) throws Exception {
		assertEquals(ConstTest.Stats.NOF_ACCT + 1, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT + 1, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		newID = acct.getID();
		assertEquals(INST_2_ID, acct.getInstitutionID());
		assertEquals("Various expenses", acct.getName());
		assertEquals(KMyMoneyAccount.Type.EXPENSE, acct.getType());
		assertEquals(ACCT_13_ID, acct.getParentAccountID());
		assertEquals(kmmInFile.getDefaultCurrencyID(), acct.getQualifSecCurrID().getCode());
		assertEquals("All the stuff that does not fit into the other expenses accounts", acct.getMemo());
	}

	private void test03_1_1_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_ACCT + 1, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT + 1, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyAccount acct = kmmOutFile.getAccountByID(newID);
		assertNotEquals(null, acct);

		assertEquals(newID, acct.getID());
		assertEquals(INST_2_ID, acct.getInstitutionID());
		assertEquals("Various expenses", acct.getName());
		assertEquals(KMyMoneyAccount.Type.EXPENSE, acct.getType());
		assertEquals(ACCT_13_ID, acct.getParentAccountID());
		assertEquals(kmmInFile.getDefaultCurrencyID(), acct.getQualifSecCurrID().getCode());
		assertEquals("All the stuff that does not fit into the other expenses accounts", acct.getMemo());
	}

	// ------------------------------
	// PART 3.2: Low-Level
	// ------------------------------

	@Test
	public void test03_2_1() throws Exception {
		KMyMoneyWritableAccount acct = 
				kmmInFile
					.createWritableAccount(KMyMoneyAccount.Type.LIABILITY,
										   Currency.getInstance(kmmInFile.getDefaultCurrencyID()),
										   ACCT_3_ID,
										   "SNAF");
		acct.setMemo("Stuff never accounted for");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritablePayeeImpl.test01_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_0_check_1_xmllint(outFile);
		test03_2_1_check_2(outFile);
	}

	// -----------------------------------------------------------------

	//  @Test
	//  public void test03_2_0_check_1() throws Exception
	//  {
	//      assertNotEquals(null, outFileGlob);
	//      assertEquals(true, outFileGlob.exists());
	//
	//      // Check if generated document is valid
	//      // ::TODO: in fact, not even the input document is.
	//      // Build document
	//      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	//      DocumentBuilder builder = factory.newDocumentBuilder(); 
	//      Document document = builder.parse(outFileGlob);
	//      System.err.println("xxxx XML parsed");
	//
	//      // https://howtodoinjava.com/java/xml/read-xml-dom-parser-example/
	//      Schema schema = null;
	//      String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	//      SchemaFactory factory1 = SchemaFactory.newInstance(language);
	//      schema = factory1.newSchema(outFileGlob);
	//
	//      Validator validator = schema.newValidator();
	//      DOMResult validResult = null; 
	//      validator.validate(new DOMSource(document), validResult);
	//      System.out.println("yyy: " + validResult);
	//      // assertEquals(validResult);
	//  }

	// Sort of "soft" variant of above function
	// CAUTION: Not platform-independent!
	// Tool "xmllint" must be installed and in path
	private void test03_2_0_check_1_xmllint(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Check if generated document is valid
		ProcessBuilder bld = new ProcessBuilder("xmllint", outFile.getAbsolutePath());
		Process prc = bld.start();

		if ( prc.waitFor() == 0 ) {
			assertEquals(0, 0);
		} else {
			assertEquals(0, 1);
		}
	}

	private void test03_2_1_check_2(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(outFile);
		//      System.err.println("xxxx XML parsed");

		// Normalize the XML structure
		document.getDocumentElement().normalize();
		//      System.err.println("xxxx XML normalized");

		NodeList nList = document.getElementsByTagName("ACCOUNT");
		assertEquals(ConstTest.Stats.NOF_ACCT + 1, nList.getLength());

		// Last (new) node
		Node lastNode = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());
		Element elt = (Element) lastNode;
		assertEquals("" + KMyMoneyAccount.Type.LIABILITY.getCode(), elt.getAttribute("type"));
		assertEquals(ACCT_3_ID.toString(), elt.getAttribute("parentaccount"));
		assertEquals("SNAF", elt.getAttribute("name"));
		assertEquals("Stuff never accounted for", elt.getAttribute("description"));
	}

	// -----------------------------------------------------------------

	@Test
	public void test03_2_4() throws Exception {
		KMyMoneyWritableAccount acct1 = 
				kmmInFile
					.createWritableAccount(KMyMoneyAccount.Type.LIABILITY,
										   Currency.getInstance(kmmInFile.getDefaultCurrencyID()),
										   ACCT_3_ID,
										   "SNAF");
		acct1.setMemo("Stuff never accounted for");

		KMyMoneyWritableAccount acct2 = 
				kmmInFile
					.createWritableAccount(KMyMoneyAccount.Type.CHECKING,
										   Currency.getInstance(kmmInFile.getDefaultCurrencyID()),
										   ACCT_2_ID,
										   "BAHAMAS SECRET");
		acct2.setMemo("My very VERY secret account on the Bahamas");

		KMyMoneyWritableAccount acct3 = 
				kmmInFile
					.createWritableAccount(KMyMoneyAccount.Type.CASH,
										   Currency.getInstance(kmmInFile.getDefaultCurrencyID()),
										   ACCT_1_ID,
										   "Bug-Out Cash");
		acct3.setMemo("My hopefully secret cash wallet for crises");

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		//      System.err.println("Outfile for TestKMyMoneyWritablePayeeImpl.test02_1: '" + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test03_2_4_check(outFile);
	}

	private void test03_2_4_check(File outFile) throws Exception {
		assertNotEquals(null, outFile);
		assertEquals(true, outFile.exists());

		// Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(outFile);
		//      System.err.println("xxxx XML parsed");

		// Normalize the XML structure
		document.getDocumentElement().normalize();
		//      System.err.println("xxxx XML normalized");

		NodeList nList = document.getElementsByTagName("ACCOUNT");
		assertEquals(ConstTest.Stats.NOF_ACCT + 3, nList.getLength());

		// Last three nodes (the new ones)
		Node node = nList.item(nList.getLength() - 3);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		Element elt = (Element) node;
		assertEquals("" + KMyMoneyAccount.Type.LIABILITY.getCode(), elt.getAttribute("type"));
		assertEquals(ACCT_3_ID.toString(), elt.getAttribute("parentaccount"));
		assertEquals("SNAF", elt.getAttribute("name"));
		assertEquals("Stuff never accounted for", elt.getAttribute("description"));

		node = nList.item(nList.getLength() - 2);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("" + KMyMoneyAccount.Type.CHECKING.getCode(), elt.getAttribute("type"));
		assertEquals(ACCT_2_ID.toString(), elt.getAttribute("parentaccount"));
		assertEquals("BAHAMAS SECRET", elt.getAttribute("name"));
		assertEquals("My very VERY secret account on the Bahamas", elt.getAttribute("description"));

		node = nList.item(nList.getLength() - 1);
		assertEquals(Node.ELEMENT_NODE, node.getNodeType());
		elt = (Element) node;
		assertEquals("" + KMyMoneyAccount.Type.CASH.getCode(), elt.getAttribute("type"));
		assertEquals(ACCT_1_ID.toString(), elt.getAttribute("parentaccount"));
		assertEquals("Bug-Out Cash", elt.getAttribute("name"));
		assertEquals("My hopefully secret cash wallet for crises", elt.getAttribute("description"));
	}

	// -----------------------------------------------------------------
	// PART 4: Delete objects
	// -----------------------------------------------------------------

	// ------------------------------
	// PART 4.1: High-Level
	// ------------------------------

	@Test
	public void test04_1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_1_ID);
		assertNotEquals(null, acct);
		assertEquals(ACCT_1_ID, acct.getID());

		// Check if modifiable
		assertEquals(true, acct.hasTransactions()); // there are payments

		// Variant 1
		try {
			kmmInFile.removeAccount(acct); // Correctly fails because there are transactions/trx splits to it
			assertEquals(1, 0);
		} catch ( IllegalStateException exc ) {
			assertEquals(0, 0);
		}

		// Variant 2
		try {
			acct.remove(); // Correctly fails because there are transactions/trx splits to it
			assertEquals(1, 0);
		} catch ( IllegalStateException exc ) {
			assertEquals(0, 0);
		}
	}
	
	@Test
	public void test04_2_var1() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_9_ID);
		assertNotEquals(null, acct);

		// Check if modifiable
		assertEquals(false, acct.hasTransactions()); // there are transactions/trx splits


		// Core (variant-specific):
		kmmInFile.removeAccount(acct);

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_2_check_memory(acct);

		// ----------------------------
		// Now, check whether the deletions have been written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_2_check_persisted(outFile);
	}

	@Test
	public void test04_2_var2() throws Exception {
		kmmInFileStats = new KMMFileStats(kmmInFile);

		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		KMyMoneyWritableAccount acct = kmmInFile.getWritableAccountByID(ACCT_9_ID);
		assertNotEquals(null, acct);

		// Check if modifiable
		assertEquals(false, acct.hasTransactions()); // there are no transactions/trx splits

		// Core (variant-specific):
		acct.remove();

		// ----------------------------
		// Check whether the objects have actually been deleted
		// (in memory, not in the file yet).

		test04_2_check_memory(acct);

		// ----------------------------
		// Now, check whether the deletions have been written to the
		// output file, then re-read from it, and whether is is what
		// we expect it is.

		File outFile = folder.newFile(ConstTest.KMM_FILENAME_OUT);
		// System.err.println("Outfile for TestKMyMoneyWritableCustomerImpl.test01_1: '"
		// + outFile.getPath() + "'");
		outFile.delete(); // sic, the temp. file is already generated (empty),
		// and the KMyMoney file writer does not like that.
		kmmInFile.writeFile(outFile);

		test04_2_check_persisted(outFile);
	}

	// ---------------------------------------------------------------

	private void test04_2_check_memory(KMyMoneyWritableAccount acct) throws Exception {
		assertEquals(ConstTest.Stats.NOF_ACCT - 1, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT - 1, kmmInFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		// CAUTION / ::TODO
		// Old Object still exists and is unchanged
		// Exception: no splits any more
		// Don't know what to do about this oddity right now,
		// but it needs to be addressed at some point.
		assertEquals(ACCT_9_ID, acct.getID());
    	assertEquals(KMyMoneyAccount.Type.EXPENSE, acct.getType());
    	assertEquals("Gas", acct.getName());
    	// usw.
		
		// However, the account cannot newly be instantiated any more,
		// just as you would expect.
		try {
			KMyMoneyWritableAccount acctNow1 = kmmInFile.getWritableAccountByID(ACCT_9_ID);
			assertEquals(1, 0);
		} catch ( Exception exc ) {
			assertEquals(0, 0);
		}
		// Same for a non non-writable instance. 
		// However, due to design asymmetry, no exception is thrown here,
		// but the method just returns null.
		KMyMoneyAccount acctNow2 = kmmInFile.getAccountByID(ACCT_9_ID);
		assertEquals(null, acctNow2);
	}

	private void test04_2_check_persisted(File outFile) throws Exception {
		kmmOutFile = new KMyMoneyFileImpl(outFile);
		kmmOutFileStats = new KMMFileStats(kmmOutFile);

		assertEquals(ConstTest.Stats.NOF_ACCT - 1, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.RAW));
		assertEquals(ConstTest.Stats.NOF_ACCT - 1, kmmOutFileStats.getNofEntriesAccounts(KMMFileStats.Type.CACHE));

		// The transaction does not exist any more, just as you would expect.
		// However, no exception is thrown, as opposed to test04_1_check_memory()
		KMyMoneyAccount acct = kmmOutFile.getAccountByID(ACCT_9_ID);
		assertEquals(null, acct); // sic
	}

	// ------------------------------
	// PART 4.2: Low-Level
	// ------------------------------
	
	// ::EMPTY

}
