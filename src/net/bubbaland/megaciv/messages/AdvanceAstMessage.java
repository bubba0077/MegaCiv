package net.bubbaland.megaciv.messages;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.megaciv.game.Civilization.AstChange;
import net.bubbaland.megaciv.game.Civilization.Name;
import net.bubbaland.megaciv.game.GameEvent;
import net.bubbaland.megaciv.game.GameEvent.EventType;

public class AdvanceAstMessage implements ClientMessage {

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

	@Override
	public String toString() {
		return "AST Advancement: " + this.advanceAst;
	}

	@Override
	public EventType getEventType() {
		return GameEvent.EventType.AST;
	}


}
