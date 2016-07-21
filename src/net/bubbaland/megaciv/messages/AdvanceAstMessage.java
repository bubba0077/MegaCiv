package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.AstChange;
import net.bubbaland.megaciv.game.Civilization.Name;

public class AdvanceAstMessage extends ClientMessage {

	@JsonProperty("advanceAst")
	private final HashMap<Name, AstChange> advanceAst;

	/**
	 * @return the advanceAst
	 */
	public HashMap<Name, AstChange> getAdvanceAst() {
		return this.advanceAst;
	}

	@JsonCreator
	public AdvanceAstMessage(@JsonProperty("advanceAst") final HashMap<Name, AstChange> advanceAst) {
		this.advanceAst = advanceAst;
	}
}
