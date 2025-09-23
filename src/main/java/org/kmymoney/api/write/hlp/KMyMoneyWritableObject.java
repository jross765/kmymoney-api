package org.kmymoney.api.write.hlp;

import org.kmymoney.api.read.hlp.KMyMoneyObject;
import org.kmymoney.api.write.KMyMoneyWritableFile;

/**
 * Interface that all interfaces for writable KMyMoney entities shall implement
 */
public interface KMyMoneyWritableObject extends KMyMoneyObject {

	/**
	 * @return the file we belong to.
	 */
	KMyMoneyWritableFile getWritableKMyMoneyFile();

}
