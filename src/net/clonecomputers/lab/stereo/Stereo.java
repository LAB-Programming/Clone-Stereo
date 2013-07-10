package net.clonecomputers.lab.stereo;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.sound.sampled.*;

import java.util.*;

import org.tritonus.lowlevel.cdda.*;
import org.tritonus.share.sampled.file.*;

/* goals:
 * fix issue that it can't open/play any audio file
 * use gracenote or similar database to recognize cd and track names
 */

public class Stereo extends JPanel{

	public static final String CD_MOUNT_LOC = "/dev/"; // "/media/" ?  // on MacOS "/Volumes/"
	
	public final ImageIcon play = new ImageIcon(getClass().getResource("resources/play.png"));
	public final ImageIcon pause = new ImageIcon(getClass().getResource("resources/pause.png"));
	public final ImageIcon back = new ImageIcon(getClass().getResource("resources/back.png"));
	public final ImageIcon forward = new ImageIcon(getClass().getResource("resources/forward.png"));
	
	private SoundPlayer sp = new DummyPlayer();
	
//	private static CddaMidLevel cdda;
//	static{
//		try{
//			cdda = CddaUtils.getCddaMidLevel();
//		}catch(UnsatisfiedLinkError e){
//			System.err.println("please put libtritonuscdparanoia.so in " + 
//					System.getProperty("java.ext.dirs").replaceAll(":", "\nOR\n"));
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
//	private String dir;
//	//private InputStream toc;
//	private Track[] tracks; // only exists in cd mode
//	private File[] songFiles; // only exists in file mode
//	private int numTracks; // don't seem to need tracks, so use this to keep track of how many there are
//	private int songindex=0;
//	private AudioInputStream song;
//	
//	private final Clip player;

	private final JFrame window;
	
	private final JPanel playerPanel = new JPanel(new BorderLayout());
	
	private final JButton playpause = new JButton(play);
	private final JButton rewind = new JButton(back);
	private final JButton fastforward = new JButton(forward);
	private final JSlider progressbar = new JSlider(0,100,0);
	private final JLabel trackname = new JLabel("song here",JLabel.CENTER);
	
	private final JPanel openPanel = new JPanel();
	
	private final JButton openCD = new JButton("Open CD");
	private final JButton openFolder = new JButton("Open Folder");
	private final JButton openURL = new JButton("Open URL");
	private final JFileChooser fc = new JFileChooser();

//	private File[] oldcds;
	
	public boolean playing;
	
//	private boolean isCD = false;
	
	public static void main(String[] args) {
		JFrame window = new JFrame("Clone Stereo");
		Stereo s = new Stereo(window);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(s);
		s.initGUI();
		window.pack();
		window.setVisible(true);
	}
	
	public void updatePlaceInSong(double progressBarValue){
		double fractionDone = progressBarValue * (progressbar.getMaximum() - progressbar.getMinimum());
		sp.setTrackPos((int) (fractionDone * sp.getTrackLength()));
	}
	
	private void updateProgressBar() {
		double fractionDone = sp.getTrackPos()/sp.getTrackLength();
		progressbar.setValue((int)fractionDone*(progressbar.getMaximum()-progressbar.getMinimum()));
	}
	
	public void togglePlay(){
		playing=!playing;
		if(!playing){
			sp.pause();
		}else{
			sp.play();
		}
		playpause.setIcon(playing? pause: play);
//		if(!playing) player.stop();
//		else{
//			/*try {
//				player.open(song);
//			} catch (LineUnavailableException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}*/
//			player.start();
//			updatePlaceInSong(progressbar.getValue());
//		}
	}
	
