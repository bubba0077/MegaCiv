package net.bubbaland.gui;

import javazoom.jl.player.Player;

public class BubbaAudio {
	private final String	filename;
	private final Class<?>	sourceClass;

	public BubbaAudio(Class<?> sourceClass, String filename) {
		this.sourceClass = sourceClass;
		this.filename = filename;
	}

	public void play() {
		try {
			final Player player = new Player(sourceClass.getResourceAsStream(this.filename));
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