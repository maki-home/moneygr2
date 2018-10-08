package am.ik.moneygr2.income.web;

import am.ik.moneygr2.income.Income;
import am.ik.moneygr2.income.IncomeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "v1/incomes")
public class IncomeRestController {
	private final IncomeRepository incomeRepository;

	public IncomeRestController(IncomeRepository incomeRepository) {
		this.incomeRepository = incomeRepository;
	}

	@GetMapping(path = "{incomeId}")
	public Income getIncome(@PathVariable("incomeId") Long incomeId) {
		return this.incomeRepository.findById(incomeId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"the given income id is not found (" + incomeId + ")"));
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping(path = "{incomeId}")
	public void deleteIncome(@PathVariable("incomeId") Long incomeId) {
		this.incomeRepository.deleteById(incomeId);
	}
}
