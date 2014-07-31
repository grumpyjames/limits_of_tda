using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BreadShop
{
    public class AccountRepository
    {
        private readonly Dictionary<int, Account> accounts = new Dictionary<int,Account>();

        public void AddAccount(int accountId, Account account)
        {
            accounts.Add(accountId, account);
        }

        public Account GetAccount(int accountId)
        {
            return accounts[accountId];
        }
    }
}
