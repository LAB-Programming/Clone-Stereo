package clone.lab;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.sound.sampled.*;
import java.util.*;

import org.tritonus.lowlevel.cdda.*;

/* goals:
 * fix issue that it can't open/play any audio file
 * use gracenote or similar database to recognize cd and track names
 */

public class Stereo extends JPanel{

	private static final long serialVersionUID = -744463332572359442L;

	public static final String CD_MOUNT_LOC = "/dev/"; // "/media/" ?  // on MacOS "/Volumes/"
	
	public final ImageIcon play = new ImageIcon(getClass().getResource("resources/play.png"));
	public final ImageIcon pause = new ImageIcon(getClass().getResource("resources/pause.png"));
	public final ImageIcon back = new ImageIcon(getClass().getResource("resources/back.png"));
	public final ImageIcon forward = new ImageIcon(getClass().getResource("resources/forward.png"));
	private static final CddaMidLevel cdda = CddaUtils.getCddaMidLevel();
	/*static{
		try{
			cdda = CddaUtils.getCddaMidLevel();
		}catch(UnsatisfiedLinkError e){
			System.err.println("please put libtritonuscdparanoia.so in " + System.getProperty(""));
			e.printStackTrace();
			System.exit(1);
		}
	}*/
	private String cd;
	private InputStream toc;
	private Track[] tracks;
	private int songindex=0;
	private AudioInputStream song;
	
	private final Clip player;

	private final JFrame window;
	
	private final JButton playpause = new JButton(play);
	private final JButton rewind = new JButton(back);
	private final JButton fastforward = new JButton(forward);
	private final JSlider progressbar = new JSlider(0,100,0);
	private final JLabel trackname = new JLabel("song here",JLabel.CENTER);

	private File[] oldcds;
	
	public boolean playing;
	
	public static void main(String[] args) {
		JFrame window = new JFrame("Clone Stereo");
		Stereo s = new Stereo(window);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(s);
		window.pack();
		window.setVisible(true);
		s.init();
		window.pack();
	}
	
	public void updatePlaceInSong(int percentFinished){
		player.setMicrosecondPosition((player.getMicrosecondLength()*(percentFinished/100)));
	}
	
