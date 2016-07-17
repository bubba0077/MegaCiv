package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization;

public class AdvanceAstMessage extends ClientMessage {

	@JsonProperty("advanceAst")
	private final HashMap<Civilization.Name, Boolean> advanceAst;

	/**
	 * @return the advanceAst
	 */
	public HashMap<Civilization.Name, Boolean> getAdvanceAst() {
		return this.advanceAst;
	}

	@JsonCreator
	public AdvanceAstMessage(@JsonProperty("advanceAst") final HashMap<Civilization.Name, Boolean> advanceAst) {
		this.advanceAst = advanceAst;
	}
}