	public void setSoundPlayer(SoundPlayer newSoundPlayer){
		SoundPlayerListener l = null;
		if(sp != null && sp.getSoundPlayerListeners().length > 0){
			l = sp.getSoundPlayerListeners()[0];
		}
		sp = newSoundPlayer;
		if(l != null) sp.addSoundPlayerListener(l);
		sp.init();
		trackname.setText(sp.getTrackName());
	}
	
	
	private void initGUI() {
		/*songs=cd.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File f, String name) {
				return name.contains(".aiff");
			}
			
		});*/
		//this.removeAll();
		openCD.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				setSoundPlayer(new CdPlayer(window));
			}
			
		});
		openFolder.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				fc.showOpenDialog(Stereo.this);
				setSoundPlayer(new DefaultJavaFilePlayer(fc.getSelectedFile()));
			}
			
		});
		progressbar.addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				updatePlaceInSong(progressbar.getValue());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				// do nothing
			}
			
		});
		progressbar.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent e) {
				updatePlaceInSong(progressbar.getValue());
			}
			
		});
		playpause.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized(Stereo.this){
					togglePlay();
					playpause.setIcon(playing? pause: play);
				}
			}
			
		});
		fastforward.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				sp.next();
			}
			
		});
		rewind.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				sp.previous();
			}
			
		});
		playpause.setBackground(Color.WHITE);
		rewind.setBackground(Color.WHITE);
		fastforward.setBackground(Color.WHITE);
		playpause.setPreferredSize(new Dimension(64,64));
		rewind.setPreferredSize(new Dimension(64,64));
		fastforward.setPreferredSize(new Dimension(64,64));
		playerPanel.add(playpause,BorderLayout.CENTER);
		playerPanel.add(rewind,BorderLayout.WEST);
		playerPanel.add(fastforward,BorderLayout.EAST);
		playerPanel.add(progressbar,BorderLayout.SOUTH);
		playerPanel.add(trackname,BorderLayout.NORTH);
		openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.Y_AXIS));
		openPanel.add(openCD);
		openPanel.add(openFolder);
		openPanel.add(openURL);
		this.setLayout(new BorderLayout());
		this.add(playerPanel, BorderLayout.CENTER);
		this.add(openPanel, BorderLayout.EAST);
	}

	public Stereo(JFrame window){
		this.window = window;
		sp.addSoundPlayerListener(new SoundPlayerListener() {
			
			@Override
			public void trackPositionUpdated(SoundPlayerEvent e) {
				updateProgressBar();
			}

			@Override
			public void trackEnded(SoundPlayerEvent e) {
				sp.next();
			}
		});
		setSoundPlayer(new DummyPlayer());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		Clip c=null;
//		try {
//			c=AudioSystem.getClip();
//		} catch (LineUnavailableException e) {
//			e.printStackTrace();
//		}
//		player=c;
//		player.addLineListener(new LineListener(){
//
//			@Override
//			public void update(LineEvent event) {
//				if(event.getType() == LineEvent.Type.STOP){
//					songindex=(songindex+1+numTracks)%numTracks;
//					startPlayingFromBeginningOfSong();
//				}
//			}
//			
//		});
//		new Thread(){
//			@Override
//			public void run(){
//				while(true){
//					synchronized(Stereo.this){
//						if(player.isRunning()) updateProgressBar();
//					}
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						throw new RuntimeException(e);
//					}
//				}
//			}
//		}.start();
		//this.add(new JLabel("Waiting for CD"));
	}

//	private String[] parseTOC(InputStream toc) throws IOException{
//	LinkedList<String> ll = new LinkedList<String>();
//	while(true){
//		StringBuilder sb = new StringBuilder();
//		while(true){
//			char c = (char)toc.read();
//			if(c == -1){
//				ll.removeFirst();
//				ll.removeLast();
//				return ll.toArray(new String[0]);
//			}
//			if(c != '\n') sb.append(c);
//			else{
//				ll.addLast(sb.toString());
//				break;
//			}
//		}
//	}
//}

