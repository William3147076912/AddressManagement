package org.example.addressmanagement;

import java.io.IOException;

public class test {
    public static void main(String[] args){
        try {
            vCardToObject.vCardfileToObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
