package fi;

import java.util.zip.CRC32;

public class CRCTest {

    public static void main(String[] args) {
	    CRC32 crc32_1 = new CRC32();
	    CRC32 crc32_2 = new CRC32();

	    crc32_1.update("A".getBytes());
	    crc32_1.update("B".getBytes());
	    crc32_1.update("C".getBytes());

	    
	    crc32_2.update("C".getBytes());
	    crc32_2.update("B".getBytes());
	    crc32_2.update("A".getBytes());

	    System.out.println(crc32_1.getValue() +" "+crc32_2.getValue());
    }

}
