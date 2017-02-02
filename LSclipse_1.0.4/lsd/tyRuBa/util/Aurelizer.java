/*
 * Created on Oct 4, 2003
 */
package tyRuBa.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Aurelizer {

	public static final Aurelizer debug_sounds 
		// Say NO! no annoying sound effects
		 = null;
		// Say YES! to interesting sound effects
		//= new Aurelizer();

	Map clips = new HashMap();
	
	public Aurelizer() {
		defineClip("store","police.au");
		defineClip("load","dead.au");
		defineClip("backup","FlyinOff.au");
		defineClip("error","Buzz01.au");
		defineClip("ok","Ding.au");
		defineClip("compact","empty.au");
		defineClip("split","cork.au");
		defineClip("zero_compact","ding2.au");
		defineClip("temporizing","alarmbell.au");
		defineClip("temporizing2","gong.au");
	}

	private void defineClip(String eventName,String soundFileName) {
		Clip clip = null;
		try {
			//AudioInputStream stream = AudioSystem.getAudioInputStream(
			//	new File( "audiofile" ));

			// From URL
			URL url = getClass().getClassLoader().getResource("lib/au/" + soundFileName);
			AudioInputStream stream = AudioSystem.getAudioInputStream(url);

			// At present, ALAW and ULAW encodings must be converted
			// to PCM_SIGNED before it can be played
			AudioFormat format = stream.getFormat();
			if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				format =
					new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED,
						format.getSampleRate(),
						format.getSampleSizeInBits() * 2,
						format.getChannels(),
						format.getFrameSize() * 2,
						format.getFrameRate(),
						true);
				// big endian
				stream = AudioSystem.getAudioInputStream(format, stream);
			}

			// Create the clip
			DataLine.Info info =
				new DataLine.Info(
					Clip.class,
					stream.getFormat(),
					((int) stream.getFrameLength() * format.getFrameSize()));
			clip = (Clip) AudioSystem.getLine(info);

			// This method does not return until the audio file is completely loaded
			clip.open(stream);

			clips.put(eventName,clip);

		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (LineUnavailableException e) {
		} catch (UnsupportedAudioFileException e) {
		}
	}
	
	public static void main(String[] args) { 
		new Aurelizer();
	}
	
	public synchronized void enter(String eventName) {
		Clip clip = (Clip)clips.get(eventName);
		if (clip.isActive()) clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	public synchronized void exit(String eventName)  {
		Clip clip = (Clip)clips.get(eventName);
		clip.stop();
	}

	public synchronized void enter_loop(String eventName) {
		Clip clip = (Clip)clips.get(eventName);
		if (clip.isActive()) clip.stop();
		clip.setFramePosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

		
}
