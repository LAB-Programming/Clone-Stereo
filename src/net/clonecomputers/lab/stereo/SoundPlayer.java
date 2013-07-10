package net.clonecomputers.lab.stereo;

public interface SoundPlayer {
	public void init();
	
	public void play();
	public void pause();
	public void next();
	public void previous();
	
	public String getTrackName();
	/**
	 * 
	 * @return track length in seconds
	 */
	public int getTrackLength();
	/**
	 * 
	 * @return track position in seconds
	 */
	public int getTrackPos();
	/**
	 * 
	 * @param newPos track position in seconds
	 */
	public void setTrackPos(int newPos);
	
	public void addSoundPlayerListener(SoundPlayerListener l);
	public void removeSoundPlayerListener(SoundPlayerListener l);
	/**
	 * must not return null
	 * @return an array of all added TrackPositionUpdateListener's (length=0 if there are none)
	 */
	public SoundPlayerListener[] getSoundPlayerListeners();
}
