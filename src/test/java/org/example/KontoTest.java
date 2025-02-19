package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;


import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class KontoTest {

    public SMTPClient buildBoolSpy(boolean result) {

        SMTPClient temporaryClient = new SMTPClient();
        SMTPClient spyClient = Mockito.spy(temporaryClient);

        Mockito.doAnswer(invocation -> result).when(spyClient).send(anyString(), anyString(), anyString());

        return spyClient;

    }

    //<editor-fold desc="Zmienne i streamy">
    Konto konto = new Konto();

    SMTPClient SMPTSpyTrue = buildBoolSpy(true);
    SMTPClient SMPTSpyFalse = buildBoolSpy(true);

    Konto konto2 = new Konto();
    double saldoPoczatkowe = konto.getSaldo();
    double kwotaPrzelewu = 10;

    //ze wzgledu na skomplikowanie warunkow jakie moze nie spelniac mail, te testy najpewniej nie pokrywaja wszystkich mozliwosci









    public static Stream<Arguments> kontoNieprawidloweMaile() {
        return Stream.of(
                //brak kraju
                Arguments.of("aa@b"),
                //brak domeny
                Arguments.of("aa@.c"),
                //brak nazwy uzytkownika
                Arguments.of("@b.c"),
                //brak @
                Arguments.of("aab.c")

        );

    }

    public static Stream<Arguments> kontoPrawidloweMaile() {
        return Stream.of(
                //typowy email
                Arguments.of("aa@b.c"),
                //wiele kropek przed @
                Arguments.of("a.a@b.c"),
                //znak specjalny dopuszcalny przed @
                Arguments.of("a_a@b.c"),
                //wielokrotnosc kropek po @
                Arguments.of("a@c.edu.pl")

        );

    }



    //</editor-fold>

    //<editor-fold desc="Funkcje testowe">

    @Test
    public void saldoPoczatkoweMusiWynosicZero(){

        assertEquals( 0, saldoPoczatkowe, "Saldo początkowe nie wyniosło 0");

    }

 @Test
    public void przelewNieMozeWynosicWiecejNizSumaSalda(){

     konto.setSaldo(kwotaPrzelewu);
     double saldoPrzedPRzelewem = konto.getSaldo();
     konto.przelew(konto2, kwotaPrzelewu + 1);
     assertTrue(konto.getSaldo() >= 0, "Przelew wyniósł więcej niż saldo");
     assertEquals(konto.getSaldo(), saldoPrzedPRzelewem);
  }
  @Test
    public void roznicaPoPrzelewieMusiBycZgodnaSumiePzelewu(){

  konto.setSaldo(kwotaPrzelewu);

  double saldoKonta1PrzedPrzelewem = konto.getSaldo();
  double saldoKonta2PrzedPrzelewem = konto2.getSaldo();

  konto.przelew(konto2, kwotaPrzelewu);

  assertEquals(konto.getSaldo(), saldoKonta1PrzedPrzelewem - kwotaPrzelewu, "Kwota przelewu niezgodna dla konta 1");
  assertEquals(konto2.getSaldo(), saldoKonta2PrzedPrzelewem + kwotaPrzelewu, "Kwota przelewu niezgodna dla konta 2");

  }

    @Test
    void kontoMusiWspieracPrzelewyEkspresowe(){
        konto.setSaldo(kwotaPrzelewu);
        konto.przelewEkspresowy(konto2, kwotaPrzelewu);
        assertEquals(kwotaPrzelewu, konto2.getSaldo());
        assertEquals(0-Konto.KOSZTPRZELEWUEKSPRES, konto.getSaldo());
    }

    @Test
    void kontoMusiMiecHistorie(){

    konto.setSaldo(kwotaPrzelewu);
    konto.przelew(konto2, kwotaPrzelewu);
    assertEquals(konto.getHistoria()[konto.getHistoria().length - 1], -kwotaPrzelewu);
    assertEquals(konto2.getHistoria()[konto2.getHistoria().length - 1], kwotaPrzelewu);


    konto.setSaldo(kwotaPrzelewu + konto.getKOSZTPRZELEWUEKSPRES());
    konto.przelewEkspresowy(konto2, kwotaPrzelewu);

    if(konto.getKOSZTPRZELEWUEKSPRES() == 0){
    assertNotEquals(Math.abs(konto.getHistoria()[konto.getHistoria().length - 1]), konto.getKOSZTPRZELEWUEKSPRES());
        assertEquals(konto.getHistoria()[konto.getHistoria().length - 1], -kwotaPrzelewu);
    }

    else{
        assertEquals(konto.getHistoria()[konto.getHistoria().length - 1], -konto.getKOSZTPRZELEWUEKSPRES());
        assertEquals(konto.getHistoria()[konto.getHistoria().length - 2], -kwotaPrzelewu);
    }

    assertEquals(konto2.getHistoria()[konto2.getHistoria().length - 1], kwotaPrzelewu);


    }

    @Test
    public void czyszczenieHistoriiMusiCzyscicHistorie(){
        konto.setSaldo(10);
        konto.przelew(konto2, 1);
        System.out.println(konto.getHistoria()[0]);
        assertEquals(1, konto.getHistoria().length, "z jakiegos powodu dlugosc historii na poczatku nie byla rowna ");
        konto.wyczyscHistorie();
        assertEquals(0, konto.getHistoria().length, "funkcja wyczyscHistorie nie wyczyscila historii");
    }


    @ParameterizedTest
    @MethodSource("kontoNieprawidloweMaile")
    public void sendToHistoryMustRejectIncorrectMail(String mail){
    assertFalse(konto.sendHistoryToMail(mail, SMPTSpyTrue));
    }

    @ParameterizedTest
    @MethodSource("kontoPrawidloweMaile")
    public void sendHistoryReturnFalseIfSendFalse(String mail){
        assertFalse(konto.sendHistoryToMail(mail, SMPTSpyFalse));
    }

    @ParameterizedTest
    @MethodSource("kontoPrawidloweMaile")
    public void sendHistoryReturnTrueIfSendTrue(String mail){
        assertFalse(konto.sendHistoryToMail(mail, SMPTSpyTrue));
    }








    //</editor-fold desc="Funkcje testowe">

  }

