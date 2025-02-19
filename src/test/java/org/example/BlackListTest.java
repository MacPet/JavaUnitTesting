package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class BlackListTest {
    //Na chwilę obecną zostawiam testy catcha, próbowałem je też uwzględnić ale nie mam pomysłu jak tego dokonać.


    String examplePesel = "09876543210";
    String exampleReason = "This is an example reason.";



    @BeforeEach
    void setUp(){
        BlackList.clear();
    }


    @Test
    void blackListMustSupportAdding(){
    BlackList.addAccountToBlackList(examplePesel, exampleReason);
    assertEquals(1, BlackList.getLength());
    }

    @Test
    void getReasonReturnsCorrectReason(){
        BlackList.addAccountToBlackList(examplePesel, exampleReason);
        assertEquals(exampleReason, BlackList.getReason(examplePesel));
    }

    @Test
    void isOnBlackListReturnsTrueIfPresent(){
        BlackList.addAccountToBlackList(examplePesel, exampleReason);
        assertTrue(BlackList.isAccountOnBlackList(examplePesel));
    }

    @Test
    void IsOnBlackListReturnsFalseIfNotPresent(){
        assertFalse(BlackList.isAccountOnBlackList(examplePesel));
    }

    @Test
    void clearMustClear(){
        BlackList.addAccountToBlackList(examplePesel, exampleReason);
        BlackList.clear();
        assertEquals(0, BlackList.getLength());
    }

    @Test
    void removeMustRemove(){
        BlackList.addAccountToBlackList(examplePesel, exampleReason);
        BlackList.remove(examplePesel);
        assertEquals(0, BlackList.getLength());
    }

    @Test
    void updateMustUpdate(){
        String newReason = "I just dont like them.";
        BlackList.addAccountToBlackList(examplePesel, exampleReason);
        BlackList.update(examplePesel, newReason);
        assertEquals(newReason, BlackList.getReason(examplePesel));

    }





}