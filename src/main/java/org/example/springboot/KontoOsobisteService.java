package org.example.springboot;

import org.example.PrivateAccount;

public interface KontoOsobisteService {

    public PrivateAccount saveUpdatePerson(PrivateAccount konto);
    public PrivateAccount findPersonById(Integer id);
}