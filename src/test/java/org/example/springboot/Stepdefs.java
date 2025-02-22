package org.example.springboot;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.example.*;
import org.example.newAccountRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class Stepdefs {




    public static class accountRegistryFeatures {

    @Given("Number of accounts in registry equals: {string}")
    public void numberOfAccountsInRegistryEquals(String expectedAmount) {
        int count = newAccountRegistry.getLength();
        assertEquals(Integer.parseInt(expectedAmount), count);
    }

    @When("I create an account using name: {string}, last name: {string}, pesel: {string}")
    public void iCreateAnAccountUsingNameLastNamePesel(String name, String lastName, String pesel) {
        PrivateAccount kontoOsobiste = new PrivateAccount(name, lastName, pesel, "", 0, new double[]{}, null);
        newAccountRegistry.add(kontoOsobiste);
    }

    @And("Account with pesel {string} exists in registry")
    public void accountWithPeselExistsInRegistry(String pesel) {
        assertNotNull(newAccountRegistry.getByPesel(pesel));
    }

    @When("I update {string} of account with pesel: {string} to {string}")
    public void iUpdateOfAccountWithPeselTo(String key, String pesel, String newValue) {

        PrivateAccount kontoOsobiste = newAccountRegistry.getByPesel(pesel).get(0);
        assertNotNull(kontoOsobiste);

        switch (key) {
            case "name" -> {
                kontoOsobiste.setName(newValue);
                assertEquals( newValue, kontoOsobiste.getName(),"Name was supposed to be modified but wasnt.");}
            case "surname" -> {
                kontoOsobiste.setSurname(newValue);
                 assertEquals( newValue, kontoOsobiste.getSurname(),"Name was supposed to be modified but wasnt.");}
            default -> fail("Unrecognized value passed");

        }
        newAccountRegistry.update(kontoOsobiste);
    }

    @Then("Account with pesel {string} has {string} equal to {string}")
    public void accountWithPeselHasEqualTo(String pesel, String key, String value) {


        List<PrivateAccount> kontaOsobiste = newAccountRegistry.getByPesel(pesel);
        PrivateAccount konto1 = kontaOsobiste.get(0);
        PrivateAccount konto2 = kontaOsobiste.get(1);
        assertNotEquals(0, kontaOsobiste.size());


        switch (key) {
            case "name" -> assertTrue(konto1.getName().equals(value) || konto2.getName().equals(value),"Name wasnt equal given value.");
            case "surname" -> assertTrue(konto1.getSurname().equals(value) || konto2.getSurname().equals(value),"Name wasnt equal given value.");
            default -> fail("Unrecognized value passed into test");}

    }

    @When("I delete account with pesel: {string}")
    public void iDeleteAccountWithPesel(String pesel) {
    newAccountRegistry.removeByPesel(pesel);
    }

    @Then("Account with pesel {string} does not exist in registry")
    public void accountWithPeselDoesNotExistInRegistry(String pesel) {
        assertEquals(0, newAccountRegistry.getByPesel(pesel).size());
    }


    @Given("Number of accounts with pesel {string} is one")
    public void numberOfAccountsWithPeselEquals(String pesel) {

        PrivateAccount kontoOsobiste = new PrivateAccount("ala", "bala", pesel);
        newAccountRegistry.add(kontoOsobiste);
        assertNotNull(newAccountRegistry.getByPesel(pesel));

    }

    @And("Number of accounts with pesel {string} equals {string}")
    public void numberOfAccountsWithPeselEquals(String pesel, String expectedAmount) {
        assertEquals(Integer.parseInt(expectedAmount), newAccountRegistry.getByPesel(pesel).size());
    }

    @When("I call the clear method")
    public void iCallTheClearMethod() {
        newAccountRegistry.clear();
    }

    }

    public static class moneyTransferFeatures{
        PrivateAccount testingAccount =
                new PrivateAccount("test", "test", "12332112332", "BRAK_KODU");

        PrivateAccount testingAccount2 =
                new PrivateAccount("test", "test", "12332112333", "BRAK_KODU");

//
//        @Given("Account's history is empty")
//        public void accountSHistoryIsEmpty() {
//        assertEquals(0, testingAccount.getHistoria().length);
//        }


        @Given("Account has {double} zloty")
        public void accountHasZloty(double money) {
        testingAccount.setBalance(money);
        assertEquals(money, testingAccount.getBalance());
        }

        @Then("Different account has {double} zloty")
        public void differentAccountHasZloty(double money) {
            assertEquals(money, testingAccount2.getBalance());
        }


        @When("User transfers {double} zloty to a different account")
        public void userTransferToDifferentAccount(double amount) {
        testingAccount.transfer(testingAccount2, amount);
        }


        @When("User express transfers {double} zloty to a different account")
        public void userExpressTransferToDifferentAccount(double amount) {
            testingAccount.transferExpress(testingAccount2, amount);
        }


        @Then("I can borrow {string}")
        public void iCanBorrow(String money){
        double startingMoney = testingAccount.getBalance();
        double loanAmount = Double.parseDouble(money);
        testingAccount.loan(loanAmount);
        assertEquals(startingMoney + loanAmount, testingAccount.getBalance());
        }

        @Then("Last entry in account's history is {double}")
        public void lastEntryInAccountSHistoryIs(double lastValue) {
        double[] history = testingAccount.getHistory();
        assertEquals(lastValue, history[history.length - 1]);

        }






    }


}