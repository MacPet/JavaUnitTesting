package org.example.springboot;

import org.example.KontoOsobiste;
import org.example.newAccountRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
public class AccountController {

    AccountController() {}

//<editor-fold desc="Helpers>

    void isEmptyCheck(List<KontoOsobiste> list) throws ResponseStatusException {

        if (list.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND //404
            );
        }

    }



//</editor-fold>


    //GETY
    @GetMapping("/accounts/count")
    Integer numberOfAccounts() {
        return newAccountRegistry.getLength();
    }

    @GetMapping("/accounts/{pesel}")
    KontoOsobiste findAccount(@PathVariable String pesel) {
        List<KontoOsobiste> accountList = newAccountRegistry.getByPesel(pesel);

        isEmptyCheck(accountList);

        return accountList.get(0);

    }

    @PostMapping("/accounts")
    void newAccount(@RequestBody KontoOsobiste newAccount) {
        newAccountRegistry.add(newAccount);
        System.out.println(newAccount.getImie());
    }

    @DeleteMapping("/accounts/{pesel}")
    void removeAccount(@PathVariable String pesel) {
        newAccountRegistry.removeByPesel(pesel);
    }


    @PostMapping("/accounts/{pesel}/transfer")
    void transferMoney(@RequestBody Transfer transferBody, @PathVariable String pesel) {

        List<KontoOsobiste> accountList = newAccountRegistry.getByPesel(pesel);

        isEmptyCheck(accountList);

        KontoOsobiste account = accountList.get(0);

        if (!Transfer.ACCEPTABLE_TYPES.contains(transferBody.type) || transferBody.amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST); //400
        }



        KontoOsobiste TEMPORARY_ACCOUNT = new KontoOsobiste("a", "a", "99999999999");
        double moneyBeforeTransfer = account.getSaldo();

        switch (transferBody.type) {
            case "outgoing" -> {
                account.przelew(TEMPORARY_ACCOUNT, transferBody.amount);
                if (account.getSaldo() == moneyBeforeTransfer)
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            case "express" -> {
                account.przelewEkspresowy(TEMPORARY_ACCOUNT, transferBody.amount);
                if (account.getSaldo() == moneyBeforeTransfer)
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            case "incoming" -> account.setSaldo(account.getSaldo() + transferBody.amount);
        }


        newAccountRegistry.update(account);
    }



}