package org.example;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

import org.example.SMTPClient;
import org.mockito.Mockito;

import javax.xml.crypto.Data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


public class Konto {

    public double[] historia = {};
    double saldo = 0;










    //protected aby wartość mogła być zastąpiona w konstruktorze przez dzieci. Jeśli musisz użyć, jest getter
    protected static double KOSZTPRZELEWUEKSPRES = 0;

    public Konto(){

    }

    public void przelew(Konto konto2, double kwotaPrzelewu) {
    if(kwotaPrzelewu > this.saldo){
        System.out.println("Przelew niedokonany, kwota przelewu nie moze byc wieksza od sala");
    }
    else{

        this.setSaldo(this.saldo - kwotaPrzelewu);
        konto2.setSaldo(konto2.saldo + kwotaPrzelewu);

        this.dodajDoHistorii(-1 * kwotaPrzelewu);

        konto2.dodajDoHistorii(kwotaPrzelewu);
    }

    }

    public void przelewEkspresowy(Konto konto2, double kwotaPrzelewu) {

   if(kwotaPrzelewu <= saldo)
        {
            this.setSaldo(saldo - kwotaPrzelewu - KOSZTPRZELEWUEKSPRES);
            konto2.setSaldo(konto2.getSaldo() + kwotaPrzelewu);
            this.dodajDoHistorii(-1 * kwotaPrzelewu);
            this.dodajDoHistorii(-1 * getKOSZTPRZELEWUEKSPRES());

            konto2.dodajDoHistorii(kwotaPrzelewu);
        }

        this.setSaldo(this.saldo);

    }

    public void dodajDoHistorii(double kwota){

           if(Math.abs(kwota) != 0){
               this.historia = Arrays.copyOf(historia, historia.length + 1);
               historia[historia.length - 1] = kwota;
           }

    }




    public void setSaldo(double noweSaldo) {
        this.saldo = noweSaldo;
    }


    public double getSaldo() {
        return saldo;
    }

    public double[] getHistoria(){
        return historia;
    }

    public double getKOSZTPRZELEWUEKSPRES() {
        return KOSZTPRZELEWUEKSPRES;
    }


    public void wyczyscHistorie() {
        this.historia = new double[]{};
    }

    public boolean sendHistoryToMail(String mail, SMTPClient client){
        String regexWeryfikacjiMailu = "$[a-zA-Z.]@[a-zA-Z]{1,}[.]{0,1}[a-zA-Z]{0,}.[a-zA-Z]{1,}^";

        if(!mail.matches(regexWeryfikacjiMailu)){
        return false;
        }

        String mailSubject = "Wyciąg z dnia " + LocalDate.now();
        String mailText = (this instanceof KontoFirmowe ? "Historia twojej firmy to:" : "Twoja historia konta to: ") + Arrays.toString(historia);
        return client.send(mailSubject, mailText, mail);


    }
}
