package net.clonecomputers.lab.stereo;

import java.util.*;

public class SoundPlayerEvent extends EventObject {
	public enum Type{
		TRACK_ENDED,PROGRESS_UPDATED,
	}
	
	protected Type type;
	protected int trackLength = -1;
	protected int trackPos = -1;
	
	public SoundPlayerEvent(Object source, int trackLength, int trackPos) {
		super(source);
		this.trackLength = trackLength;
		this.trackPos = trackPos;
		type = Type.PROGRESS_UPDATED;
	}
	
	public SoundPlayerEvent(Object source) {
		super(source);
		type = Type.TRACK_ENDED;
	}
	
	public int getTrackLength(){
		return trackLength;
	}
	
	public int getTrackPos(){
		return trackPos;
	}
	
	public float getTrackFractionComplete(){
		return trackPos/(float)trackLength;
	}
	
	public Type getType(){
		return type;
	}

}