	public void togglePlay(){
		playing=!playing;
		if(!playing) player.close();
		else{
			try {
				player.open(song);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			updatePlaceInSong(progressbar.getValue());
		}
	}
	
	private String[] parseTOC() throws IOException{
		LinkedList<String> ll = new LinkedList<String>();
		while(true){
			StringBuilder sb = new StringBuilder();
			while(true){
				char c = (char)toc.read();
				if(c == -1){
					ll.removeFirst();
					ll.removeLast();
					return ll.toArray(new String[0]);
				}
				if(c != '\n') sb.append(c);
				else{
					ll.addLast(sb.toString());
					break;
				}
			}
		}
	}

	private void init() {
		while(!findCD());
		System.out.println(cd);
		String[] sa = null;
		try{
			sa = parseTOC();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		for(int i = 0; i < sa.length; i++){
			tracks[i] = new Track(sa[i]);
		}
		/*songs=cd.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File f, String name) {
				return name.contains(".aiff");
			}
			
		});*/
		this.removeAll();
		progressbar.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				updatePlaceInSong(progressbar.getValue());
			}
			
		});
		playpause.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				togglePlay();
				playpause.setIcon(playing? pause: play);
			}
			
		});
		fastforward.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				songindex=(songindex+1+tracks.length)%tracks.length;
				//System.out.println(songs[songindex].getName());
				try {
					song=cdda.getTrack(cd, songindex);
				} catch (IOException e) {
					e.printStackTrace();
				}
				player.close();
				try {
					player.open(song);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		rewind.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				songindex=(songindex-1+tracks.length)%tracks.length;
				//System.out.println(songs[songindex].getName());
				try {
					//song=AudioSystem.getAudioInputStream(songs[songindex]);
					song=cdda.getTrack(cd, songindex);
				} catch (IOException e) {
					e.printStackTrace();
				}
				player.close();
				try {
					player.open(song);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		setLayout(new BorderLayout());
		playpause.setBackground(Color.WHITE);
		rewind.setBackground(Color.WHITE);
		fastforward.setBackground(Color.WHITE);
		playpause.setPreferredSize(new Dimension(64,64));
		rewind.setPreferredSize(new Dimension(64,64));
		fastforward.setPreferredSize(new Dimension(64,64));
		add(playpause,BorderLayout.CENTER);
		add(rewind,BorderLayout.WEST);
		add(fastforward,BorderLayout.EAST);
		add(progressbar,BorderLayout.SOUTH);
		add(trackname,BorderLayout.NORTH);
		
		try {
			song=CddaUtils.getCddaMidLevel().getTrack(cd, songindex);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		player.close();
		try {
			player.open(song);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Stereo(JFrame window){
		this.window = window;
		Clip c=null;
		try {
			c=AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		player=c;
		this.add(new JLabel("Waiting for CD"));
	}
	
	public boolean findCD(){
		File f = new File(CD_MOUNT_LOC);
		File[] cds = f.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File f, String name) {
				//return name.contains("CD");
				return true;
			}
			
		});
		if(cds == null || cds.length == 0 || (oldcds != null && cds.length == oldcds.length)) return false;
		// else{
		// 	File pcd = chooseCD(cds);
		// 	if(pcd == null){
		// 		oldcds = cds;
		// 		return false;
		// 	}
		// 	cd = pcd.getAbsolutePath() + "/";
		// 	// return true;
		// }
		else{
			for(File pcd: cds){
				System.out.println("testing " + pcd.getName());
				try {
					cdda.getTocAsXml(cd);
				} catch (Exception e) {
					continue;
				}
		 		cd = pcd.getAbsolutePath() + "/";
		 		System.out.println(pcd.getName() + " works!");
			}
			if(cd == null){
				oldcds = cds;
				return false;
			}
		}
		//cd=cdda.getDefaultDevice();
		if(cd == null) return false;
		try {
			toc = cdda.getTocAsXml(cd);
			//if(false) throw new IOException();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e){
			e.printStackTrace();
		}
		try{
			Thread.sleep(100);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		return cd != null;
	}
	
	public File chooseCD(final File[] cds){
		final JDialog d = new JDialog(window, "Choose a CD", true);
		final JList l = new JList(new AbstractListModel(){
			private static final long serialVersionUID = -7338755877050309980L;
			@Override public int getSize() {
				return cds.length + 1;
			}
			@Override public Object getElementAt(int index) {
				if(index == cds.length) return "none of the above";
			 	return cds[index].getName();
			}
		});
		l.setDragEnabled(false);
		d.getContentPane().add(new JScrollPane(l), BorderLayout.CENTER);
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		ok.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent e){
				System.out.println("cancel.actionPerformed()");
				l.setSelectedIndex(-1);
				d.dispose();
			}
		});
		ok.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent e){
				System.out.println("ok.actionPerformed()");
				d.dispose();
			}
		});
		JPanel buttons = new JPanel();
		buttons.setLayout(new BorderLayout());
		buttons.add(ok,BorderLayout.EAST);
		buttons.add(cancel, BorderLayout.WEST);
		d.getContentPane().add(buttons, BorderLayout.SOUTH);
		d.addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){
				System.out.println("d.windowClosing()");
				e.getWindow().dispose();
			}
		});
		Dimension lsize = l.getPreferredScrollableViewportSize();
		lsize.setSize(lsize.getWidth()*2, lsize.getHeight());
		d.setPreferredSize(lsize);
		Rectangle dloc = new Rectangle(lsize);
		dloc.setLocation(100, 100);
		d.setBounds(dloc);
		d.setVisible(true);
		System.out.println("hi");
		int index = l.getSelectedIndex();
		System.out.println(index);
		return (index == cds.length) || (index == -1)? null: cds[index];
	}
}
