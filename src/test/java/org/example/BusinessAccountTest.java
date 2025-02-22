package org.example;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.base.MockitoException;
import org.springframework.expression.EvaluationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BusinessAccountTest {



    //<editor-fold desc="Variables and streams">
    String name = "name";
    String nip = "8461627563";

    static double  loanAmount = 1000;

    BusinessAccount account = new BusinessAccount(name, nip);
    BusinessAccount account2 = new BusinessAccount(name, nip);



    public BusinessAccount constructorMocker(String name, String companyNIP) {

        try (MockedConstruction<BusinessAccount> mocked = mockConstruction(BusinessAccount.class, (mock, context) -> {

            System.out.println("Mocked constructor used with parameters: " + context.arguments());
            String mockNip = context.arguments().get(1).toString();

            when(mock.verifyAccount(anyString())).thenAnswer(invocation -> mockNip.length() != 10 || mockNip.equals(nip) || mockNip.equals("7740001454"));
            //when(mock.transfer(any(), anyDouble())).thenCallRealMethod()


            if (!mock.verifyAccount(context.arguments().get(1).toString())) {
               throw new EvaluationException("Company not registered!");
            }
        })) {

            return new BusinessAccount(name, companyNIP);
        }
    }



    public static Stream<Arguments> accountsAllowedToTransfer() {
        return Stream.of(
                Arguments.of(new BusinessAccount("a", "a",  loanAmount * 2, new double[]{1775})),
                Arguments.of(new BusinessAccount("b", "a", loanAmount * 3, new double[]{1775}))
        );
    }


    public static Stream<Arguments> incorrectNIPsOfIncorrectLength() {
        return Stream.of(
                //too short
                Arguments.of("123456789"),
                //too long
                Arguments.of("123123123123123")
        );
    }

    public static Stream<Arguments> incorrectNIPsOfCorrectLength() {
        return Stream.of(
                //Doesn't exist
                Arguments.of("0000000000"),
                //Is not a numerical
                Arguments.of("ABCDEFGHIJ")
        );
    }

    public static Stream<Arguments> correctNIPs() {
        return Stream.of(
                Arguments.of("8461627563"),
                Arguments.of("7740001454")
        );
    }

    public static Stream<Arguments> accountsWithoutTransferZUS() {
        return Stream.of(
                //Meets the sum requirement but not the ZUS requirement (empty list)
                Arguments.of(new BusinessAccount("a", "a", loanAmount * 2, new double[]{} )),
                //Meets the sum requirement but not the ZUS requirement (no value of 1775)
                Arguments.of(new BusinessAccount("b", "a", loanAmount * 3, new double[]{1774,1776} )),
                //Fails to meet both requirements
                Arguments.of(new BusinessAccount("b", "a", loanAmount * 1, new double[]{1774,1776} ))

        );
    }

    public static Stream<Arguments> accountsWithoutTransferBalance() {
        return Stream.of(
                //Passes the ZUS requirement but not the balance requirement
                Arguments.of(new BusinessAccount("a", "a",  loanAmount * 1, new double[]{1775})),
                //Passes the ZUS requirement but not the balance requirement (0)
                Arguments.of(new BusinessAccount("b", "a", 0, new double[]{1775})),
                //Fails both requirements
                Arguments.of(new BusinessAccount("b", "a", loanAmount * 1, new double[]{1774,1776}))

        );
    }





    //</editor-fold>

    //<editor-fold desc="Tests">
    @Test
    void accountMustHaveNameAndNip(){
    assertEquals(account.getName(), name);
    assertEquals(account.getNip(), nip);
    }

    @Test
    void accountMustSupportTransfer(){
    account.setBalance(10);
    account.transfer(account2, 10);
        assertEquals(10, account2.getBalance());
    }

    @Test
    void accountMustSupportExpressTransfer(){
        account.setBalance(10);
        account.transferExpress(account2, 10);
        assertEquals(10, account2.getBalance());
        assertEquals(0 - account.getExpressTransferCost(), account.getBalance());

    }

    @ParameterizedTest
    @MethodSource("accountsAllowedToTransfer")
    void loanMustMeetRequirementsToWork(BusinessAccount account){
        double balanceBeforeTransfer = account.getBalance();
        account.loan(loanAmount);
        assertEquals(balanceBeforeTransfer + loanAmount, account.getBalance());
    }

    @ParameterizedTest
    @MethodSource("accountsWithoutTransferBalance")
    void mustMeetZusRequirementToWork(BusinessAccount account){
        double balanceBeforeTransfer = account.getBalance();
        account.loan(loanAmount);
        assertEquals(balanceBeforeTransfer, account.getBalance());
    }
    @ParameterizedTest
    @MethodSource("accountsWithoutTransferZUS")
    void mustMeetBalanceRequirementToWork(BusinessAccount account){
        double balanceBeforeTransfer = account.getBalance();
        account.loan(loanAmount);
        assertEquals(balanceBeforeTransfer, account.getBalance());
    }


    //In this project I were to assume incorrect nips that also had incorrect length were to be allowed to pass.
    @ParameterizedTest
    @MethodSource("incorrectNIPsOfIncorrectLength")
    void nipsOfBadLengthShouldntCrashTheSystem(String nip){
    BusinessAccount newAccount = new BusinessAccount("a", nip);
    assertEquals(BusinessAccount.class, newAccount.getClass());
    }

    @ParameterizedTest
    @MethodSource("incorrectNIPsOfCorrectLength")
    void wrongNIPSofCorrectLengthShouldBeBLocked(String nip){

        assertThrows(MockitoException.class,
                () -> constructorMocker("a", nip));


    }

    @ParameterizedTest
    @MethodSource("correctNIPs")
    void correctNIPShouldntCauseError(String nip){
        assertDoesNotThrow(() -> constructorMocker("a", nip));
    }

    @Test
    public void constructorShouldThrowWhenInvalidNIP(){
    assertThrows(EvaluationException.class, () -> new BusinessAccount("aaa", "0000000000"));
    }

    @Test
    void accountEqualsIsTrueForItself(){
        assertTrue(account.equals(account));
    }

    @Test
    void accountEqualsIsTrueForCopy(){
        BusinessAccount copy = new BusinessAccount(name, nip, account.getBalance(), account.getHistory());
        assertTrue(copy.equals(account));
    }



    //</editor-fold>

}