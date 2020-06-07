package A;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.math.BigInteger;

public class Utils {
	public static byte[] emptyPacket(){
		byte[] packet = {0,0,0,0,0,0,0,0};
		return packet;
	}
	
	public static byte[] emptyPacket(byte type){
		byte[] packet = {type,0,0,0,0,0,0,0};
		return packet;
	}
	
	public static void send(byte [] bytes, OutputStream os) throws IOException {
			os.write(bytes);
	}
	
	public static byte[] receive(byte [] data, InputStream is) throws IOException {

		int r = is.read(data, 0, 8);
		if(r == -1) return data;

		return data;
	}
	
	public static int byteToInt(byte hb, byte lb) {
		return ((int)hb << 8) | ((int)lb & 0xFF);
	}
	
	public static int byteToInt(byte[] bytes) {
		int ret = 0;
		for (int i=0; i<4 && i < bytes.length; i++) {
			ret <<= 8;
			ret |= (int)bytes[i] & 0xFF;
		}
		return ret;
	}
	public static int readableByte(byte b) {
		return ((int)b&0xFF);
	}
	public static byte[] intToBytes(int value) {
		return BigInteger.valueOf( value ).toByteArray();
	}
}