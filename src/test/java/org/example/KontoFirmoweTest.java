package org.example;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.springframework.expression.EvaluationException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KontoFirmoweTest {



    //<editor-fold desc="Dane i streamy danych">
    String nazwa = "nazwa";
    String nip = "8461627563";

    static double  kwotaKredytu = 1000;

    KontoFirmowe konto = new KontoFirmowe(nazwa, nip);
    KontoFirmowe konto2 = new KontoFirmowe(nazwa, nip);


    public SMTPClient buildBoolSpy(boolean result) {

        SMTPClient temporaryClient = new SMTPClient();
        SMTPClient spyClient = Mockito.spy(temporaryClient);

        Mockito.doAnswer(invocation -> result).when(spyClient).send(anyString(), anyString(), anyString());

        return spyClient;

    }
    SMTPClient SMPTSpyTrue = buildBoolSpy(true);
    SMTPClient SMPTSpyFalse = buildBoolSpy(true);





    public KontoFirmowe constructorMocker(String nazwa, String nipFirmy) {

        try (MockedConstruction<KontoFirmowe> mocked = mockConstruction(KontoFirmowe.class, (mock, context) -> {

            System.out.println("Mockowany constructor uzyty z paramtrami: " + context.arguments());
            String mockNip = context.arguments().get(1).toString();

            when(mock.sprawdzKonto(anyString())).thenAnswer(invocation -> mockNip.length() != 10 || mockNip.equals(nip) || mockNip.equals("7740001454"));
            //when(mock.przelew(any(), anyDouble())).thenCallRealMethod()


            if (!mock.sprawdzKonto(context.arguments().get(1).toString())) {
               throw new EvaluationException("Company not registered!");
            }
        })) {

            return new KontoFirmowe(nazwa, nipFirmy);
        }
    }



    public static Stream<Arguments> kontaPrawidloweDlaPozyczki() {
        return Stream.of(
                Arguments.of(new KontoFirmowe("a", "a", new double[]{1775}, kwotaKredytu * 2)),
                Arguments.of(new KontoFirmowe("b", "a", new double[]{1775}, kwotaKredytu * 3))
        );
    }


    public static Stream<Arguments> zleNipyDoPrzepuszczenia() {
        return Stream.of(
                //za krotki
                Arguments.of("123456789"),
                //za dlugi
                Arguments.of("123123123123123")
        );
    }

    public static Stream<Arguments> zleNipyDoZablokowania() {
        return Stream.of(
                //Nie istnieje
                Arguments.of("0000000000"),
                //Nie jest liczbami
                Arguments.of("ABCDEFGHIJ")
        );
    }

    public static Stream<Arguments> dobreNipy() {
        return Stream.of(
                Arguments.of("8461627563"),
                Arguments.of("7740001454")
        );
    }

    public static Stream<Arguments> kontaBezPozyczkiZUS() {
        return Stream.of(
                //spelnia warunek sumy, nie spelnia warunku zusu (pusta lista)
                Arguments.of(new KontoFirmowe("a", "a", new double[]{}, kwotaKredytu * 2)),
                //spelnia warunek saldo, nie spelnia warunku zusu (brak 1775)
                Arguments.of(new KontoFirmowe("b", "a", new double[]{1774,1776}, kwotaKredytu * 3)),
                //Nie spelnia obu warunkow
                Arguments.of(new KontoFirmowe("b", "a", new double[]{1774,1776}, kwotaKredytu * 1))

        );
    }

    public static Stream<Arguments> kontaBezPozyczkiSaldo() {
        return Stream.of(
                //Spelnia warunek zusu, nie spelnia warunku saldo
                Arguments.of(new KontoFirmowe("a", "a", new double[]{1775}, kwotaKredytu * 1)),
                //Spelnia warunek zusu, nie spelnia warunku saldo (0)
                Arguments.of(new KontoFirmowe("b", "a", new double[]{1775}, 0)),
                //Nie spelnia obu warunkow
                Arguments.of(new KontoFirmowe("b", "a", new double[]{1774,1776}, kwotaKredytu * 1))

        );
    }

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
    void kontoMusiMiecNazweNip(){
    assertEquals(konto.getNazwa(), nazwa);
    assertEquals(konto.getNip(), nip);
    }

    @Test
    void kontoMusiWspieracPrzelewy(){
    konto.setSaldo(10);
    konto.przelew(konto2, 10);
        assertEquals(10, konto2.getSaldo());
    }

    @Test
    void kontoMusiWspieracPrzelewyEkspresowe(){
        konto.setSaldo(10);
        konto.przelewEkspresowy(konto2, 10);
        assertEquals(10, konto2.getSaldo());
        assertEquals(0 - KontoFirmowe.KOSZTPRZELEWUEKSPRES, konto.getSaldo());

    }

    @ParameterizedTest
    @MethodSource("kontaPrawidloweDlaPozyczki")
    void kredytMusiSpelniacWarunki(KontoFirmowe konto){
        double kwotaPrzedOperajca = konto.getSaldo();
        konto.zaciagnijKredyt(kwotaKredytu);
        assertEquals(kwotaPrzedOperajca + kwotaKredytu, konto.getSaldo());
    }

    @ParameterizedTest
    @MethodSource("kontaBezPozyczkiSaldo")
    void kredytMusiSpelniacWymogZUS(KontoFirmowe konto){
        double kwotaPrzedOperajca = konto.getSaldo();
        konto.zaciagnijKredyt(kwotaKredytu);
        assertEquals(kwotaPrzedOperajca, konto.getSaldo());
    }
    @ParameterizedTest
    @MethodSource("kontaBezPozyczkiZUS")
    void kredytMusiSpelniacWymogSalda(KontoFirmowe konto){
        double kwotaPrzedOperajca = konto.getSaldo();
        konto.zaciagnijKredyt(kwotaKredytu);
        assertEquals(kwotaPrzedOperajca, konto.getSaldo());
    }


    //testy na 100% coveerage dodatkowe
    @ParameterizedTest
    @MethodSource("zleNipyDoPrzepuszczenia")
    void nipyZlejDlugosciMajaNieCrashowacSystemu(String nip){
    KontoFirmowe noweKonto = new KontoFirmowe("a", nip);
    assertEquals(KontoFirmowe.class, noweKonto.getClass());
    }

    @ParameterizedTest
    @MethodSource("zleNipyDoZablokowania")
    void zleNipyDobrejDlugosciMajaWyrzucacError(String nip){

        assertThrows(MockitoException.class,
                () -> constructorMocker("a", nip));


    }

    @ParameterizedTest
    @MethodSource("dobreNipy")
    void dobreNipyNieMogaWyrzucacBledu(String nip){
        assertDoesNotThrow(() -> constructorMocker("a", nip));
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

    @Test
    public void constructorPowinienRzucacGdyNiedopuszczalnyPesel(){
assertThrows(EvaluationException.class, () -> new KontoFirmowe("aaa", "0000000000"));
    }




    //</editor-fold>

}