package net.clonecomputers.lab.stereo;

public class Track{
	public final int id;
	public final int start;
	public final int length;
	public final int type;
	public final boolean audio;
	public final boolean copy;
	public final boolean pre;
	public final int channels;

	public Track(int id, int start, int length, int type, boolean audio, boolean copy, boolean pre, int channels){
		this.id = id;
		this.start = start;
		this.length = length;
		this.type = type;
		this.audio = audio;
		this.copy = copy;
		this.pre = pre;
		this.channels = channels;
	}

	public Track(String s){
		id = Integer.parseInt(getVal("id",s));
		start = Integer.parseInt(getVal("start",s));
		length = Integer.parseInt(getVal("length",s));
		type = Integer.parseInt(getVal("type",s));
		audio = Boolean.parseBoolean(getVal("audio",s));
		copy = Boolean.parseBoolean(getVal("copy",s));
		pre = Boolean.parseBoolean(getVal("pre",s));
		channels = Integer.parseInt(getVal("channels",s));
	}

	private String getVal(String key, String s){
		int start = s.indexOf('\"', s.indexOf(key));
		int end = s.indexOf('\"', start+1);
		return s.substring(start, end);
	}
}