package net.bubbaland.sntp;

import java.time.Instant;

public interface SntpListener {

	public abstract void onSntpError(Instant when);

	public abstract void onSntpSync(Instant when);

}
