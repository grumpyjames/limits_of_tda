using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BreadShop
{
    public class BreadShop
    {
        private static int PRICE_OF_BREAD = 12;

        private readonly OutboundEvents events;
        private readonly AccountRepository accountRepository = new AccountRepository();

        public BreadShop(OutboundEvents outboundEvents)
        {
            this.events = outboundEvents;
        }

        public void CreateAccount(int id)
        {
            Account newAccount = new Account();
            accountRepository.AddAccount(id, newAccount);
            events.AccountCreatedSuccessfully(id);
        }
        
        public void Deposit(int accountId, int creditAmount) {
            try
            {
                Account account = accountRepository.GetAccount(accountId);
                int newBalance = account.Deposit(creditAmount);
                events.NewAccountBalance(accountId, newBalance);
            }
            catch (KeyNotFoundException)
            {
                events.AccountNotFound(accountId);
            }            
        }

        public void PlaceOrder(int accountId, int orderId, int amount) {
            try
            { 
                Account account = accountRepository.GetAccount(accountId);                
                int cost = amount * PRICE_OF_BREAD;
                if (account.GetBalance() >= cost) {
                    account.AddOrder(orderId, amount);
                    int newBalance = account.Deposit(-cost);
                    events.OrderPlaced(accountId, amount);
                    events.NewAccountBalance(accountId, newBalance);
                } else {
                    events.OrderRejected(accountId);
                }
            }
            catch (KeyNotFoundException)
            {
                events.AccountNotFound(accountId);
            }
        }

        public void CancelOrder(int accountId, int orderId) {
            try
            {
                Account account = accountRepository.GetAccount(accountId);                

                int cancelledQuantity = account.CancelOrder(orderId);
                if (cancelledQuantity == -1)
                {
                    events.OrderNotFound(accountId, orderId);
                    return;
                }

                int newBalance = account.Deposit(cancelledQuantity * PRICE_OF_BREAD);
                events.OrderCancelled(accountId, orderId);
                events.NewAccountBalance(accountId, newBalance);
            }
            catch (KeyNotFoundException)
            {
                events.AccountNotFound(accountId);
            }
        }

        public void PlaceWholesaleOrder() {
            throw new InvalidOperationException("Implement me in Objective A");
        }

        public void OnWholesaleOrder(int quantity) {
            throw new InvalidOperationException("Implement me in Objective B");
        }
    }
}
