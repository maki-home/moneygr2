package am.ik.moneygr2.income.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import am.ik.moneygr2.income.Income;
import am.ik.moneygr2.income.IncomeCategory;
import am.ik.moneygr2.income.IncomeCategoryRepository;
import am.ik.moneygr2.income.IncomeRepository;
import am.ik.moneygr2.security.MoneygrOidcUser;
import org.thymeleaf.util.StringUtils;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IncomeController {
	private final IncomeRepository incomeRepository;
	private final IncomeCategoryRepository incomeCategoryRepository;

	public IncomeController(IncomeRepository incomeRepository,
			IncomeCategoryRepository incomeCategoryRepository) {
		this.incomeRepository = incomeRepository;
		this.incomeCategoryRepository = incomeCategoryRepository;
	}

	@ModelAttribute
	Income income() {
		Income income = new Income();
		income.setIncomeDate(LocalDate.now());
		return income;
	}

	@GetMapping(path = "incomes")
	public String showIncomes(Model model,
			@AuthenticationPrincipal MoneygrOidcUser user) {
		return showIncomes(model, user,
				LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
				Optional.empty());
	}

	@GetMapping(path = "incomes", params = "fromDate")
	public String showIncomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

		List<Income> incomes = this.incomeRepository.findByIncomeDate(fromDate, to);

		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("incomes", incomes);
		model.addAttribute("total", incomes.stream().mapToLong(Income::getAmount).sum());
		model.addAttribute("user", user.asMember());
		model.addAttribute("categories", this.incomeCategories());
		model.addAttribute("members", user.getMemberMap());
		return "incomes";
	}

	@PostMapping(path = "incomes")
	public String registerIncome(@Validated Income income, BindingResult result,
			Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return income.getIncomeDate() == null ? showIncomes(model, user)
					: showIncomes(model, user, income.getIncomeDate(), Optional.empty());
		}
		if (StringUtils.isEmpty(income.getIncomeBy())) {
			income.setIncomeBy(user.getName());
		}
		this.incomeRepository.save(income);
		LocalDate date = income.getIncomeDate().with(TemporalAdjusters.firstDayOfMonth());
		attributes.addAttribute("fromDate",
				date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return "redirect:/incomes";
	}

	private Map<Integer, String> incomeCategories() {
		List<IncomeCategory> categories = this.incomeCategoryRepository.findAll();
		Map<Integer, String> cat = new LinkedHashMap<>();
		for (IncomeCategory category : categories) {
			cat.put(category.getCategoryId(), category.getCategoryName());
		}
		return cat;
	}
}
