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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class KontoOsobisteTest {

//<editor-fold desc="Dane i streamy danych">

    // wygląda na to, że wartosci niestatyczne w testach sa ustawiane do defaultowych z kazdym testem, nie ma wiec powodu
    //ustawiac ich z pomoca setUpa

    public SMTPClient SMTPSpyFactory(boolean result) {

        SMTPClient temporaryClient = new SMTPClient();
        SMTPClient spyClient = spy(temporaryClient);

        doAnswer(invocation -> result).when(spyClient).send(anyString(), anyString(), anyString());

        return spyClient;

    }

    private static List<String> blackList = new ArrayList<>();

    private static MockedStatic<BlackList> mockedBlackList;
    @BeforeAll
    public static void setupBlacklistMock() {

        //Mocki potrzebne aby unkinąć kontaktu z bazą danych podczas zaciągania kredytu
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


    SMTPClient SMPTSpyTrue = SMTPSpyFactory(true);
    SMTPClient SMPTSpyFalse = SMTPSpyFactory(true);


    String imie = "Janusz";
    String nazwisko = "Kowalski";

    String pesel = "70345678911";

    String kodRabatowy = "PROM_123";

    String  wiadomoscBlednyPesel = "Niepoprawny pesel!";

    //konta dla testów które nie potrzebują parametryowanych inputów, bo testują prostą rzecz
    KontoOsobiste konto = new KontoOsobiste(imie, nazwisko, pesel, kodRabatowy);
    KontoOsobiste konto2 = new KontoOsobiste(imie, nazwisko, pesel);


    double bonusRabatowy = KontoOsobiste.BONUSRABATOWY;
    static double kwotaKredytu = 100;

    double kwotaPrzelewuTestowegoPozyczka = 1;





    public static Stream<Arguments> kontaNieprawidlowePesele() {
        return Stream.of(
                //dobra dlugosc, zly typ
                Arguments.of(new KontoOsobiste("a", "a", "ABCDEABCDEA")),
                //za mala dlugosc
                Arguments.of(new KontoOsobiste("b", "a", "0123456789", "PROM_321")),
                //za duza dlugosc
                Arguments.of(new KontoOsobiste("c", "a", "012345678901", "PROM_999")),
                //pusty string
                Arguments.of(new KontoOsobiste("d", "a", "", "PROM_000"))

    
        );
    }

    public static Stream<Arguments> kontaPrawidlowePesele() {
        return Stream.of(
                Arguments.of(new KontoOsobiste("a", "a", "12312312311")),
                Arguments.of(new KontoOsobiste("b", "a", "32132132132", "PROM_321")),
                Arguments.of(new KontoOsobiste("c", "a", "12345678901", "PROM_321"))



                );
    }

    public static Stream<Arguments> kontaNiekontaPrawidloweKuponyRabatowe() {
   return Stream.of(
           //kupony ze zlym kodem
           Arguments.of(new KontoOsobiste("a", "a", "71111111111", "PROM_")),
           Arguments.of(new KontoOsobiste("b", "a", "88111111111", "PROM_1")),
           Arguments.of(new KontoOsobiste("c", "a", "991111111111", "PROM_12")),
           Arguments.of(new KontoOsobiste("d", "a", "88111111111", "PROM_1234")),
           Arguments.of(new KontoOsobiste("e", "a", "66111111111", "PROM123")),
           Arguments.of(new KontoOsobiste("f", "a", "65111111111", "123")),
           //kupony z dobrym kodem ale zlym peselem
           Arguments.of(new KontoOsobiste("g", "a", "30111111111", "PROM_123")),
           Arguments.of(new KontoOsobiste("h", "a", "59111111111", "PROM_123")),
           Arguments.of(new KontoOsobiste("a", "a", "31111111111", "PROM_123"))
   );

    }

    public static Stream<Arguments> kontaPrawidloweKuponyRabatowe() {
        return Stream.of(
                Arguments.of(new KontoOsobiste("a", "a", "71111111111", "PROM_123")),
                Arguments.of(new KontoOsobiste("b", "a", "60111111111", "PROM_321")),
                Arguments.of(new KontoOsobiste("c", "a", "99111111111", "PROM_000")),
                Arguments.of(new KontoOsobiste("d", "a", "88111111111", "PROM_999"))

        );

    }

    public static Stream<Arguments> kontaPrawidloweDlaPozyczki() {
        return Stream.of(
                //spelnia warunek sumy
                Arguments.of(new KontoOsobiste("a", "a", "71111111111", "", 0, new double[]{-1, -1, -1, -1, 5000}, null)),
                //spelnia warunek 3 transakcji
                Arguments.of(new KontoOsobiste("b", "a","", "61111111111", 0, new double[]{1,1,1}, null))

        );

    }
    public static Stream<Arguments> kontaBezPozyczkiSuma() {
        return Stream.of(
                //Suma równa 0
                Arguments.of(new KontoOsobiste("a", "a","", "11111111111", 0, new double[]{1, -1, 1, -1, 1, -1}, null)),
                //Suma większa niż kwota ale mniej niż 5 transakcji
                Arguments.of(new KontoOsobiste("b", "a","", "11111111111", 0, new double[]{1, 1, -1, kwotaKredytu}, null)),
                //pusta historia
                Arguments.of(new KontoOsobiste("c", "a","", "11111111111", 0, new double[]{}, null))


                );

    }
    public static Stream<Arguments> kontaBezPozyczkiTransakcje() {
        return Stream.of(
                //same negatywny
                Arguments.of(new KontoOsobiste("a", "a","", "11111111111", 0,  new double[]{-1, -1, -1}, null)),
                //3 pozytywne ogółem, lecz nie razem
                Arguments.of(new KontoOsobiste("a", "a","", "11111111111", 0, new double[]{1,-1, 1, -1,1}, null)),
                //3 pozytywne lecz nie ostatnie 3
                Arguments.of(new KontoOsobiste("a", "a", "","11111111111",0,  new double[]{1,1,1,-1}, null)),
                //pusta historia
                Arguments.of(new KontoOsobiste("a", "a","", "11111111111", 0, new double[]{}, null))

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
    public void kontoMusiMiecImieNazwiskoSaldo(){
        assertEquals(imie, konto.getImie(), "Konto ma niepoprawnie imie");
        assertEquals( nazwisko, konto.getNazwisko(), "konto ma niepoprawne nazwisko");
        assertTrue(konto.getSaldo() == 0 || (konto.getSaldo() == bonusRabatowy && konto.uzytoKuponu), "Saldo początkowe nie wyniosło 0");
    }


    @ParameterizedTest
    @MethodSource("kontaNieprawidlowePesele")
    void kontoMusiMiecPeselOdpowiedniejDlugosciIBycNumeryczny(KontoOsobiste konto){
    assertEquals(wiadomoscBlednyPesel, konto.getPesel(), "");
    }

    @ParameterizedTest
    @MethodSource("kontaPrawidlowePesele")
    void kontoZDobrymPeselemMusiTenPeselZachowac(KontoOsobiste konto){
        assertNotEquals(wiadomoscBlednyPesel, konto.getPesel(), "Pesel konta " + konto.getImie() + " został zmieniony, a nie powinien był być.");
    }





    @ParameterizedTest
    @MethodSource("kontaPrawidloweKuponyRabatowe")
    void peselMusiBycNumeryczny(){
        assertTrue(konto.getPesel().matches("[0-9]{11}"), "Pesel nie składał się z liczb!");
    }

    @ParameterizedTest
    @MethodSource("kontaPrawidloweKuponyRabatowe")
    void dobryKodMusiZwiekszacStanKonta(){

        assertEquals(bonusRabatowy, konto.getSaldo());

    }

    @ParameterizedTest
    @MethodSource("kontaNiekontaPrawidloweKuponyRabatowe")
    void kodRabatowyNieMozeMiecZlegoRegexu(KontoOsobiste kontoZlyKupon) {
        assertEquals(0, kontoZlyKupon.getSaldo());
    }



    @Test
    void kontoMusiWspieracPrzelewyEkspresowe(){
        konto.setSaldo(kwotaPrzelewuTestowegoPozyczka);
        System.out.println(konto.saldo);
        konto2.setSaldo(0);
        konto.przelewEkspresowy(konto2, kwotaPrzelewuTestowegoPozyczka);
        assertEquals(kwotaPrzelewuTestowegoPozyczka, konto2.getSaldo());
        assertEquals(-KontoOsobiste.KOSZTPRZELEWUEKSPRES, konto.getSaldo());
    }

    @ParameterizedTest
    @MethodSource("kontaPrawidloweDlaPozyczki")
    void kredytMusiSpelniacWarunki(KontoOsobiste konto){
    konto.zaciagnijKredyt(kwotaKredytu);
    assertEquals(kwotaKredytu, konto.getSaldo());
    }

    @Test
    void kredytNieMozePrzejscJesliPeselJestZablokowany() {
        BlackList.addAccountToBlackList(konto.getPesel(), "aaa");
        System.out.println(Arrays.toString(blackList.toArray()));
        konto.zaciagnijKredyt(kwotaKredytu);
        assertNotEquals(kwotaKredytu, konto.getSaldo());
    }


    @Test
    void kontoEqualsJestTrueDlaSiebie(){
    assertTrue(konto.equals(konto));
    }

    @Test
    void kontOEqualsJestTrueDlaKopiDanych(){
    KontoOsobiste kopia = new KontoOsobiste(imie, nazwisko, pesel, kodRabatowy, konto.getSaldo(), konto.getHistoria(), konto.getDatabaseID());
    assertTrue(kopia.equals(konto));
    }

    @ParameterizedTest
    @MethodSource("kontaBezPozyczkiTransakcje")
    void kredytNieSpelniaWymogu3Transakcji(KontoOsobiste konto){
        konto.zaciagnijKredyt(kwotaKredytu);
        assertEquals(0, konto.getSaldo());
    }
    @ParameterizedTest
    @MethodSource("kontaBezPozyczkiSuma")
    void kredytNieSpelniaWymoguSumy(KontoOsobiste konto){
        konto.zaciagnijKredyt(kwotaKredytu);
        assertEquals(0, konto.getSaldo());
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
    public void getKosztPrzelewuEKSPRESSReturnsPrivateAccountsValue(){
        assertEquals(1, konto.getKOSZTPRZELEWUEKSPRES());
    }

    @Test
    public void setDatabaseIDShouldTHrowWhenIDLessThan0(){
        assertThrows(EvaluationException.class, () -> konto.setDatabaseID(-1));
    }

    @Test
    public void setDatabaseID(){
        int newID = 555888;
        konto.setDatabaseID(newID);
        assertEquals(newID, konto.getDatabaseID());
    }

    @Test
    public void setImie(){
        String noweImie = "jakub";
        konto.setImie(noweImie);
        assertEquals(noweImie, konto.getImie());
    }

    @Test
    public void setNazwisko(){
        String noweNazwisko = "kowal";
        konto.setNazwisko(noweNazwisko);
        assertEquals(noweNazwisko, konto.getNazwisko());
    }



//</editor-fold>

}

