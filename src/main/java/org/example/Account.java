package org.example;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;


@Data
@NoArgsConstructor
public class Account {

    double[] history = {};
    double balance = 0;
    public double getExpressTransferCost(){
    return 0;
    }

    public Account(double balance, double[] history){
    this.balance = balance;
    this.history = history;
    }

    public void transfer(Account account2, double transferAmount) {
    if(transferAmount > this.balance){
        System.out.println("Transfer failed, transfer amount cant be bigger than transfer balance.");
    }
    else{

        this.setBalance(this.balance - transferAmount);
        account2.setBalance(account2.balance + transferAmount);

        this.addToHistory(-1 * transferAmount);

        account2.addToHistory(transferAmount);
    }

    }

    public void transferExpress(Account account2, double transferAmount) {

   if(transferAmount <= balance)
        {
            this.setBalance(balance - transferAmount - getExpressTransferCost());
            account2.setBalance(account2.getBalance() + transferAmount);
            this.addToHistory(-1 * transferAmount);
            this.addToHistory(-1 * getExpressTransferCost());

            account2.addToHistory(transferAmount);
        }

        this.setBalance(this.balance);

    }

    public void addToHistory(double kwota){

           if(Math.abs(kwota) != 0){
               this.history = Arrays.copyOf(history, history.length + 1);
               history[history.length - 1] = kwota;
           }

    }


    public void clearHistory() {
        this.history = new double[]{};
    }

    public boolean mailHistory(String mail, SMTPClient client){
        //Imperfect regex, a true email regex is too complicated for me to write
        String verifyMailRegex = "$[a-zA-Z.]@[a-zA-Z]{1,}[.]{0,1}[a-zA-Z]{0,}.[a-zA-Z]{1,}^";

        if(!mail.matches(verifyMailRegex)){
        return false;
        }

        String mailSubject = "" + LocalDate.now();
        String mailText = "Transaction history of your bank account: " + Arrays.toString(history);

        //not a real functionality
        return client.send(mailSubject, mailText, mail);
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Account account = (Account) object;
        return balance == account.getBalance()
                && Arrays.equals(history, account.getHistory());

    }
}
