package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.expression.EvaluationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PrivateAccountTest {

//<editor-fold desc="Variables">
    

    private static final List<String> blackList = new ArrayList<>();

    private static MockedStatic<BlackList> mockedBlackList;
    @BeforeAll
    public static void setupBlacklistMock() {

        //Mock needed to avoid database-side verification when creating new account
        mockedBlackList = mockStatic(BlackList.class);

        mockedBlackList.when(() -> BlackList.addAccountToBlackList(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String pesel = invocation.getArgument(0);
                    blackList.add(pesel);
                    return null;
                });

        mockedBlackList.when(() -> BlackList.isAccountOnBlackList(anyString()))
                .thenAnswer(invocation -> {
                    String pesel = invocation.getArgument(0);
                    return blackList.contains(pesel);
                });
    }

    @AfterAll
    public static void removeBlackListMock() {
        mockedBlackList.close();
    }




    String name = "Janusz";
    String surname = "Kowalski";

    String pesel = "70345678911";

    String promoCode = "PROM_123";
    
    PrivateAccount account = new PrivateAccount(name, surname, pesel, promoCode);
    PrivateAccount account2 = new PrivateAccount(name, surname, pesel);


    double couponAmount = PrivateAccount.COUPON_BONUS;
    static double loanAmount = 100;

    double testTransferAmount = 1;





    public static Stream<Arguments> accountsWithIncorrectPesels() {
        return Stream.of(
                //Correct length, incorrect symbols
                Arguments.of(new PrivateAccount("a", "a", "ABCDEABCDEA")),
                //Too short
                Arguments.of(new PrivateAccount("b", "a", "0123456789", "PROM_321")),
                //Too long
                Arguments.of(new PrivateAccount("c", "a", "012345678901", "PROM_999")),
                //Empty
                Arguments.of(new PrivateAccount("d", "a", "", "PROM_000"))


        );
    }

    public static Stream<Arguments> accountsWithCorrectPesels() {
        return Stream.of(
                Arguments.of(new PrivateAccount("a", "a", "12312312311")),
                Arguments.of(new PrivateAccount("b", "a", "32132132132", "PROM_321")),
                Arguments.of(new PrivateAccount("c", "a", "12345678901", "PROM_321"))
                );
    }

    public static Stream<Arguments> accountsThatCantGetCouponBonus() {
   return Stream.of(
           //Wrong coupon code
           Arguments.of(new PrivateAccount("a", "a", "71111111111", "PROM_")),
           Arguments.of(new PrivateAccount("b", "a", "88111111111", "PROM_1")),
           Arguments.of(new PrivateAccount("c", "a", "991111111111", "PROM_12")),
           Arguments.of(new PrivateAccount("d", "a", "88111111111", "PROM_1234")),
           Arguments.of(new PrivateAccount("e", "a", "66111111111", "PROM123")),
           Arguments.of(new PrivateAccount("f", "a", "65111111111", "123")),
           //Correct coupon, wrong pesel
           Arguments.of(new PrivateAccount("g", "a", "30111111111", "PROM_123")),
           Arguments.of(new PrivateAccount("h", "a", "59111111111", "PROM_123")),
           Arguments.of(new PrivateAccount("a", "a", "31111111111", "PROM_123"))
   );

    }

    public static Stream<Arguments> accountsWithCorrectCoupons() {
        return Stream.of(
                Arguments.of(new PrivateAccount("a", "a", "71111111111", "PROM_123")),
                Arguments.of(new PrivateAccount("b", "a", "60111111111", "PROM_321")),
                Arguments.of(new PrivateAccount("c", "a", "99111111111", "PROM_000")),
                Arguments.of(new PrivateAccount("d", "a", "88111111111", "PROM_999"))
        );

    }

    public static Stream<Arguments> accountsCorrectForLoans() {
        return Stream.of(
                //Meets the sum requirement
                Arguments.of(new PrivateAccount("a", "a", "71111111111", "", 0, new double[]{-1, -1, -1, -1, 5000}, null)),
                //Meets the 3 transactions requirement
                Arguments.of(new PrivateAccount("b", "a","", "61111111111", 0, new double[]{1,1,1}, null))
        );

    }
    public static Stream<Arguments> accountsThatDontMeetSumReq() {
        return Stream.of(
                //Sum equal to 0
                Arguments.of(new PrivateAccount("a", "a","", "11111111111", 0, new double[]{1, -1, 1, -1, 1, -1}, null)),
                //Sum bigger than amount but less than 5 transactions
                Arguments.of(new PrivateAccount("b", "a","", "11111111111", 0, new double[]{1, 1, -1, loanAmount}, null)),
                //empty history
                Arguments.of(new PrivateAccount("c", "a","", "11111111111", 0, new double[]{}, null))


                );

    }
    public static Stream<Arguments> accountsThatDontMeetTransactionReq() {
        return Stream.of(
                //Only negative transactions
                Arguments.of(new PrivateAccount("a", "a","", "11111111111", 0,  new double[]{-1, -1, -1}, null)),
                //3 positive transactions but not one after another
                Arguments.of(new PrivateAccount("a", "a","", "11111111111", 0, new double[]{1,-1, 1, -1,1}, null)),
                //3 positive transactions but not the last 3
                Arguments.of(new PrivateAccount("a", "a", "","11111111111",0,  new double[]{1,1,1,-1}, null)),
                //Empty history
                Arguments.of(new PrivateAccount("a", "a","", "11111111111", 0, new double[]{}, null))

        );

    }




//</editor-fold>

//<editor-fold desc="Tests">



    @Test
    public void accountMustHaveNameSurnameBalance(){
        assertEquals(name, account.getName(), "Account has incorrect name");
        assertEquals( surname, account.getSurname(), "Account has incorrect surname");
        assertTrue(account.getBalance() == 0 || (account.getBalance() == couponAmount && account.isCouponUsed), "Starting balance wasnt equal to 0");
    }


    @ParameterizedTest
    @MethodSource("accountsWithIncorrectPesels")
    void peselMustBeNumericAndOfCorrectLength(PrivateAccount account){
    assertEquals(PrivateAccount.INCORRECT_PESEL_MESSAGE, account.getPesel(), "");
    }

    @ParameterizedTest
    @MethodSource("accountsWithCorrectPesels")
    void correctPeselShouldntBeReplaced(PrivateAccount account){
        assertNotEquals(PrivateAccount.INCORRECT_PESEL_MESSAGE, account.getPesel(), "Pesel code of account " + account.getName() + " was modified, but it shouldnt have been.");
    }




    @ParameterizedTest
    @MethodSource("accountsWithCorrectCoupons")
    void peselMustBeNumeric(){
        assertTrue(account.getPesel().matches("[0-9]{11}"), "Pesel had non-digit symbols.");
    }

    @ParameterizedTest
    @MethodSource("accountsWithCorrectCoupons")
    void properCouponMustIncreaseBalance(){

        assertEquals(couponAmount, account.getBalance());

    }

    @ParameterizedTest
    @MethodSource("accountsThatCantGetCouponBonus")
    void couponCantHaveWrongBalance(PrivateAccount accountWrongCoupon) {
        assertEquals(0, accountWrongCoupon.getBalance());
    }



    @Test
    void accountMustSupportExpressTransfer(){
        account.setBalance(testTransferAmount);
        System.out.println(account.getBalance());
        account2.setBalance(0);
        account.transferExpress(account2, testTransferAmount);
        assertEquals(testTransferAmount, account2.getBalance());
        assertEquals(-account.getExpressTransferCost(), account.getBalance());
    }

    @ParameterizedTest
    @MethodSource("accountsCorrectForLoans")
    void loanMustMeetConditions(PrivateAccount account){
    account.loan(loanAmount);
    assertEquals(loanAmount, account.getBalance());
    }

    @Test
    void cantLoanToBlockedAccounts() {
        BlackList.addAccountToBlackList(account.getPesel(), "placeholder reason");
        System.out.println(Arrays.toString(blackList.toArray()));
        account.loan(loanAmount);
        assertNotEquals(loanAmount, account.getBalance());
    }



    @ParameterizedTest
    @MethodSource("accountsThatDontMeetTransactionReq")
    void accountsDontMeet3transactionReq(PrivateAccount account){
        account.loan(loanAmount);
        assertEquals(0, account.getBalance());
    }
    @ParameterizedTest
    @MethodSource("accountsThatDontMeetSumReq")
    void accountsDontMeetSumReq(PrivateAccount account){
        account.loan(loanAmount);
        assertEquals(0, account.getBalance());
    }

    @Test
    public void parentClassTransferCostWasUpdated(){
        assertNotEquals(0, account.getExpressTransferCost());
    }

    @Test
    public void setDatabaseIDShouldThrowWhenIDLessThan0(){
        assertThrows(EvaluationException.class, () -> account.setDatabaseID(-1));
    }

    //Not sure if I should remove these since theyre provided by lombok now?
    @Test
    public void setDatabaseID(){
        int newID = 555888;
        account.setDatabaseID(newID);
        assertEquals(newID, account.getDatabaseID());
    }

    @Test
    public void setName(){
        String newName = "jakub";
        account.setName(newName);
        assertEquals(newName, account.getName());
    }

    @Test
    public void setSurname(){
        String newSurname = "kowal";
        account.setSurname(newSurname);
        assertEquals(newSurname, account.getSurname());
    }

    @Test
    void accountEqualsIsTrueForItself(){
        assertTrue(account.equals(account));
    }

    @Test
    void accountEqualsIsTrueForCopy(){
        PrivateAccount copy = new PrivateAccount(name, surname, pesel, promoCode, account.getBalance(), account.getHistory(), account.getDatabaseID());
        assertTrue(copy.equals(account));
    }



//</editor-fold>

}

