using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using System.Threading.Tasks;

namespace BreadShop
{
    public class AccountRepository
    {
        private readonly Dictionary<int, Account> accounts = new Dictionary<int, Account>();
        private readonly OutboundEvents events;

        public AccountRepository(OutboundEvents events)
        {
            this.events = events;
        }

        public void AddAccount(int accountId, Account account)
        {
            accounts.Add(accountId, account);
            events.AccountCreatedSuccessfully(accountId);
        }

        public void OnAccountDo(int accountId, Action<Account> action)
        {
            if (accounts.ContainsKey(accountId))
            {
                action(accounts[accountId]);
            }
            else
            {
                events.AccountNotFound(accountId);
            }
        }

        public void OnAccountsDo(Action<Account> action)
        {
            foreach (Account account in accounts.Values)
            {
                action(account);
            }
        }
    }
}
