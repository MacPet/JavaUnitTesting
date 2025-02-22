package org.example;

import static java.lang.System.out;

import lombok.*;
import org.springframework.expression.EvaluationException;

import java.util.Arrays;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor //needed for API
public class PrivateAccount extends Account {


    private String name;
    private String surname;
    private String pesel;
    private String coupon = "";

    private Integer databaseID = null;



    public static final int COUPON_AGE_LIMIT = 60;
    public static final double COUPON_BONUS = 50.00;
    public static final double NEW_EXPRESS_TRANSFER_COST = 1;
    public static final String INCORRECT_PESEL_MESSAGE =  "Incorrect pesel!";
    public boolean isCouponUsed = false;


    @Override
    public double getExpressTransferCost(){
        return 2;
    }

    //In case of a lack of a
    public PrivateAccount(String name, String surname, String pesel){
        this.name = name;
        this.surname = surname;
        this.pesel = verifyPesel(pesel);
    }

    public PrivateAccount(String name, String surname, String pesel, String coupon){
        this.name = name;
        this.surname = surname;
        this.pesel = verifyPesel(pesel);

            int userYearOfBirth = (this.getPesel().length() == 11) ? Integer.parseInt(this.getPesel().substring(0, 2)) : COUPON_AGE_LIMIT - 1;

            if(coupon.matches("PROM_[0-9]{3}")) {

                if (userYearOfBirth >= COUPON_AGE_LIMIT) {

                    this.setBalance(this.getBalance() + COUPON_BONUS);
                    out.println("User " + name + " " + surname + " was granted " + COUPON_BONUS + " of coupon bonus.");
                    this.isCouponUsed = true;
                    this.coupon = coupon;

                }
                else {
                out.println("User born before year  " + COUPON_AGE_LIMIT + " , bonus not granted.");
                }

            }


        }
        
    public PrivateAccount(String name, String surname, String pesel, String coupon, double balance, double[] history, Integer databaseID){
        super(balance, history);
    this.name = name;
    this.surname = surname;
    this.pesel = verifyPesel(pesel);
    this.coupon = coupon;
    this.databaseID = databaseID;
    }

    
    

    public void setDatabaseID(Integer newID) throws EvaluationException {

        if(newID < 0){
        throw new EvaluationException("new ID was set to a negative value.");
        }

        this.databaseID = newID;
    }
    
    
    public void loan(double loanAmount) {
        boolean isPeselBlocked = BlackList.isAccountOnBlackList(this.getPesel());
    if(isPeselBlocked){
        System.out.println("Failed to loan - pesel blocked");
    }
    else if(checkLast3TransactionsPositive() || checkSumOfLast5HigherThanLoan(loanAmount)){
     this.setBalance(this.getBalance() + loanAmount);
    }
    else System.out.println("failed to loan - system error");
    }

    
private String verifyPesel(String pesel){
if(pesel.matches("[0-9]{11}")){
return pesel;
}
else return INCORRECT_PESEL_MESSAGE;
}
    private boolean checkLast3TransactionsPositive(){
        if(history.length < 3) {
            return false;
        }

       return (history[history.length - 1] > 0) && (history[history.length - 2] > 0) && (history[history.length - 3] > 0);
    }
    private boolean checkSumOfLast5HigherThanLoan(double loanAmount){

     int numbersChecked = 5;

     if(history.length < numbersChecked){
     return false;
     }

     double sum = 0;
     for(int i = 1; i < numbersChecked + 1; i++){
     sum+=history[history.length - i];
     }

     return sum >= loanAmount;

    }



    @Override
    public boolean equals(Object object) {

        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return  false;

        PrivateAccount account = (PrivateAccount) object;
        return name.equals(account.getName())
                && surname.equals(account.getSurname())
                && pesel.equals(account.getPesel())
                && coupon.equals(account.getCoupon())
                && Objects.equals(databaseID, account.getDatabaseID());

    }


}
