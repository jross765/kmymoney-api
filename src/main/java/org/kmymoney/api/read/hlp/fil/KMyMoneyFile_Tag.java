package org.kmymoney.api.read.hlp.fil;

import java.util.Collection;

import org.kmymoney.api.read.KMyMoneyTag;
import org.kmymoney.base.basetypes.simple.KMMTagID;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.beanbase.TooManyEntriesFoundException;

public interface KMyMoneyFile_Tag {

	/**
	 * @param tagID the unique ID of the tag to look for
	 * @return the tag or null if it's not found
	 */
	KMyMoneyTag getTagByID(KMMTagID tagID);

	/**
	 * @param expr search expression
	 * @return
	 * 
	 * @see #getTagsByName(String, boolean)
	 */
	Collection<KMyMoneyTag> getTagsByName(String expr);

	/**
	 * @param expr search expression
	 * @param relaxed
	 * @return
	 * 
	 * @see #getTagsByName(String)
	 */
	Collection<KMyMoneyTag> getTagsByName(String expr, boolean relaxed);

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
	Collection<KMyMoneyTag> getTags();

}
