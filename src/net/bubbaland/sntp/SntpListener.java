package net.bubbaland.sntp;

public interface SntpListener {

	public abstract void onSntpError();

	public abstract void onSntpSync();

}
