package am.ik.moneygr2.outcome.event;

import java.time.LocalDate;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategoryTrainer;
import am.ik.moneygr2.outcome.OutcomeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutcomeEventHandler {
	private final Logger log = LoggerFactory.getLogger(OutcomeEventHandler.class);
	private final OutcomeRepository outcomeRepository;
	private final OutcomeCategoryTrainer trainer;

	public OutcomeEventHandler(OutcomeRepository outcomeRepository,
			OutcomeCategoryTrainer trainer) {
		this.outcomeRepository = outcomeRepository;
		this.trainer = trainer;
	}

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void beforeCommit(OutcomeUpdateEvent event) {
		Outcome outcome = event.outcome();
		log.info("before Commit {}", outcome);
		if (outcome.getOutcomeDate() == null) {
			outcome.setOutcomeDate(LocalDate.now());
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void afterCommit(OutcomeUpdateEvent event) {
		Outcome outcome = event.outcome();
		log.info("after Commit {}", outcome);
		this.trainer.train(outcome.getOutcomeName(),
				outcome.getOutcomeCategory().getCategoryId());
	}
}