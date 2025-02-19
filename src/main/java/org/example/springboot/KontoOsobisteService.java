package org.example.springboot;

import org.example.KontoOsobiste;

public interface KontoOsobisteService {

    public KontoOsobiste saveUpdatePerson(KontoOsobiste konto);
    public KontoOsobiste findPersonById(Integer id);
}