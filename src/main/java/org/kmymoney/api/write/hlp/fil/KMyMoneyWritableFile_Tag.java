package org.kmymoney.api.write.hlp.fil;

import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.base.basetypes.simple.KMMTagID;

public interface KMyMoneyWritableFile_Tag {
	
	/**
	 * @param tagID the unique id of the tag to look for
	 * @return the tag or null if it's not found
	 * 
	 * @see #getTagByID(KMMTagID)
	 */
	KMyMoneyWritableTag getWritableTagByID(KMMTagID tagID);

	// ----------------------------

	/**
	 * @param name 
	 * @return a new tag with no values that is already added to this file
	 */
	KMyMoneyWritableTag createWritableTag(String name);

	/**
	 *
	 * @param tag the tag to remove.
	 */
	void removeTag(KMyMoneyWritableTag tag);

}
