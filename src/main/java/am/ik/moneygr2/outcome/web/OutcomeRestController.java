package am.ik.moneygr2.outcome.web;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "v1/outcomes")
public class OutcomeRestController {
	private final OutcomeRepository outcomeRepository;

	public OutcomeRestController(OutcomeRepository outcomeRepository) {
		this.outcomeRepository = outcomeRepository;
	}

	@GetMapping(path = "{outcomeId}")
	public Outcome getOutcome(@PathVariable("outcomeId") Long outcomeId) {
		return this.outcomeRepository.findById(outcomeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"the given outcome id is not found (" + outcomeId + ")"));
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping(path = "{outcomeId}")
	public void deleteOutcome(@PathVariable("outcomeId") Long outcomeId) {
		this.outcomeRepository.deleteById(outcomeId);
	}
}
