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
        private readonly Dictionary<int, int> orderIdToOpenOrderQuantity = new Dictionary<int, int>();

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
    }
}
