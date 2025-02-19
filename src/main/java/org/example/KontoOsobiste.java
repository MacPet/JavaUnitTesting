package org.example;


import static java.lang.System.out;
import org.example.newAccountRegistry;
import org.springframework.expression.EvaluationException;

import java.util.Arrays;
import java.util.Objects;

public class KontoOsobiste extends Konto {


    private String imie;
    private String nazwisko;
    private String pesel;
    private String kupon = "";

    private Integer databaseID = null;



    //stałe
    public static final int LIMITRABATOWYROCZNIK = 60;
    public static final double BONUSRABATOWY = 50.00;
    public static final double NOWYKOSZTPRZELEWUEKSPRES = 1;
    public boolean uzytoKuponu = false;




    //na wypadek braku kodu rabatowego
    public KontoOsobiste(String imie, String nazwisko, String pesel){
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = zweryfikujPesel(pesel);
        KOSZTPRZELEWUEKSPRES = NOWYKOSZTPRZELEWUEKSPRES;
    }

    public KontoOsobiste(String imie, String nazwisko, String pesel, String kodRabatowy){

        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = zweryfikujPesel(pesel);
        KOSZTPRZELEWUEKSPRES = NOWYKOSZTPRZELEWUEKSPRES;

            int rocznikUzytkownika = (this.getPesel().length() == 11) ? Integer.parseInt(this.getPesel().substring(0, 2)) : LIMITRABATOWYROCZNIK - 1;

            //test kuponu
            if(kodRabatowy.matches("PROM_[0-9]{3}")) {


                //test wieku dla kuponu
                if (rocznikUzytkownika >= LIMITRABATOWYROCZNIK) {

                    this.setSaldo(this.getSaldo() + BONUSRABATOWY);
                    out.println("Użytkownikowi" + imie + " " + nazwisko + " przydzielono " + BONUSRABATOWY + " złotych.");
                    this.uzytoKuponu = true;
                    this.kupon = kodRabatowy;

                }
                //else {
                //out.println("Użytkownik urodzony przed rocznikiem " + LIMITRABATOWYROCZNIK + " , nie przydzielono bonusu.");
                //}

            }


        }







    //FUNKCJA DEFAULTOWA
    // NIE USUWAĆ, POTRZEBNA DO DZIAŁANIA POSTÓW SPRING BOOT
    public KontoOsobiste(){}


    //glownie do testowania
    public KontoOsobiste(String imie, String nazwisko, String pesel,  String kupon, double saldo, double[] historia, Integer databaseID){
    this.imie = imie;
    this.nazwisko = nazwisko;
    this.pesel = zweryfikujPesel(pesel);
    this.kupon = kupon;
    this.historia = historia;
    this.saldo = saldo;
    this.databaseID = databaseID;
    KOSZTPRZELEWUEKSPRES = NOWYKOSZTPRZELEWUEKSPRES;
    }

    public String getImie() {
        return imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getPesel() {
        return pesel;
    }

    public String getKupon(){return kupon;}

    public Integer getDatabaseID(){return databaseID;}



    public void setDatabaseID(Integer newID) throws EvaluationException {

        if(newID < 0){
        throw new EvaluationException("new ID was set to a negative value.");
        }

        this.databaseID = newID;
    }



    public void setImie(String  noweImie) {
        this.imie = noweImie;
    }

    public void setNazwisko(String noweNazwisko) {
        this.nazwisko = noweNazwisko;
    }


    //override potrzebny w celu updateu metody przelewu ekspres bez ponownego definiowania


    public void zaciagnijKredyt(double kwotaKredytu) {
        boolean czyPeselZablokowany = BlackList.isAccountOnBlackList(this.getPesel());
    if(czyPeselZablokowany){
        System.out.println("nie udalo sie zaciagnac kredytu - pesel zablokowany");
    }
    else if(ostatnieTrzyTransakcjeWplatami() || sumaOstatnichPieciuWiekszaOdKwoty(kwotaKredytu)){
     this.setSaldo(this.getSaldo() + kwotaKredytu);
    }
      // else System.out.println("nie udalo sie zaciagnac kredytu");
    }


    //PAMIETAJ ABY UŻYĆ JEŚLI TWORZYSZ NOWY KONSTRUKTOR Z PESELEM
private String zweryfikujPesel(String pesel){
if(pesel.matches("[0-9]{11}")){
return pesel;
}
else return "Niepoprawny pesel!";
}

    @Override
    public boolean equals(Object obiekt) {
        if (this == obiekt) return true;
        if (obiekt == null || getClass() != obiekt.getClass()) return false;

        KontoOsobiste konto = (KontoOsobiste) obiekt;
        return imie.equals(konto.getImie())
                && nazwisko.equals(konto.getNazwisko())
                && pesel.equals(konto.getPesel())
                && kupon.equals(konto.getKupon())
                && saldo == konto.getSaldo()
                && Arrays.equals(historia, konto.getHistoria())
                &&  Objects.equals(databaseID, konto.getDatabaseID());

    }


    private boolean ostatnieTrzyTransakcjeWplatami(){
        if(historia.length < 3) {
            return false;
        }

       return (historia[historia.length - 1] > 0) && (historia[historia.length - 2] > 0) && (historia[historia.length - 3] > 0);
    }
    private boolean sumaOstatnichPieciuWiekszaOdKwoty(double kwotaKredytu){

     int iloscSprawdzana = 5;

     if(historia.length < iloscSprawdzana){
     return false;
     }

     double suma = 0;
     for(int i = 1; i < iloscSprawdzana + 1; i++){
     suma+=historia[historia.length - i];
     }

     return suma >= kwotaKredytu;

    }


}
