package org.example;

import lombok.Data;
import org.springframework.expression.EvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;



@Data
public class BusinessAccount extends Account {


String name;
String nip;

public static final String ENV_URL =
        System.getenv("BANK_APP_MF_URL") != null
        ? System.getenv("BANK_APP_MF_URL")
        : "https://wl-api.mf.gov.pl/api/search/nip/";


    @Override
    public double getExpressTransferCost(){
        return 5;
    }


double ZUS_AMOUNT = 1775;

public static final int NIP_LENGTH = 10;

public BusinessAccount(String name, String nip){
this.name = name;
this.nip = nip;

    if(nip.length() == NIP_LENGTH && !verifyAccount(nip)){
        throw new EvaluationException("Company not registered!");
    }

}

    public BusinessAccount(String name, String nip, double balance,  double[] history){
        this.name = name;
        this.nip = nip;
        this.history = history;
        this.balance = balance;

        if(!verifyAccount(nip)) throw new EvaluationException("Company not registered!");

    }

    public void loan(double loanAmount) {
    if(loanRequirementZUS() && loanRequirementBalance(loanAmount)){
        this.balance+=loanAmount;
    }
    }

    private boolean loanRequirementZUS() {
        for (double v : history) {
            if (v == ZUS_AMOUNT) {
                return true;
            }

        }
    return false;
    }

    private boolean loanRequirementBalance(double loanAmount) {
    return this.balance >= loanAmount * 2;
    }


    boolean verifyAccount(String nip){

    if(nip.length() != NIP_LENGTH) return true;

    LocalDate dateToday = LocalDate.now();
    String url = ENV_URL + nip + "?date=" + dateToday;


        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
            return entity.getStatusCode().equals(HttpStatus.OK) && !entity.toString().contains("\"subject\":null");

        } catch (RestClientException e) {return false;}

    }



    @Override
    public boolean equals(Object object) {

        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return  false;

        BusinessAccount account = (BusinessAccount) object;
        return name.equals(account.getName())
                && nip.equals(account.getNip());

    }
}
