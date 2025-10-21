# Notes on the Module "API"

## What Does It Do?

This is the core module of the project, providing all low-level read-/write access functions to a 
KMyMoney 
file.

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JKMyMoneyLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

* This repository contains no history before V. 1.7 (cf. notes in parent repo).

## Major Changes 
### V. 0.8.0 &rarr; 0.8.1
* Small corrections / bug fixes:
  * `KMyMoneyPayee(Impl)`: 
    * Fixed bug in split-bookkeeping stuff.
    * Added two attributes (methods) coming from new file format that had been forgotten in V. 0.8.0.
    * Improved interface for matching stuff and changed impl. accordingly.
  * `KMyMoneyTransactionImpl`: 
    * Added `getDateEnteredFormatted()` (consistency, had been forgotten)  
    * Fixed bug in `getXYZDateFormatted()`
  * `KMyMoneySecurityImpl`: Had forgotten to implement `setRoundingMethod()`.

* Little bit of cleanup-work.

### V. 0.7 &rarr; 0.8
**Caution: This is the first version of the API that is compatible with the KMyMoney V. 5.2.x format. No support for file format from V. 5.1.x any more.**

* Changes to support new file format (KMyMoney V. 5.2.x):
  * Removed all code that referred to internal counters (they do not exist any more).
  * Introduced KMMAccountReconciliation(Impl) (sub-entity of KMyMoneyAccount).

* `KMyMoneyAccount(Impl)`: 
  * New method `getReconciliations()`
  * New method `printTree()`

* `KMyMoney(Writable)TransactionSplit(Impl)`: New methods `getNumber()` and `setNumber()`.

* `KMyMoneyFile(Impl)`: 
  * Changed return type of *all* `getAccountsXYZ()` from Collection to List.
  * Dto. for `getTransactionsXYZ()`.
  * Dto. for `getTransactionSplitsXYZ()`.
  * Dto. for `getSecuritiesXYZ()`.
  * Dto. for `getPricesXYZ()`.
  * New method `dump()`.

### V. 0.6 &rarr; 0.7
**Caution: This is the last version of the API that is compatible with the KMyMoney V. 5.1.x format.**

* Fixed a couple of bugs in write-branch of API (for various entities), esp. in object-deleting code.

* Introduced `KMyMoney(Writable)Tag(Impl)`.

* Institutions have accounts, obviously. Changed implementation of `KMyMoneyInstitutionImpl`, `KMyMoneyAccountImpl` and other classes that reflect this.

* `KMyMoneyWritableTransactionSplitImpl`: Fixed bug in `setShares()`.

* `KMyMoneyWritableAccount(Impl)`, `KMyMoney(Writable)File(Impl)`: Expanded interface and implemented it.

* `KMyMoneyWritableFileImpl`: 
  * Changed interface: Method `createWritableAccount()`: Deprecated old variant without arguments, introduced new variants with arguments.
  * Setting file info "last modified" just before writing out file.
    
* `KMyMoney(Writable)TransactionSplit(Impl)`: 
   * Removed "formatted for HTML" methods -- don't see a real need for it, and even if there was one, then it would belong elsewhere (still thinking of removing the "formatted (without HTML)" methods as well; I see them in the grey area).
   * Renamed methods in writable variant: `setMemo()` instead of `setDescription()` (for consistency with read-only variant).

* Significantly improved overall test coverage, esp. in write-branch.

* Various minor changes, cleaning and improving code.

* Finally applied some minor corrections to the XSD file so that KMyMoney files read and written are valid.

  (Seems obvious, doesn't it? Well, it isn't -- cf. the "known issues" part in the sister project's README file for details.)

### V. 0.5 &rarr; 0.6
* Finally added institutions and dependent code in other classes (e.g. `KMyMoney(Writable)Account(Impl)`).

* `KMyMoney(Writable)Security(Impl)`: New methods `get(Writable)StockAccounts()`, which is a handy short-cut for specific use cases.

* `KMyMoney(Writable)File(Impl)`: Implemented empty skeleton methods that had been forgotten and previously just returned null, such as `getAccountsByName()`.

* Added a few method implementations that had been forgotten here and there.

* Re-iterated over some code, e.g.:
	* `KMyMoney(Writable)Currency(Impl)`
	* `KMyMoneySecurityImpl`
	* `KMyMoney(Writable)FileImpl`
	* Improved overall robustness by more consistently checking method parameters.

* Finally completed list of internal manager classes `File[XYZ]Manager`.

* Minor changes in interfaces.

* Fixed a couple of bugs.

* Some code-cleanup here and there.

* Improved test coverage.

### V. 0.4 &rarr; 0.5
* Extracted some basic packages to new module "Base".

* Clean-up work, most of it under the hood.

* Improved test coverage.

### V. 0.3 &rarr; 0.4
**The** major change here: 

Write access to all supported entities (there are a few unsupported ones  left).

In addition to that:

* More interface methods (get/set).

* Fixed a couple of bugs.

* Renamed class `KMMCurrPair` to `KMMPricePairID`.

### V. 0.2 &rarr; 0.3
First version that you can seriously use.
We'll consider this version the first beta.

However, still only read-access.

### V. 0.1 &rarr; 0.2
(Pre-)Alpha.

## Planned
It should go without saying, but the following points are of course subject to change and by no means a promise that they will actually be implemented soon:

* Add support for other entities (budget, etc.)

* Introduce special variant of transaction: Simple transaction (with just two splits).

* Better test case coverage.

* Last not least: Provide user documentation.

## Known Issues
* Performance: When using the `Writable`-classes (i.e., generating new objects or changing existing ones), the performance is less-than-overwhelming, especially when working with larger files.

* Generating new objects currently only works (reliably) when at least one object of the same type (an institution, say) is already in the file.

* When you generate a price pair that does not exist yet (or a price for a price pair that does not exist yet), then it will be written into the file but not be visible in KMyMoney. In order to make it visible, you first have to generate the according currency.

  *Example*: As in the test data file, your standard currency is EUR, you have one foreign currency defined (USD) and a couple of securities. Now, you generate a price (pair) BRL/EUR (cf. example program `GenPrc`). Then, in KMyMoney, when you go to Tools  &rarr; Prices, this price will not be shown, although it's actually in the file. In order to make it visible, you go to Tools &rarr; Currencies, click the "add" button and add the Brazilian Real as a currency. Then, generated price will be visible.

* `KMyMoneyWritableAccounts`: Getting a list of writable account objects (we have several methods for this) takes very long. Please be patient.
