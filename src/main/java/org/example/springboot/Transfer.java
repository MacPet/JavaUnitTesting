package org.example.springboot;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class Transfer {

public double amount;
public String type;
public static List<String> ACCEPTABLE_TYPES = Arrays.asList("incoming", "outgoing", "express");


//Potrzebne dla springboota
//public Transfer(){}
public Transfer(String type, double amount){
this.type = type;
this.amount = amount;

}


}
