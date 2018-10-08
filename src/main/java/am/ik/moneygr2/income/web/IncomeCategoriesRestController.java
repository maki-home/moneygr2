package am.ik.moneygr2.income.web;

import am.ik.moneygr2.income.IncomeCategory;
import am.ik.moneygr2.income.IncomeCategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "v1/incomeCategories")
public class IncomeCategoriesRestController {
	private final IncomeCategoryRepository incomeCategoryRepository;

	public IncomeCategoriesRestController(
			IncomeCategoryRepository incomeCategoryRepository) {
		this.incomeCategoryRepository = incomeCategoryRepository;
	}

	@GetMapping(path = "{categoryId}")
	public IncomeCategory getIncomeCategory(
			@PathVariable("categoryId") Integer categoryId) {
		return this.incomeCategoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"the given category id is not found (" + categoryId + ")"));
	}
}
