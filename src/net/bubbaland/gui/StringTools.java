package net.bubbaland.gui;

public class StringTools {

	public static String wrapHtml(String htmlString, int maxWidth) {
		// System.out.println("Input: " + htmlString);
		String s = "";
		for (String t : htmlString.split("<[Bb][Rr]/>")) {
			String currentWord = "";
			int count = 0;
			boolean inTag = false;
			for (char c : t.toCharArray()) {
				currentWord = currentWord + c;
				switch (c) {
					case '<':
						inTag = true;
						break;
					case '>':
						inTag = false;
						break;
					case ' ':
						s = s + currentWord;
						currentWord = "";
						break;
					default:
						break;
				}
				if (!inTag) {
					count++;
				}

				// System.out.println("Current character: " + c);
				// System.out.println("In tag: " + inTag);
				// System.out.println("Current word: " + currentWord);
				// System.out.println("Count: " + count);
				// System.out.println("New string: " + s);
				// System.out.println("");

				if (count >= maxWidth) {
					s = s + "<BR/>";
					count = 0;
				}
			}

			s = s + currentWord;
			s = s + "<BR/>";
		}
		// System.out.println("Output: " + s + "\n");
		return s;
	}

	public static void main(String[] args) {
		String test1 =
				"The beneficiary selects 2 of your coastal cities and you select 1 coastal city from each of 2 other "
						+ "players. All selected cities are replaced by pirate cities. The beneficiary may not be selected "
						+ "as a secondary victim.<BR/>"
						+ "<span color=red><strong>Cartography</strong>: If you are the primary victim, the beneficiary selects "
						+ "and replaces 1 additional coastal city.</span><BR/>"
						+ "<span color=red><strong>Naval Warfare</strong>: If you are the primary victim, the beneficiary selects "
						+ "and replaces 1 less coastal city. You may not be selected as a secondary victim.</span><BR/>";
		// String test1 = "acb defg";

		wrapHtml(test1, 50);
	}

}
