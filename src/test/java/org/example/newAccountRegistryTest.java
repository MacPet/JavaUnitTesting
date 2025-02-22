package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;




class newAccountRegistryTest {

    //<editor-fold desc="Variables and streams">
    PrivateAccount exampleAccount = new PrivateAccount("a", "a", "10000000000", "PROM_123");
    PrivateAccount exampleAccount2 = new PrivateAccount("b", "a", "20000000000", "PROM_321");
    PrivateAccount exampleAccount3 = new PrivateAccount("b", "a", "30000000000", "PROM_321");
    public static Stream<Arguments> correctAccounts() {
        return Stream.of(
                Arguments.of(new PrivateAccount("a", "a", "10000000000", "PROM_123")),
                Arguments.of(new PrivateAccount("b", "a", "20000000000", "PROM_321")),
                Arguments.of(new PrivateAccount("c", "a", "30000000000", "PROM_000")),
                Arguments.of(new PrivateAccount("d", "a", "40000000000", "PROM_999"))

        );

    }



    //</editor-fold>

    //<editor-fold desc="Functions">



    @BeforeEach
    void setUp(){
        newAccountRegistry.clear();
    }

    @ParameterizedTest
    @MethodSource("correctAccounts")
    void registryMustSupportAdding(PrivateAccount account){

        newAccountRegistry.add(account);
        assertEquals(newAccountRegistry.getLast().getPesel(), account.getPesel(), "Account was supposed to be added to the end of the lsit, but wasnt");

    }

    @Test
    void getLastShouldReturnLast(){
        newAccountRegistry.add(exampleAccount);
        newAccountRegistry.add(exampleAccount2);
        newAccountRegistry.add(exampleAccount3);
        assertEquals(exampleAccount3.getName(), newAccountRegistry.getLast().getName());
        assertEquals(exampleAccount3.getPesel(), newAccountRegistry.getLast().getPesel());
    }

    @Test
    void registryMustSupportRemoval(){
        newAccountRegistry.add(exampleAccount);
        newAccountRegistry.add(exampleAccount2);
        newAccountRegistry.add(exampleAccount3);
        newAccountRegistry.removeByPesel(exampleAccount3.getPesel());
        System.out.println(newAccountRegistry.getLast().getPesel());

        assertEquals(exampleAccount2.getPesel(), newAccountRegistry.getLast().getPesel());
    }

    @Test
    void clearMustClear(){

        newAccountRegistry.add(exampleAccount);
        newAccountRegistry.clear();
        assertEquals(0, newAccountRegistry.getLength(), "The clear() function didnt reset the accounts registry.");

    }

    @Test
    void registryMustSupportPeselSearch(){

        newAccountRegistry.add(exampleAccount);
        newAccountRegistry.add(exampleAccount2);
        assertEquals(exampleAccount.getPesel(), newAccountRegistry.getByPesel(exampleAccount.getPesel()).get(0).getPesel(), "The getByPesel() function returned incorrect account or a wrong data format.");

    }

    @Test
    void incorrectPeselSearchShouldReturnEmptyList(){
        assertEquals(0,newAccountRegistry.getByPesel("00000000000").size(), "Incorrect getByPesel should have returned 0 but didnt >:(");
    }

    @Test
    void registryMustHaveACounter(){
        assertEquals(0, newAccountRegistry.getLength(),  "getLength() method was supposed to return 0 but instead returned " + newAccountRegistry.getLength());
        newAccountRegistry.add(exampleAccount);
        assertEquals(1, newAccountRegistry.getLength(),  "getLength() method was supposed to return 1 but instead returned " + newAccountRegistry.getLength());
        newAccountRegistry.add(exampleAccount2);
        assertEquals(2, newAccountRegistry.getLength(), 2, "getLength() method was supposed to return 2 but instead returned " + newAccountRegistry.getLength());
    }

  @Test
  void updateShouldUpdateDatabase(){
        double newAmount = 100100;
    newAccountRegistry.add(exampleAccount);
    exampleAccount.setBalance(newAmount);
    newAccountRegistry.update(exampleAccount);
    PrivateAccount accountFromDB = newAccountRegistry.getByPesel(exampleAccount.getPesel()).get(0);
    assertEquals(newAmount, accountFromDB.getBalance());
  }



    @Nested
    class databaseCrashTests {

        private static MockedStatic<DriverManager> mockedDriverManager;

        @BeforeAll
        public static void setupDriverError(){
            mockedDriverManager = mockStatic(DriverManager.class);

            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenAnswer(invocation -> { throw new SQLException("mock crash");});
        }

        @AfterAll
        public static void removeDBMock(){ mockedDriverManager.close();}


        @Test
        void getLengthShouldReturnMinus1OnError(){
            assertDoesNotThrow(newAccountRegistry::getLength);
            assertEquals(-1, newAccountRegistry.getLength(),  "getLength() method was supposed to return 0 but instead returned " + newAccountRegistry.getLength());
        }

        @Test
        void getLastlDBerrorShouldntCrashSystem(){
        assertDoesNotThrow(newAccountRegistry::getLast);
        }

        @Test
        void getByPeselDBerrorShouldntCrashSystem(){
            assertDoesNotThrow(() -> newAccountRegistry.getByPesel(""));
        }

        @Test
        void updateDBerrorShouldntCrashSystem(){
            assertDoesNotThrow(() -> newAccountRegistry.update(exampleAccount));
        }

        @Test
        void removeByPeselDBerrorShouldntCrashSystem(){
            assertDoesNotThrow(() -> newAccountRegistry.removeByPesel(""));
        }

        @Test
        void addDBerrorShouldntCrashSystem(){
            assertDoesNotThrow(() -> newAccountRegistry.add(exampleAccount));
        }
    }





    //</editor-fold desc="Functions">

}