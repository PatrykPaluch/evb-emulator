package pk.lab06.sw.program;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.CompoundControl;


public class Utils {
	
	boolean b;
	
	public static void setVolume(int volume) {
		try {
			
			Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
			for (int i = 0; i < mixerInfos.length; i++)
			{
				Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);

				Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

				for (int j = 0; j < targetLineInfos.length; j++) {
					Line line = AudioSystem.getLine(targetLineInfos[j]);
					line.open();
					FloatControl control = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
					control.setValue( (float)(volume/100.0) );
					line.close();
				}

			}
		  
		}
		catch (Exception e) {
			return;
		}
	}
	
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

		if (is.available() < 8) return data;
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
	
	public static int byteToIntInv(byte [] bytes) {
		int ret = 0;
		for (int i=bytes.length-1; i>=0; i--) {
			ret <<= 8;
			ret |= (int)bytes[i] & 0xFF;
		}
		return ret;
	}
	
	public static int readableByte(byte b) {
		return ((int)b&0xFF);
	}
	
	public static byte[] intToBytes(int value) {
		byte [] bytes = BigInteger.valueOf( value ).toByteArray();
		byte [] ret;
		if (bytes.length < 4) {
			int l = bytes.length;
			byte [] tmp = bytes;
			ret = new byte[4];
			int do_dodania = 4 - l;
			for (int i=0; i<do_dodania; i++) {
				ret[i] = 0;
			}
			for (int i=0; i<tmp.length; i++) {
				ret[do_dodania+i] = tmp[i];
			}
		}
		else ret = bytes;
		return ret;
	}
	
	public static String centerText(String text, int width) {
		
		String tmp = "";
		for(int i=0; i< width/2 - text.length()/2; i++) {
			tmp = tmp + " ";
		}
		String ret = tmp + text + tmp;
		return ret;
	}
	
	private static Control findControl(Type type, Control... controls) {
		if (controls == null || controls.length == 0) return null;
		for (Control control : controls) {
			if (control.getType().equals(type)) return control;
			if (control instanceof CompoundControl) {
				CompoundControl compoundControl = (CompoundControl) control;
				Control member = findControl(type, compoundControl.getMemberControls());
				if (member != null) return member;
			}
		}
		return null;
	}

	public static long colorDifference(long r1, long g1, long b1, long r2, long g2, long b2) {
		return abs(r1 - r2) + abs(g1 - g2) + abs(b1 - b2);
	}
	public static long abs(long i) {
		if (i < 0) return -i;
		return i;
	}
	
	public void showLogs(boolean b) {
		this.b = b;
	}
	
	public void log(String str) {
		if(b) {
			System.out.println(str);
		}
	}
}




