package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;


import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;

class AccountTest {

    //<editor-fold desc="Variables and data streams">
    public SMTPClient buildBoolSpy(boolean result) {

        SMTPClient temporaryClient = new SMTPClient();
        SMTPClient spyClient = Mockito.spy(temporaryClient);

        Mockito.doAnswer(invocation -> result).when(spyClient).send(anyString(), anyString(), anyString());

        return spyClient;

    }

    //<editor-fold desc="Variables and data streams">
    Account account = new Account();

    SMTPClient SMPTSpyTrue = buildBoolSpy(true);
    SMTPClient SMPTSpyFalse = buildBoolSpy(true);

    Account account2 = new Account();
    double startingBalance = account.getBalance();
    double transferAmount = 10;
    

    




    public static Stream<Arguments> accountIncorrectMails() {
        return Stream.of(
                //No TLD
                Arguments.of("aa@b"),
                //No domain
                Arguments.of("aa@.c"),
                //No username
                Arguments.of("@b.c"),
                //No @
                Arguments.of("aab.c")

        );

    }

    public static Stream<Arguments> accountCorrectMails() {
        return Stream.of(
                //typical mail
                Arguments.of("aa@b.c"),
                //mail with multiple dots before @
                Arguments.of("a.a@b.c"),
                //mail with an acceptable special symbol before @
                Arguments.of("a_a@b.c"),
                //Mail with multiple dots after @
                Arguments.of("a@c.edu.pl")

        );

    }



    //</editor-fold>

    //<editor-fold desc="Tests">

    @Test
    public void defaultBalanceIs0(){
        assertEquals( 0, startingBalance, "Default balance wasn't equal to 0");
    }

 @Test
    public void transferOnlyIfAmountUnderBalance(){

     account.setBalance(transferAmount);
     double balanceBeforeTransfer = account.getBalance();
     account.transfer(account2, transferAmount + 1);
     assertTrue(account.getBalance() >= 0, "Transfer was equal more than balance");
     assertEquals(account.getBalance(), balanceBeforeTransfer);
  }
  @Test
  public void balanceMustChangeAfterTransfer(){

  account.setBalance(transferAmount);

  double balanceBeforeTransfer1 = account.getBalance();
  double balanceBeforeTransfer2 = account2.getBalance();

  account.transfer(account2, transferAmount);

  assertEquals(account.getBalance(), balanceBeforeTransfer1 - transferAmount, "Balance after transfer incorrect for transfer initiator");
  assertEquals(account2.getBalance(), balanceBeforeTransfer2 + transferAmount, "Balance after transfer incorrect for transfer target");

  }

    @Test
    void accountMustSupportExpressTransfers(){
        account.setBalance(transferAmount);
        account.transferExpress(account2, transferAmount);
        assertEquals(transferAmount, account2.getBalance());
        assertEquals(0 - account.getExpressTransferCost(), account.getBalance());
    }

    @Test
    void accountMustHaveHistory(){

    account.setBalance(transferAmount);
    account.transfer(account2, transferAmount);
    assertEquals(account.getHistory()[account.getHistory().length - 1], -transferAmount);
    assertEquals(account2.getHistory()[account2.getHistory().length - 1], transferAmount);


    account.setBalance(transferAmount + account.getExpressTransferCost());
    account.transferExpress(account2, transferAmount);

    double lastValueAccount1 = account.getHistory()[account.getHistory().length - 1];
    double secondLastValueAccount1 = account.getHistory()[account.getHistory().length - 2];
    double lastValueAccount2 = account2.getHistory()[account.getHistory().length - 1];



        if(account.getExpressTransferCost() == 0){
        assertNotEquals(Math.abs(lastValueAccount1), account.getExpressTransferCost());
        assertEquals(lastValueAccount1, -transferAmount);
    }

    else{
        assertEquals(lastValueAccount1, -account.getExpressTransferCost());
        assertEquals(secondLastValueAccount1, -transferAmount);
    }

    assertEquals(lastValueAccount2, transferAmount);


    }

    @Test
    public void clearHistory(){
        account.setBalance(10);
        account.transfer(account2, 1);
        //System.out.println(account.getHistory()[0]);
        assertEquals(1, account.getHistory().length);
        account.clearHistory();
        assertEquals(0, account.getHistory().length, "clearHistory failed.");
    }


    @ParameterizedTest
    @MethodSource("accountIncorrectMails")
    public void sendToHistoryMustRejectIncorrectMail(String mail){
    assertFalse(account.mailHistory(mail, SMPTSpyTrue));
    }

    @ParameterizedTest
    @MethodSource("accountCorrectMails")
    public void sendHistoryReturnFalseIfSendFalse(String mail){
        assertFalse(account.mailHistory(mail, SMPTSpyFalse));
    }

    @ParameterizedTest
    @MethodSource("accountCorrectMails")
    public void sendHistoryReturnTrueIfSendTrue(String mail){
        assertFalse(account.mailHistory(mail, SMPTSpyTrue));
    }








    //</editor-fold desc="Tests">

  }

