package am.ik.moneygr2.outcome.web;

import am.ik.moneygr2.outcome.OutcomeCategory;
import am.ik.moneygr2.outcome.OutcomeCategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "v1/outcomeCategories")
public class OutcomeCategoriesRestController {
	private final OutcomeCategoryRepository outcomeCategoryRepository;

	public OutcomeCategoriesRestController(
			OutcomeCategoryRepository outcomeCategoryRepository) {
		this.outcomeCategoryRepository = outcomeCategoryRepository;
	}

	@GetMapping(path = "{categoryId}")
	public OutcomeCategory getOutcomeCategory(
			@PathVariable("categoryId") Integer categoryId) {
		return this.outcomeCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"the given category id is not found (" + categoryId + ")"));
	}
}
