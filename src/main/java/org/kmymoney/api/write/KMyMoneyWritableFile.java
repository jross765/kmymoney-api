package org.kmymoney.api.write;

import java.io.File;
import java.io.IOException;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Acct;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Curr;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Inst;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Prc;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Pye;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Sec;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Tag;
import org.kmymoney.api.write.hlp.fil.KMyMoneyWritableFile_Trx;

/**
 * Extension of KMyMoneyFile that allows writing.
 *
 * @see KMyMoneyFile
 */
public interface KMyMoneyWritableFile extends KMyMoneyFile, 
                                              KMyMoneyWritableObject,
                                              KMyMoneyWritableFile_Inst,
                                              KMyMoneyWritableFile_Acct,
                                              KMyMoneyWritableFile_Trx,
                                              KMyMoneyWritableFile_Pye,
                                              KMyMoneyWritableFile_Tag,
                                              KMyMoneyWritableFile_Curr,
                                              KMyMoneyWritableFile_Sec,
                                              KMyMoneyWritableFile_Prc,
                                              HasWritableUserDefinedAttributes
{
	public enum CompressMode {
		COMPRESS,
		DO_NOT_COMPRESS,
		GUESS_FROM_FILENAME
	}
	
	// ---------------------------------------------------------------

	/**
	 * @return true if this file has been modified.
	 */
	boolean isModified();

	/**
	 * @param pB true if this file has been modified.
	 * @see {@link #isModified()}
	 */
	void setModified(boolean pB);

	/**
	 * Write the data to the given file. That file becomes the new file returned by
	 * {@link KMyMoneyFile#getKMyMoneyFile()}
	 * 
	 * @param file the file to write to
	 * @throws IOException kn io-poblems
	 */
	void writeFile(File file) throws IOException;

	void writeFile(File file, CompressMode compMode) throws IOException;

	/**
	 * The value is guaranteed not to be later than then the maximum of the current
	 * system-time and the modification-time in the file at the time of the last
	 * (full) read or successful write operation.
	 * <br> 
	 * It is thus suitable to detect if the file has been modified outside of this library.
	 * 
	 * @return the time in ms (compatible with File.lastModified) of the last
	 *         write-operation
	 */
	long getLastWriteTime();

    // ---------------------------------------------------------------

	/**
	 * @return the underlying JAXB-element
	 */
	@SuppressWarnings("exports")
	KMYMONEYFILE getRootElement();

}
