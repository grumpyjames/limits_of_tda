using System;

namespace BreadShopTest
{
    using Microsoft.VisualStudio.TestTools.UnitTesting;   
    using Moq;

    using BreadShop;

    [TestClass]
    public class BreadShopTest
    {
        private readonly int accountIdOne = 1;
        private readonly int accountIdTwo = 2;
        private readonly int orderIdOne = 1;
        private readonly int orderIdTwo = 2;

        private readonly Mock<OutboundEvents> mockEvents;
        private readonly BreadShop breadShop;

        public BreadShopTest()
        {
            this.mockEvents = new Mock<OutboundEvents>();
            this.breadShop = new BreadShop(mockEvents.Object);
        }

        [TestMethod]
        public void create_an_account() {            
            breadShop.CreateAccount(accountIdOne);
            mockEvents.Verify(events => events.AccountCreatedSuccessfully(accountIdOne));
        }

        [TestMethod]
        public void deposit_some_money() {
            breadShop.CreateAccount(accountIdOne);

            int depositAmount = 300;            
            breadShop.Deposit(accountIdOne, depositAmount);

            mockEvents.Verify(events => events.NewAccountBalance(accountIdOne, depositAmount));
        }

        [TestMethod]
        public void reject_deposits_for_nonexistent_accounts() {
            int nonExistentAccountId = -5;            
            breadShop.Deposit(nonExistentAccountId, 4000);

            mockEvents.Verify(events => events.AccountNotFound(nonExistentAccountId));
        }

        [TestMethod]
        public void deposits_add_up() {
            CreateAccountWithBalance(accountIdOne, 300);
            
            breadShop.Deposit(accountIdOne, 300);
            mockEvents.Verify(events => events.NewAccountBalance(accountIdOne, 300 + 300));
        }

        [TestMethod]
        public void place_an_order_succeeds_if_there_is_enough_money() {
            CreateAccountWithBalance(accountIdOne, 500);

            breadShop.PlaceOrder(accountIdOne, orderIdOne, 40);
            mockEvents.Verify(events => events.OrderPlaced(accountIdOne, 40));
            mockEvents.Verify(events => events.NewAccountBalance(accountIdOne, 500 - Cost(40)));
        }

        [TestMethod]
        public void cannot_place_order_for_nonexistent_account() {            
            breadShop.PlaceOrder(-5, orderIdOne, 40);
            mockEvents.Verify(events => events.AccountNotFound(-5));
        }

        [TestMethod]
        public void cannot_place_an_order_for_more_than_account_can_afford() {
            CreateAccountWithBalance(accountIdOne, 500);

            // 42 * 12 = 504            
            breadShop.PlaceOrder(accountIdOne, orderIdOne, 42);
            mockEvents.Verify(events => events.OrderRejected(accountIdOne));
        }

        [TestMethod]
        public void cancel_an_order_by_id() {
            int balance = 500;
            CreateAccountWithBalance(accountIdOne, balance);

            int amount = 40;
            breadShop.PlaceOrder(accountIdOne, orderIdOne, amount);

            breadShop.CancelOrder(accountIdOne, orderIdOne);
            mockEvents.Verify(events => events.OrderCancelled(accountIdOne, orderIdOne));
            mockEvents.Verify(events => events.NewAccountBalance(accountIdOne, balance));
        }

        [TestMethod]
        public void cannot_cancel_an_order_for_nonexistent_account() {            
            breadShop.CancelOrder(-5, orderIdOne);

            mockEvents.Verify(events => events.AccountNotFound(-5));
        }

        [TestMethod]
        public void cannot_cancel_a_nonexistent_order() {
            breadShop.CreateAccount(accountIdOne);
            
            breadShop.CancelOrder(accountIdOne, -5);
            mockEvents.Verify(events => events.OrderNotFound(accountIdOne, -5));
        }

        [TestMethod]
        public void cancelling_an_allows_balance_to_be_reused() {
            int balance = 500;
            CreateAccountWithBalance(accountIdOne, balance);

            int amount = 40;
            breadShop.PlaceOrder(accountIdOne, orderIdOne, amount);
            breadShop.CancelOrder(accountIdOne, orderIdOne);

            // it's entirely possible that the balance in the resulting event doesn't match the internal
            // state of the system, so we ensure the balance has really been restored
            // by trying to place a new order with it.
            breadShop.PlaceOrder(accountIdOne, orderIdTwo, amount);
            mockEvents.Verify(events => events.OrderPlaced(accountIdOne, amount));
            mockEvents.Verify(events => events.NewAccountBalance(accountIdOne, balance - Cost(amount)));
        }

        [TestMethod]
        [Ignore] // tests Objective A
        public void an_empty_shop_places_an_empty_wholesale_order() {
            breadShop.PlaceWholesaleOrder();

            mockEvents.Verify(events => events.PlaceWholesaleOrder(0));
        }

