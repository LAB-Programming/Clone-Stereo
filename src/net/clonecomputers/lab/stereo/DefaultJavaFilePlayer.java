package net.clonecomputers.lab.stereo;

import java.io.*;
import java.util.*;

import javax.sound.sampled.*;

public class DefaultJavaFilePlayer implements SoundPlayer {

	List<SoundPlayerListener> listenerlist = new LinkedList<SoundPlayerListener>();
	
	File[] songFiles;
	int songindex;
	private AudioInputStream song;

	private final Clip player;

	public DefaultJavaFilePlayer(File f) {
		openFolder(f);
		Clip c=null;
		try {
			c=AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		player=c;
		player.addLineListener(new LineListener(){

			@Override
			public void update(LineEvent event) {
				if(event.getType() == LineEvent.Type.STOP){
					for(SoundPlayerListener l: listenerlist){
						l.trackEnded(new SoundPlayerEvent(this));
					}
				}
			}

		});
	}

	private void openFolder(File f){
		songFiles = acceptableSongFiles(f).toArray(new File[0]);
	}

	LinkedList<File> acceptableSongFiles(File searchDirectory){
		File[] fa = searchDirectory.listFiles();
		LinkedList<File> acceptableFiles = new LinkedList<File>();
		for(File f: fa){
			if(f.isDirectory()) acceptableFiles.addAll(acceptableSongFiles(f));
			String name = f.getName();
			String[] sa = name.split("\\.");
			System.out.println(Arrays.toString(sa));
			if(sa.length < 2) continue; // needs extention
			String ending = sa[sa.length-1];
			System.out.println(ending);
			boolean goodEnding = false;
			for(String ext: new String[]{
					"aifc",
					"aif",
					"aiff",
					"au",
					"snd",
					"wav",
					//"mp3",
			}){
				//System.out.println(ext);
				if(ending.equalsIgnoreCase(ext)) goodEnding = true;
			}
			boolean readableSoundFile = f.canRead() && f.isFile() && !name.startsWith(".") && goodEnding;
			if(readableSoundFile) System.out.println("Accepting " + name);
			else System.out.println("Rejecting " + name);
			if(readableSoundFile) acceptableFiles.addLast(f);
		}
		return acceptableFiles;
	}

	@Override
	public void init() {
		// TODO Implement this
	}

	@Override
	public void play() {
		try {
			song = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(songFiles[songindex])));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedAudioFileException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void next() {
		// TODO Auto-generated method stub

	}

	@Override
	public void previous() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTrackName() {
		// TODO Auto-generated method stub
		return songFiles[songindex].getName();
	}

	@Override
	public int getTrackLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTrackPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTrackPos(int newPos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSoundPlayerListener(SoundPlayerListener l) {
		listenerlist.add(l);

	}

	@Override
	public void removeSoundPlayerListener(SoundPlayerListener l) {
		listenerlist.remove(l);

	}

	@Override
	public SoundPlayerListener[] getSoundPlayerListeners() {
		return listenerlist.toArray(new SoundPlayerListener[0]);
	}

}
