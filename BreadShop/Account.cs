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
        private Dictionary<int, int> orderIdToOpenOrderQuantity = new Dictionary<int, int>();

        public int GetBalance()
        {
            return balance;
        }
        
        public int Deposit(int creditAmount)
        {
            balance += creditAmount;
            return balance;
        }

        public void AddOrder(int orderId, int amount) {
            orderIdToOpenOrderQuantity.Add(orderId, amount);
        }

        public int CancelOrder(int orderId)
        {
            if (orderIdToOpenOrderQuantity.ContainsKey(orderId))
            {
                int cancelledQuantity = orderIdToOpenOrderQuantity[orderId];
                orderIdToOpenOrderQuantity.Remove(orderId);
                return cancelledQuantity;
            }
            else
            {
                return -1;
            }            
        }
    }
}
