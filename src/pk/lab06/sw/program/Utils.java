package pk.lab06.sw.program;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.CompoundControl;


public class Utils {

	public static final int PACKAGE_PANEL_ASK_FOR_VOLUME = 1;
	public static final int PACKAGE_PANEL_ASK_FOR_SYSTEM_USAGE = 2;
	public static final int PACKAGE_PANEL_ASK_FOR_SYSTEM_INFO = 3;
	public static final int PACKAGE_PANEL_ASK_FOR_BUTTONS = 4;
	public static final int PACKAGE_PANEL_SET_VOLUME = 10;
	public static final int PACKAGE_PANEL_USE_BUTTON = 11;

	public static final int PACKAGE_HOST_ASK_FOR_VOLUME = 64;
	public static final int PACKAGE_HOST_SYSTEM_INFO = 74;
	public static final int PACKAGE_HOST_SEND_VOLUME = 75;
	public static final int PACKAGE_HOST_SEND_SYSTEM_USAGE = 76;
	public static final int PACKAGE_HOST_SEND_COLOR = 77;
	public static final int PACKAGE_HOST_SEND_BUTTON_INFO = 78;

	public static final int PACKAGE_PING = 128;
	public static final int PACKAGE_PONG = 129;

	private static boolean b = true;


	public static void clearStream(InputStream is){
		new Thread(()->{
			//noinspection CatchMayIgnoreException
			try {
				//noinspection StatementWithEmptyBody
				while (is.read()!=-1);
			}catch (IOException er){}
		});
	}

	public static FloatControl getVolumeControl(Mixer mixer){
		for (Line.Info targetLineInfo : mixer.getTargetLineInfo()) {
			try {
				Line line = mixer.getLine(targetLineInfo);

				if (!line.isOpen()) line.open();
				if (line.isControlSupported(FloatControl.Type.VOLUME)) {
					return (FloatControl) line.getControl(FloatControl.Type.VOLUME);
				}
			}catch (LineUnavailableException er){
				er.printStackTrace();
			}
		}
		return null;
	}
	public static FloatControl getVolumeControl(){
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info mixerInfo : mixerInfos) {
			Mixer mixer = AudioSystem.getMixer(mixerInfo);
			return getVolumeControl(mixer);
		}
		return null;
	}

	public static void setVolume(int volume) {
		System.out.println("Dźwięk: " + volume);
		FloatControl control = getVolumeControl();
		if(control != null) {
			float volumeValue = (volume / 100.0f) * control.getMaximum() + control.getMinimum();
			control.setValue(volumeValue);
			log("\tZmiana dzwieku na: " + volumeValue + " ( " + control.getMinimum() + " - " + control.getMaximum() + ")");
		}
	}

	public static int getVolume(){

		FloatControl control = getVolumeControl();
		if(control != null) {
			return (int) (((control.getValue() - control.getMinimum()) / control.getMaximum()) * 100);
		}
		return 0;
	}
	
	public static byte[] emptyPacket(){
		return new byte[]{0,0,0,0,0,0,0,0};
	}
	
	public static byte[] emptyPacket(byte type){
		return new byte[]{type,0,0,0,0,0,0,0};
	}
	
	public static void send(byte [] bytes, OutputStream os) throws IOException {
			os.write(bytes);
	}
	
	public static int receive(byte [] data, InputStream is) throws IOException {
		if (is.available() < 8) return 0;
		return is.read(data, 0, 8);
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
	
	public static void showLogs(boolean b) {
		Utils.b = b;
	}
	
	public static void log(String str) {
		if(b) {
			System.out.println(str);
		}
	}


	public static String[] allMatched(Matcher m){
		ArrayList<String> matched = new ArrayList<String>();
		while (m.find()){
			matched.add(m.group());
		}
		return matched.toArray(new String[0]);
	}

	/**
	 * @return {current temperature , critical temperature}
	 */
	public static double[] getCurrTemp(){
		try {
			Process prc = Runtime.getRuntime().exec("sensors");
			BufferedReader br = new BufferedReader(new InputStreamReader(prc.getInputStream()));

			double curr = 0;
			double crit = 0;
			int n = 0;
			String line;
			Pattern pattern = Pattern.compile("(?<=[+-])([\\w\\d.]*)(?=°C)");
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("Core")) continue;
				Matcher m = pattern.matcher(line);
				String[] tmps = allMatched(m);
				if (tmps.length >= 3) {
					curr += Double.parseDouble(tmps[0]);
					crit += Double.parseDouble(tmps[2]);
					++n;
				}
			}
			prc.waitFor();
			curr /= n;
			crit /= n;
			return new double[] {curr, crit};
		}catch (IOException | InterruptedException er){
			return new double[] {0,0};
		}
	}


	/**
	 * @return {free, used, max} in MB
	 */
	public static long[] getMemoryUsageMb(){
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		long max =
				operatingSystemMXBean.getTotalPhysicalMemorySize()
						+ operatingSystemMXBean.getTotalSwapSpaceSize();

		long free =
				operatingSystemMXBean.getFreePhysicalMemorySize()
						+ operatingSystemMXBean.getFreeSwapSpaceSize();

		long used = max - free;

		long maxMb = max/1024/1024;
		long freeMb = free/1024/1024;
		long usedMb = used/1024/1024;

		return new long[] {freeMb, usedMb, maxMb};
	}

}




