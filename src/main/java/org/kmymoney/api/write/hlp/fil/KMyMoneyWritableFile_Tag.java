package org.kmymoney.api.write.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.api.write.KMyMoneyWritableTag;
import org.kmymoney.base.basetypes.simple.KMMTagID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

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
	 * @param expr search expression
	 * @return
	 * 
	 * @see #getWritableTagsByName(String, boolean)
	 */
	Collection<KMyMoneyWritableTag> getWritableTagsByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 * 
	 * @see #getWritableTagsByName(String)
	 */
	Collection<KMyMoneyWritableTag> getWritableTagsByName(String expr, boolean relaxed);

	/**
	 * @param expr search expression
	 * @return
	 * @throws NoEntryFoundException
	 * @throws TooManyEntriesFoundException
	 */
	KMyMoneyTag getTagByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

	/**
	 * @return a (read-only) collection of all tags Do not modify the
	 *         returned collection!
	 */
	Collection<KMyMoneyWritableTag> getWritableTags();

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
