package am.ik.moneygr2.outcome.event;

import java.io.Serializable;

import am.ik.moneygr2.outcome.Outcome;

public class OutcomeUpdateEvent implements Serializable {
	private final Outcome outcome;

	public OutcomeUpdateEvent(Outcome outcome) {
		this.outcome = outcome;
	}

	public Outcome outcome() {
		return this.outcome;
	}
}