        [TestMethod]
        [Ignore] // tests Objective A
        public void wholesale_orders_are_made_for_the_sum_of_the_quantities_of_outstanding_orders_in_one_account() {
            int balance = Cost(40 + 55);
            CreateAccountWithBalance(accountIdOne, balance);
            breadShop.PlaceOrder(accountIdOne, orderIdOne, 40);
            breadShop.PlaceOrder(accountIdOne, orderIdTwo, 55);

            breadShop.PlaceWholesaleOrder();
            mockEvents.Verify(events => events.PlaceWholesaleOrder(40 + 55));
        }

        [TestMethod]
        [Ignore] // tests Objective A
        public void wholesale_orders_are_made_for_the_sum_of_the_quantities_of_outstanding_orders_across_accounts() {
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, 40);
            CreateAccountAndPlaceOrder(accountIdTwo, orderIdTwo, 55);

            breadShop.PlaceWholesaleOrder();
            mockEvents.Verify(events => events.PlaceWholesaleOrder(40 + 55));            
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void arrival_of_wholesale_order_trigger_fills_of_a_single_outstanding_order() {
            int quantity = 40;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

            breadShop.OnWholesaleOrder(quantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantity));            
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void wholesale_order_quantities_might_only_fill_an_outstanding_order_partially() {
            int quantity = 40;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

            int wholesaleOrderQuantity = quantity / 2;            
            breadShop.OnWholesaleOrder(wholesaleOrderQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity));                        
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void an_order_can_be_filled_by_two_consecutive_wholesale_orders() {
            int quantity = 40;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);

            int wholesaleOrderQuantity = quantity / 2;            
            breadShop.OnWholesaleOrder(wholesaleOrderQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity));
            
            breadShop.OnWholesaleOrder(wholesaleOrderQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantity));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void orders_do_not_overfill() {
            int quantity = 40;
            int wholesaleOrderQuantity = 42;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);
            
            breadShop.OnWholesaleOrder(wholesaleOrderQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantity));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void fully_filled_orders_are_removed_and_therefore_cannot_be_cancelled()
        {
            int quantity = 40;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);
            
            breadShop.OnWholesaleOrder(quantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantity));
            
            breadShop.CancelOrder(accountIdOne, orderIdOne);
            mockEvents.Verify(events => events.OrderNotFound(accountIdOne, orderIdOne));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void orders_do_not_overfill_across_two_wholesale_orders() {
            int quantity = 40;
            int wholesaleOrderQuantityOne = 21;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantity);
            
            breadShop.OnWholesaleOrder(wholesaleOrderQuantityOne);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, wholesaleOrderQuantityOne));

            int wholesaleOrderQuantityTwo = 33; // This is more than the remaining quantity, so should fully fill the remainder
            breadShop.OnWholesaleOrder(wholesaleOrderQuantityTwo);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantity - wholesaleOrderQuantityOne));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void orders_across_different_accounts_are_filled() {
            int quantityOne = 40;
            int quantityTwo = 55;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantityOne);
            CreateAccountAndPlaceOrder(accountIdTwo, orderIdTwo, quantityTwo);
            
            breadShop.OnWholesaleOrder(quantityOne + quantityTwo);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantityOne));
            mockEvents.Verify(events => events.OrderFilled(accountIdTwo, orderIdTwo, quantityTwo));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void orders_fill_in_a_consistent_order_across_different_accounts() {
            int quantityOne = 40;
            int quantityTwo = 55;
            CreateAccountAndPlaceOrder(accountIdOne, orderIdOne, quantityOne);
            CreateAccountAndPlaceOrder(accountIdTwo, orderIdTwo, quantityTwo);

            
            int secondFillQuantity = 8;            
            breadShop.OnWholesaleOrder(quantityOne + secondFillQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantityOne));
            mockEvents.Verify(events => events.OrderFilled(accountIdTwo, orderIdTwo, secondFillQuantity));
        }

        [TestMethod]
        [Ignore] // tests Objective B
        public void orders_fill_in_a_consistent_order_across_orders_in_the_same_account() {
            int quantityOne = 40;
            int quantityTwo = 50;
            int balance = Cost(quantityOne) + Cost(quantityTwo);
            CreateAccountWithBalance(accountIdOne, balance);
            breadShop.PlaceOrder(accountIdOne, orderIdOne, quantityOne);
            breadShop.PlaceOrder(accountIdOne, orderIdTwo, quantityTwo);
            
            int secondFillQuantity = 8;

            breadShop.OnWholesaleOrder(quantityOne + secondFillQuantity);
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdOne, quantityOne));            
            mockEvents.Verify(events => events.OrderFilled(accountIdOne, orderIdTwo, secondFillQuantity));
        }

        private int Cost(int quantityOne)
        {
            return quantityOne * 12;
        }

        private void CreateAccountWithBalance(int accountId, int initialBalance)
        {
            breadShop.CreateAccount(accountId);
            breadShop.Deposit(accountId, initialBalance);
        }

        private void CreateAccountAndPlaceOrder(int accountId, int orderId, int amount)
        {
            int balance = Cost(amount);
            CreateAccountWithBalance(accountId, balance);
            breadShop.PlaceOrder(accountId, orderId, amount);
        }
    }
}