//private void startPlayingFromBeginningOfSong(){
//	if(isCD) try {
//		song=cdda.getTrack(dir, songindex);
//	} catch (IOException e1) {
//		e1.printStackTrace();
//	} else {
//		File songFile = songFiles[songindex];
//		String[] sa = songFile.getName().split("\\.");
//		StringBuilder trackName = new StringBuilder();
//		for(int i = 0; i < sa.length - 1; i++){
//			trackName.append(sa[i]);
//		}
//		trackname.setText(trackName.toString());
//		System.out.println(songFile.getName());
//		try {
//			song = AudioSystem.getAudioInputStream(songFile);
//		} catch (UnsupportedAudioFileException e) {
//			throw new RuntimeException(e);
//			//song = TAudioFileReader.getAudioInputStream(songFile);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	player.close();
//	try {
//		player.open(song);
//	} catch (LineUnavailableException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	player.start();
//	playing = true;
//	playpause.setIcon(playing? pause: play);
//}

//private void openCD() {
//	isCD = true;
//	while(!findCD());
//	System.out.println(dir);
//	InputStream toc = null;
//	try {
//		toc = cdda.getTocAsXml(dir);
//		//if(false) throw new IOException();
//	} catch (IOException e) {
//		e.printStackTrace();
//	} catch (RuntimeException e){
//		e.printStackTrace();
//	}
//	String[] sa = null;
//	try{
//		sa = parseTOC(toc);
//	}catch(IOException e){
//		throw new RuntimeException(e);
//	}
//	for(int i = 0; i < sa.length; i++){
//		tracks[i] = new Track(sa[i]);
//	}
//	numTracks = tracks.length;
//	startPlayingFromBeginningOfSong();
//}
//
//public boolean findCD(){
//	File f = new File(CD_MOUNT_LOC);
//	File[] cds = f.listFiles(new FilenameFilter(){
//		@Override
//		public boolean accept(File f, String name) {
//			boolean isCD = false;
//			try {
//				cdda.getTocAsXml(dir);
//				isCD = true;
//			} catch (Exception e) {
//				// not a cd
//			}
//			return isCD;
//		}
//		
//	});
//	if(cds == null || cds.length == 0 || (oldcds != null && cds.length == oldcds.length)) return false;
//	// else{
//	// 	File pcd = chooseCD(cds);
//	// 	if(pcd == null){
//	// 		oldcds = cds;
//	// 		return false;
//	// 	}
//	// 	cd = pcd.getAbsolutePath() + "/";
//	// 	// return true;
//	// }
//	else{
//		dir = cds[0].getAbsolutePath() + "/"; // just pick the first cd for now
//	}
//	//cd=cdda.getDefaultDevice();
//	if(dir == null) return false;
//	try{
//		Thread.sleep(100);
//	}catch(InterruptedException e){
//		throw new RuntimeException(e);
//	}
//	return dir != null;
//}

//LinkedList<File> acceptableSongFiles(File searchDirectory){
//	File[] fa = searchDirectory.listFiles();
//	LinkedList<File> acceptableFiles = new LinkedList<File>();
//	for(File f: fa){
//		if(f.isDirectory()) acceptableFiles.addAll(acceptableSongFiles(f));
//		String name = f.getName();
//		String[] sa = name.split("\\.");
//		System.out.println(Arrays.toString(sa));
//		if(sa.length < 2) continue; // needs extention
//		String ending = sa[sa.length-1];
//		System.out.println(ending);
//		boolean goodEnding = false;
//		for(String ext: new String[]{
//				"aifc",
//				"aif",
//				"aiff",
//				"au",
//				"snd",
//				"wav",
//				"mp3",
//		}){
//			//System.out.println(ext);
//			if(ending.equalsIgnoreCase(ext)) goodEnding = true;
//		}
//		boolean readableSoundFile = f.canRead() && f.isFile() && !name.startsWith(".") && goodEnding;
//		if(readableSoundFile) System.out.println("Accepting " + name);
//		else System.out.println("Rejecting " + name);
//		if(readableSoundFile) acceptableFiles.addLast(f);
//	}
//	return acceptableFiles;
//}

