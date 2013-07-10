package net.clonecomputers.lab.stereo;

public class DummyPlayer implements SoundPlayer {

	@Override
	public void init() {
		
	}

	@Override
	public void play() {
		System.out.println("dummy player can't play");
	}

	@Override
	public void pause() {
		System.out.println("dummy player can't pause");
	}

	@Override
	public void next() {
		System.out.println("dummy player can't skip tracks");
	}

	@Override
	public void previous() {
		System.out.println("dummy player can't rewind");
	}

	@Override
	public String getTrackName() {
		System.out.println("dummy player can't do that");
		return "Dummy Player";
	}

	@Override
	public int getTrackLength() {
		System.out.println("dummy player can't do that");
		return 0;
	}

	@Override
	public int getTrackPos() {
		System.out.println("dummy player can't do that");
		return 0;
	}

	@Override
	public void setTrackPos(int newPos) {
		System.out.println("dummy player can't scrub");
	}

	@Override
	public void addSoundPlayerListener(SoundPlayerListener l) {
		System.out.println("dummy player doesn't know what to do with this");
	}

	@Override
	public void removeSoundPlayerListener(SoundPlayerListener l) {
		System.out.println("dummy player couldn't have put one there in in the first place");
	}

	@Override
	public SoundPlayerListener[] getSoundPlayerListeners() {
		System.out.println("dummy player has none");
		return new SoundPlayerListener[0];
	}

}
