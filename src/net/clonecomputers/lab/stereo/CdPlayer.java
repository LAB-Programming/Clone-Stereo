package net.clonecomputers.lab.stereo;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.tritonus.lowlevel.cdda.*;

public class CdPlayer implements SoundPlayer {
	
	public static final String CD_MOUNT_LOC = "/dev/"; // "/media/" ?  // on MacOS "/Volumes/"
	
	private static final CddaMidLevel cdda;
	static{
		CddaMidLevel tmpcdda = null;
		try{
			tmpcdda = CddaUtils.getCddaMidLevel();
		}catch(UnsatisfiedLinkError e){
			System.err.println("please put libtritonuscdparanoia.so in " + 
					System.getProperty("java.ext.dirs").replaceAll(":", "\nOR\n"));
			e.printStackTrace();
			System.exit(1);
		}
		cdda = tmpcdda;
	}
	
	private Track[] tracks;

	private JFrame window;
	
	private String[] parseTOC(InputStream toc) throws IOException{
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
	
	private void openCD() {
		String dir;
		while((dir=findCD()) == null);
		System.out.println(dir);
		InputStream toc = null;
		try {
			toc = cdda.getTocAsXml(dir);
			//if(false) throw new IOException();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e){
			e.printStackTrace();
		}
		String[] sa = null;
		try{
			sa = parseTOC(toc);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		for(int i = 0; i < sa.length; i++){
			tracks[i] = new Track(sa[i]);
		}
	}
	
	public String findCD(){
		String dir = null;
		File f = new File(CD_MOUNT_LOC);
		File[] cds = f.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File f, String name) {
				boolean isCD = false;
				try {
					cdda.getTocAsXml(new File(f,name).getAbsolutePath());
					isCD = true;
				} catch (Exception e) {
					// not a cd
				}
				return isCD;
			}
			
		});
		if(cds == null || cds.length == 0
				/* || (oldcds != null && cds.length == oldcds.length)*/) return null;
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
			dir = cds[0].getAbsolutePath() + "/"; // just pick the first cd for now
		}
		try{
			Thread.sleep(100);
		}catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		return dir;
	}
	
	public File chooseCD(final File[] cds){
		final JDialog d = new JDialog(window, "Choose a CD", true);
		final JList<String> l = new JList<String>(new AbstractListModel<String>(){
			private static final long serialVersionUID = -7338755877050309980L;
			@Override public int getSize() {
				return cds.length + 1;
			}
			@Override public String getElementAt(int index) {
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
	/**
	 * 
	 * @param window the place that all dialogs should come from
	 */
	public CdPlayer(JFrame window) {
		this.window = window;
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub

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
		return "Not Implemented (cd)";
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
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSoundPlayerListener(SoundPlayerListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public SoundPlayerListener[] getSoundPlayerListeners() {
		// TODO Auto-generated method stub
		return new SoundPlayerListener[0];
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
