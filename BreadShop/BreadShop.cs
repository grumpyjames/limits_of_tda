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
        private readonly AccountRepository accountRepository;

        public BreadShop(OutboundEvents outboundEvents)
        {
            this.events = outboundEvents;
            this.accountRepository = new AccountRepository(outboundEvents);
        }

        public void CreateAccount(int id)
        {
            Account newAccount = new Account(id);
            accountRepository.AddAccount(id, newAccount);            
        }
        
        public void Deposit(int accountId, int creditAmount) {
            accountRepository.OnAccountDo(accountId, account => account.Deposit(creditAmount, events));            
        }

        public void PlaceOrder(int accountId, int orderId, int amount) {
            accountRepository.OnAccountDo(accountId, account => account.PlaceOrder(orderId, amount, PRICE_OF_BREAD, events));            
        }

        public void CancelOrder(int accountId, int orderId) {
            accountRepository.OnAccountDo(accountId, account => account.CancelOrder(orderId, PRICE_OF_BREAD, events));            
        }

        public void PlaceWholesaleOrder() {
            WholesaleOrderAccumulator accumulator = new WholesaleOrderAccumulator();
            accountRepository.OnAccountsDo(account => account.VisitOrders(orderQty => accumulator.AddQuantity(orderQty)));

            accumulator.PlaceOrder(events);
        }

        public void OnWholesaleOrder(int quantity) {
            accountRepository.OnWholesaleOrder(quantity);
        }

        private class WholesaleOrderAccumulator
        {
            private int wholesaleOrderQuantity = 0;

            public void AddQuantity(int orderQuantity)
            {
                wholesaleOrderQuantity += orderQuantity;
            }

            public void PlaceOrder(OutboundEvents events)
            {
                events.PlaceWholesaleOrder(wholesaleOrderQuantity);
            }
        }
    }
}
