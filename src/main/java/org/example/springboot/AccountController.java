package org.example.springboot;

import org.example.PrivateAccount;
import org.example.newAccountRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
public class AccountController {

    AccountController() {}

//<editor-fold desc="Helpers>

    void isEmptyCheck(List<PrivateAccount> list) throws ResponseStatusException {

        if (list.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND //404
            );
        }

    }



//</editor-fold>


    @GetMapping("/accounts/count")
    Integer numberOfAccounts() {
        return newAccountRegistry.getLength();
    }

    @GetMapping("/accounts/{pesel}")
    PrivateAccount findAccount(@PathVariable String pesel) {
        List<PrivateAccount> accountList = newAccountRegistry.getByPesel(pesel);

        isEmptyCheck(accountList);

        return accountList.get(0);

    }

    @PostMapping("/accounts")
    void newAccount(@RequestBody PrivateAccount newAccount) {
        newAccountRegistry.add(newAccount);
        System.out.println(newAccount.getName());
    }

    @DeleteMapping("/accounts/{pesel}")
    void removeAccount(@PathVariable String pesel) {
        newAccountRegistry.removeByPesel(pesel);
    }


    @PostMapping("/accounts/{pesel}/transfer")
    void transferMoney(@RequestBody Transfer transferBody, @PathVariable String pesel) {

        List<PrivateAccount> accountList = newAccountRegistry.getByPesel(pesel);

        isEmptyCheck(accountList);

        PrivateAccount account = accountList.get(0);

        if (!Transfer.ACCEPTABLE_TYPES.contains(transferBody.type) || transferBody.amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //400
        }



        PrivateAccount TEMPORARY_ACCOUNT = new PrivateAccount("a", "a", "99999999999");
        double moneyBeforeTransfer = account.getBalance();

        switch (transferBody.type) {
            case "outgoing" -> {
                account.transfer(TEMPORARY_ACCOUNT, transferBody.amount);
                if (account.getBalance() == moneyBeforeTransfer)
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            case "express" -> {
                account.transferExpress(TEMPORARY_ACCOUNT, transferBody.amount);
                if (account.getBalance() == moneyBeforeTransfer)
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            case "incoming" -> account.setBalance(account.getBalance() + transferBody.amount);
        }


        newAccountRegistry.update(account);
    }



}