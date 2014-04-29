# BreadShop - Background

> What's that got to do with the price of bread?
>> Unattributed

After the huge success of the Apple store, our investors have decided to create the Bread Shop.

They require a piece of software that allows customers to:

* open an account
* deposit money
* order bread
* cancel existing orders
* make wholesale bread orders based on customer demand
* 'fill' bread orders when a wholesale delivery arrives

Undoubtedly other requirements will arrive, but this is the basic set.

Currently only the first four requirements have been met.

# Objectives

While we are fundamentally interested in the success of the bread shop, we are also consultants, and we need some material for our new blog posts. As such, we have decided to adopt a new programming paradigm and explore the limits of tell don't ask.

# Tell don't ask - a rather extreme definition

A given function call is TDA iff no information is returned to the caller from the callee.

This means, roughly:

* No return types
* No passing objects that have any functions with return types
* No circular call paths

Handily, the existing code already has a TDA like external interface; all of `BreadShop`s public methods obey the rule, and the only information returned is through the `OutboundEvents` interface.

N.B Currently, only primitive(ish) types are poked into OutboundEvents. There's nothing to stop us using real objects, but we might find it trickier to write tests if you do...

## Objective 1

Make the internals (i.e all code in `src/main/java`) obey the TDA rule.

Hints:
While we shouldn't pass things like `List` and `Map` between our objects, it's totally fine for them to be implemented in terms of them.

It's also completely OK to pass non-primitive objects around, but all of their public methods must obey TDA.

## Objective 2 : We can bake it for you wholesale

Add a function to the `BreadShop` that behaves as follows:

* Takes no arguments
* Calls a new `placeWholesaleOrder` on `OutboundEvents` with an integer amount that is equal to the `sum` of all the orders in all of the known accounts

Remember to stay within the rules of TDA!

## Objective 3 : Choose your own adventure.

At this point, you might have a good idea of a story that would be hard to express within the TDA rules. Perhaps have a go at implementing it, if so.

If nothing immediately occurs to you, try one of the following requirements:

a.) The bread arrives

Add a function to the `BreadShop` named `onWholesaleOrder`. This would be called when the wholesale order arrives. It takes a single argument - the size of the wholesale order.

It should:

* Attempt to 'fill' as many orders as possible
* For each order that is filled, a call to a new OutboundEvents function `onOrderFilled(int accountId, int orderId, int amount)` should be made
* Filled orders should be removed from the system (so if another wholesale order arrived, they won't get filled twice)

It is perfectly valid for orders to be placed or cancelled between a call to `placeBatchOrder` and `onWholesaleOrder`. Dealing with this is outside of the scope of this story, but if you've time to spare, why not have a go?