//private void openFolder(File f){
//	dir = f.getAbsolutePath() + "/";
//	songFiles = acceptableSongFiles(f).toArray(new File[0]);
//	numTracks = songFiles.length;
//	startPlayingFromBeginningOfSong();
//}

	
//	public File chooseCD(final File[] cds){
//		final JDialog d = new JDialog(window, "Choose a CD", true);
//		final JList<String> l = new JList<String>(new AbstractListModel<String>(){
//			private static final long serialVersionUID = -7338755877050309980L;
//			@Override public int getSize() {
//				return cds.length + 1;
//			}
//			@Override public String getElementAt(int index) {
//				if(index == cds.length) return "none of the above";
//			 	return cds[index].getName();
//			}
//		});
//		l.setDragEnabled(false);
//		d.getContentPane().add(new JScrollPane(l), BorderLayout.CENTER);
//		JButton ok = new JButton("OK");
//		JButton cancel = new JButton("Cancel");
//		ok.addActionListener(new ActionListener(){
//			@Override public void actionPerformed(ActionEvent e){
//				System.out.println("cancel.actionPerformed()");
//				l.setSelectedIndex(-1);
//				d.dispose();
//			}
//		});
//		ok.addActionListener(new ActionListener(){
//			@Override public void actionPerformed(ActionEvent e){
//				System.out.println("ok.actionPerformed()");
//				d.dispose();
//			}
//		});
//		JPanel buttons = new JPanel();
//		buttons.setLayout(new BorderLayout());
//		buttons.add(ok,BorderLayout.EAST);
//		buttons.add(cancel, BorderLayout.WEST);
//		d.getContentPane().add(buttons, BorderLayout.SOUTH);
//		d.addWindowListener(new WindowAdapter(){
//			@Override public void windowClosing(WindowEvent e){
//				System.out.println("d.windowClosing()");
//				e.getWindow().dispose();
//			}
//		});
//		Dimension lsize = l.getPreferredScrollableViewportSize();
//		lsize.setSize(lsize.getWidth()*2, lsize.getHeight());
//		d.setPreferredSize(lsize);
//		Rectangle dloc = new Rectangle(lsize);
//		dloc.setLocation(100, 100);
//		d.setBounds(dloc);
//		d.setVisible(true);
//		System.out.println("hi");
//		int index = l.getSelectedIndex();
//		System.out.println(index);
//		return (index == cds.length) || (index == -1)? null: cds[index];
//	}
	
	@SuppressWarnings("unused")
	private static SourceDataLine getSourceDataLine(String strMixerName,
			AudioFormat audioFormat,
			int nBufferSize) {
		/*
		 *	Asking for a line is a rather tricky thing.
		 *	We have to construct an Info object that specifies
		 *	the desired properties for the line.
		 *	First, we have to say which kind of line we want. The
		 *	possibilities are: SourceDataLine (for playback), Clip
		 *	(for repeated playback)	and TargetDataLine (for
		 *	 recording).
		 *	Here, we want to do normal playback, so we ask for
		 *	a SourceDataLine.
		 *	Then, we have to pass an AudioFormat object, so that
		 *	the Line knows which format the data passed to it
		 *	will have.
		 *	Furthermore, we can give Java Sound a hint about how
		 *	big the internal buffer for the line should be. This
		 *	isn't used here, signaling that we
		 *	don't care about the exact size. Java Sound will use
		 *	some default value for the buffer size.
		 */
		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
				audioFormat, nBufferSize);
		try {
			if (strMixerName != null) {
				Mixer.Info	mixerInfo = AudioCommon.getMixerInfo(strMixerName);
				if (mixerInfo == null) {
					System.out.println("AudioPlayer: mixer not found: " + strMixerName);
					System.exit(1);
				}
				Mixer	mixer = AudioSystem.getMixer(mixerInfo);
				line = (SourceDataLine) mixer.getLine(info);
			} else {
				line = (SourceDataLine) AudioSystem.getLine(info);
			}

			/*
			 *	The line is there, but it is not yet ready to
			 *	receive audio data. We have to open the line.
			 */
			line.open(audioFormat, nBufferSize);
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return line;
	}
}
