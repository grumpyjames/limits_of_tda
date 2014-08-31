using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BreadShop
{
    public class Account
    {
        private int balance = 0;
        private readonly int id;
        private Dictionary<int, int> orderIdToOpenOrderQuantity = new Dictionary<int, int>();

        public Account(int id)
        {
            this.id = id;
        }

        public void Deposit(int creditAmount, OutboundEvents events)
        {
            balance += creditAmount;
            events.NewAccountBalance(id, balance);
        }

        public void PlaceOrder(int orderId, int amount, int price, OutboundEvents events)
        {
            int cost = price * amount;
            if (balance >= cost)
            {
                DoPlaceOrder(orderId, amount, events);
                Deposit(-cost, events);
            }
            else
            {
                events.OrderRejected(id);
            }
        }

        public void CancelOrder(int orderId, int price, OutboundEvents events)
        {
            if (orderIdToOpenOrderQuantity.ContainsKey(orderId))
            {
                int cancelledQuantity = DoCancelOrder(orderId, events);
                Deposit(cancelledQuantity * price, events);
            }
            else
            {
                events.OrderNotFound(id, orderId);
            }
        }

        public void VisitOrders(Action<int> orderQuantityAction)
        {
            orderQuantityAction(orderIdToOpenOrderQuantity.Values.Sum());
        }

        private void DoPlaceOrder(int orderId, int amount, OutboundEvents events)
        {
            orderIdToOpenOrderQuantity.Add(orderId, amount);
            events.OrderPlaced(id, amount);
        }

        private int DoCancelOrder(int orderId, OutboundEvents events)
        {
            int cancelledQuantity = orderIdToOpenOrderQuantity[orderId];
            orderIdToOpenOrderQuantity.Remove(orderId);
            events.OrderCancelled(id, orderId);
            return cancelledQuantity;
        }

        internal void FillOrders(int quantity, OutboundEvents events, IEnumerator<Account> remainingAccounts,
                                 Action<int, IEnumerator<Account>, OutboundEvents> next)
        {
            int remainingQuantity = quantity;
            Dictionary<int, int> remainingOrders = new Dictionary<int, int>();
            foreach (KeyValuePair<int, int> order in orderIdToOpenOrderQuantity)
            {
                if (order.Value <= remainingQuantity)
                {
                    events.OrderFilled(id, order.Key, order.Value);
                    remainingQuantity -= order.Value;
                }
                else
                {
                    if (remainingQuantity != 0)
                    {
                        events.OrderFilled(id, order.Key, remainingQuantity);
                        remainingOrders.Add(order.Key, order.Value - remainingQuantity);
                        remainingQuantity = 0;
                    }
                    else
                    {
                        remainingOrders.Add(order.Key, order.Value);
                    }
                }
            }

            orderIdToOpenOrderQuantity = remainingOrders;
            next(remainingQuantity, remainingAccounts, events);
        }
    }
}
