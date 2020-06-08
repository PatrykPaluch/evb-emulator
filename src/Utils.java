import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
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
	
	ControlTest audio;
	public Utils() {
		audio = new ControlTest();
	}
	
	public void setVolume(int volume) {
		audio.setVolume(volume);
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
}

class ControlTest {

   public static boolean setVolume(int value) {

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

					control.setValue( value );
					line.close();
				}

			}
		  
		}
		catch (Exception e) {
			return false;
		}
		return true;
    }

}




