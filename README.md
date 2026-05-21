# Notes on the Module "API"

## What Does It Do?

This is the core module of the project, providing all low-level read-/write access functions to a 
KMyMoney 
file.

## What is This Repo's Relationship with the Other Repos?

* This is a module-level repository which is part of a multi-module project, i.e. it has a parent and several siblings. 

  [Parent](https://github.com/jross765/JKMyMoneyLibNTools.git)

* Under normal circumstances, you cannot compile it on its own (at least not without further preparation), but instead, you should clone it together with the other repos and use the parent repo's build-script.

* This repository contains no history before V. 0.8 (cf. notes in parent repo).

## Major Changes

Cf. document "[Major Changes](https://github.com/jross765/JKMyMoneyLibNTools/kmymoney-api/major_changes.md)".

## Planned
It should go without saying, but the following points are of course subject to change and by no means a promise that they will actually be implemented soon:

* Add support for other entities (budget, etc.)

* Better test case coverage.

* Last not least: Provide user documentation.

## Known Issues
* Performance: When using the `Writable`-classes (i.e., generating new objects or changing existing ones), the performance is less-than-overwhelming, especially when working with larger files.

* *Edge case*: The "virgin" test file (the practically empty one) cannot be parsed -- in the current stage of development, the lib still assumes a handful of entities being already there.

* *Edge case*: Generating new objects currently only works (reliably) when at least one object of the same type (an institution, say) is already in the file.

* When you generate a price pair that does not exist yet (or a price for a price pair that does not exist yet), then it will be written into the file but not be visible in KMyMoney. In order to make it visible, you first have to generate the according currency.

  *Example*: As in the test data file, your standard currency is EUR, you have one foreign currency defined (USD) and a couple of securities. Now, you generate a price (pair) BRL/EUR (cf. example program `GenPrc`). Then, in KMyMoney, when you go to Tools  &rarr; Prices, this price will not be shown, although it's actually in the file. In order to make it visible, you go to Tools &rarr; Currencies, click the "add" button and add the Brazilian Real as a currency. Then, generated price will be visible.

* `KMyMoneyWritableAccounts`: Getting a list of writable account objects (we have several methods for this) takes very long. Please be patient.
