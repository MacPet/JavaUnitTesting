package org.example.springboot;

import org.example.newAccountRegistry;
import org.example.PrivateAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountControllerTest {

    AccountController controller = new AccountController();
    PrivateAccount testAccount = new PrivateAccount("a", "a", "12312312312");
    double saldoBeforeTransfer = testAccount.getBalance();

    @BeforeEach
    void setUp(){
        newAccountRegistry.clear();
    }

    @Test
    void checkGetAmount(){
     assertEquals(0, controller.numberOfAccounts());
        newAccountRegistry.add(testAccount);
     assertEquals(1, controller.numberOfAccounts());
    }

    @Test
    void checkGet404(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.findAccount("this is not a real pesel >:)"));
        assertEquals("404 NOT_FOUND", exception.getMessage());
    }

    @Test
    void checkGet(){
        newAccountRegistry.add(testAccount);
        assertEquals(testAccount.getName(), controller.findAccount(testAccount.getPesel()).getName());
    }

    @Test
    void checkPost(){
    controller.newAccount(testAccount);
    List<PrivateAccount> accountsOfSamePesel = newAccountRegistry.getByPesel(testAccount.getPesel());
    PrivateAccount lastAccount = accountsOfSamePesel.get(accountsOfSamePesel.size() - 1);

    assertEquals(lastAccount.getName(), testAccount.getName(), "Controller POST function didnt add an account to registry");
    assertEquals(lastAccount.getName(), testAccount.getSurname(), "Controller POST function didnt add an account to registry");


    }

    @Test
    void checkDelete(){

     newAccountRegistry.add(testAccount);

     controller.removeAccount(testAccount.getPesel());
     assertEquals(0, newAccountRegistry.getLength(), "Controller function removeAccount() didnt remove the account from registry");
    }


    @Test
    void transferCantAcceptNegativeNumbers(){

        newAccountRegistry.add(testAccount);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.transferMoney(new Transfer("incoming", -1), testAccount.getPesel()));

        assertEquals("400 BAD_REQUEST", exception.getMessage());

    }

    @Test
    void transferMustBeOfAcceptableType(){

        newAccountRegistry.add(testAccount);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.transferMoney(new Transfer("msakldjdsknd", 1), testAccount.getPesel()));

        assertEquals("400 BAD_REQUEST", exception.getMessage());
    }

    @Test
    void transferAccountMustExist(){
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.transferMoney(new Transfer("incoming", 1), "00000000404"));

        assertEquals("404 NOT_FOUND", exception.getMessage());
    }


    @Test
    void transferAccountMustContainEnoughMoney(){

        newAccountRegistry.add(testAccount);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.transferMoney(new Transfer("outgoing", 10000000), testAccount.getPesel()));

        assertEquals("422 UNPROCESSABLE_ENTITY", exception.getMessage());
    }

    @Test
    void transferAccountMustContainEnoughMoneyExpress(){

        newAccountRegistry.add(testAccount);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.transferMoney(new Transfer("express", 10000000), testAccount.getPesel()));

        assertEquals("422 UNPROCESSABLE_ENTITY", exception.getMessage());
    }

    @Test
    void transferIncomingMustWork(){

        newAccountRegistry.add(testAccount);


        controller.transferMoney(new Transfer("incoming", 1000), testAccount.getPesel());
        PrivateAccount updatedAccount = newAccountRegistry.getByPesel(testAccount.getPesel()).get(0);
        assertEquals(saldoBeforeTransfer + 1000, updatedAccount.getBalance());


    }

    @Test
    void transferOutgoingMustWork(){

        testAccount.setBalance(10);
        newAccountRegistry.add(testAccount);


        controller.transferMoney(new Transfer("outgoing", 10), testAccount.getPesel());
        PrivateAccount updatedAccount = newAccountRegistry.getByPesel(testAccount.getPesel()).get(0);
        assertEquals(0, updatedAccount.getBalance());

    }

    @Test
    void transferExpressgMustWork(){

        testAccount.setBalance(10);
        newAccountRegistry.add(testAccount);


        controller.transferMoney(new Transfer("express", 10), testAccount.getPesel());
        PrivateAccount updatedACccount = newAccountRegistry.getByPesel(testAccount.getPesel()).get(0);
        assertEquals(-testAccount.getExpressTransferCost(), updatedACccount.getBalance());

    }

}