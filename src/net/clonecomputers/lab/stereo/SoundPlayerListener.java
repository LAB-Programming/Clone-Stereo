package net.clonecomputers.lab.stereo;

import java.util.*;

public interface SoundPlayerListener extends EventListener {
	public void trackPositionUpdated(SoundPlayerEvent e);
	public void trackEnded(SoundPlayerEvent e);
}
