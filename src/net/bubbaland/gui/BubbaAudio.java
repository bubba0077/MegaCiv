package net.bubbaland.gui;

import java.net.URL;
import javazoom.jl.player.Player;

/**
 * Simple class to play an audio file repeatedly.
 * 
 * @author Walter Kolczynski
 * 
 */

public class BubbaAudio {

	private final URL url;

	/**
	 * Create a new instance for a given resource
	 * 
	 * @param url
	 *            Audio file to be played
	 */
	public BubbaAudio(URL url) {
		this.url = url;
	}

	/**
	 * Play the audio file
	 */
	public void play() {
		try {
			// Open a new stream from the audio file
			final Player player = new Player(this.url.openStream());
			new Thread() {
				@Override
				public void run() {
					try {
						player.play();
					} catch (final Exception e) {

					} finally {
						player.close();
					}
				}
			}.start();
		} catch (final Exception e) {
			System.out.println("Couldn't open audio file");
			e.printStackTrace();
		}
	}
}