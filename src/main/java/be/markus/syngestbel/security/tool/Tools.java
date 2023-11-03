package be.markus.syngestbel.security.tool;

import java.security.SecureRandom;
import java.util.Arrays;

public class Tools {

    public String randomHexadecimal(int size){

        if(size<=0) throw new IllegalArgumentException("Size must be greater than 0");

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[size/2];
        random.nextBytes(bytes);

        StringBuilder hexaString = new StringBuilder();
        for(byte b: bytes){
            hexaString.append(String.format("%o2X",b));
        }

        return hexaString.toString();

    }

}
