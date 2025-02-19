package org.example;

import org.springframework.expression.EvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

public class KontoFirmowe extends Konto {


String nazwa;
String nip;

public static final String ENV_URL = System.getenv("BANK_APP_MF_URL") != null
        ? System.getenv("BANK_APP_MF_URL")
        : "https://wl-api.mf.gov.pl/api/search/nip/";

double NOWYKOSZTPRZELEWUEKSPRES = 5;

double kwotaZUS = 1775;

public static final int DLUGOSC_NIPU = 10;

public KontoFirmowe(String nazwa, String nip){
this.nazwa = nazwa;
this.nip = nip;
KOSZTPRZELEWUEKSPRES = NOWYKOSZTPRZELEWUEKSPRES;

    if(nip.length() == DLUGOSC_NIPU && !sprawdzKonto(nip)){
        throw new EvaluationException("Company not registered!");
    }

}

    public KontoFirmowe(String nazwa, String nip, double[] historia, double saldo){
        this.nazwa = nazwa;
        this.nip = nip;
        this.historia = historia;
        this.saldo = saldo;
        KOSZTPRZELEWUEKSPRES = NOWYKOSZTPRZELEWUEKSPRES;

        if(!sprawdzKonto(nip)) throw new EvaluationException("Company not registered!");

    }




    public String getNazwa() {
    return nazwa;
    }

    public String getNip(){
    return nip;
    }


    //override potrzebny w celu updateu metody przelewu ekspres bez ponownego definiowania

    //probowalem zrobic ten element z pomoca KOSZTPRZELEWUEKSPRES = (wartość), lecz to zmieniało też wartość
    //w klasie rodzicu, bo java nie wspiera overridu values
    @Override
    public double getKOSZTPRZELEWUEKSPRES() {
        return KOSZTPRZELEWUEKSPRES;
    }

    public void zaciagnijKredyt(double kwotaKredytu) {
    if(kredytWarunekZUS() && kredytWarunekSaldo(kwotaKredytu)){
        this.saldo+=kwotaKredytu;
    }
    }

    private boolean kredytWarunekZUS() {
        for (double v : historia) {
            if (v == kwotaZUS) {
                return true;
            }

        }
    return false;
    }

    private boolean kredytWarunekSaldo(double kwotaKredytu) {
    return this.saldo >= kwotaKredytu * 2;
    }


    boolean  sprawdzKonto(String nip){

    if(nip.length() != DLUGOSC_NIPU) return true;

    LocalDate dzisiejszaData = LocalDate.now();
    String url = ENV_URL + nip + "?date=" + dzisiejszaData;


        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
            return entity.getStatusCode().equals(HttpStatus.OK) && !entity.toString().contains("\"subject\":null");

        } catch (RestClientException e) {return false;}

    }
}
