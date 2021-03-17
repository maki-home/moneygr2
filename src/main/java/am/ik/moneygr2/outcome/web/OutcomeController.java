package am.ik.moneygr2.outcome.web;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategoryRepository;
import am.ik.moneygr2.outcome.OutcomeRepository;
import am.ik.moneygr2.security.MoneygrOidcUser;
import org.thymeleaf.util.StringUtils;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OutcomeController {
	private final OutcomeRepository outcomeRepository;

	private final OutcomeCategoryRepository outcomeCategoryRepository;

	public OutcomeController(OutcomeRepository outcomeRepository,
			OutcomeCategoryRepository outcomeCategoryRepository) {
		this.outcomeRepository = outcomeRepository;
		this.outcomeCategoryRepository = outcomeCategoryRepository;
	}

	@ModelAttribute
	Outcome outcome(
			@CookieValue(name = "creditCard", defaultValue = "false") boolean isCreditCard) {
		Outcome outcome = new Outcome();
		outcome.setCreditCard(isCreditCard);
		return outcome;
	}

	@GetMapping(path = { "outcomes", "/" })
	public String showOutcomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@RequestParam(defaultValue = "false") boolean onlyExpenses) {
		LocalDate now = LocalDate.now();
		model.addAttribute("outcomeDate", now);
		return showOutcomes(model, user, now, onlyExpenses);
	}

	@GetMapping(path = "outcomes/{outcomeDate}")
	public String showOutcomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate,
			@RequestParam(defaultValue = "false") boolean onlyExpenses) {
		return showOutcomes(model, user, outcomeDate, Optional.of(outcomeDate),
				onlyExpenses);
	}

	@GetMapping(path = "outcomes", params = "fromDate")
	public String showOutcomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
			@RequestParam(defaultValue = "false") boolean onlyExpenses) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		List<Outcome> outcomes = onlyExpenses
				? this.outcomeRepository.findByOutcomeDateOnlyExpenses(fromDate, to)
				: this.outcomeRepository.findByOutcomeDate(fromDate, to);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomes", outcomes);
		model.addAttribute("total",
				outcomes.stream().mapToLong(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user.asMember());
		model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
		model.addAttribute("members", user.getMemberMap());
		return "outcomes";
	}

	@GetMapping(path = "outcomes", params = "keyword")
	public String searchOutcomes(Model model,
			@AuthenticationPrincipal MoneygrOidcUser user, @RequestParam String keyword) {
		List<Outcome> outcomes = outcomeRepository.findByOutcomeNameContaining(keyword);
		model.addAttribute("outcomes", outcomes);
		model.addAttribute("total",
				outcomes.stream().mapToLong(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user.asMember());
		model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
		model.addAttribute("members", user.getMemberMap());
		return "outcomes";
	}

	@GetMapping(path = "outcomes", params = { "parentCategoryId", "fromDate" })
	public String showOutcomesByParentCategoryId(Model model,
			@AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
			@RequestParam Integer parentCategoryId) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

		List<Outcome> outcomes = outcomeRepository
				.findByParentCategoryId(parentCategoryId, fromDate, to);

		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomes", outcomes);
		model.addAttribute("total",
				outcomes.stream().mapToLong(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user.asMember());
		Map<String, Map<Integer, String>> categories = this.outcomeCategoryRepository.outcomeCategories();
		model.addAttribute("categories", categories);
		model.addAttribute("parentCategory", categories.entrySet().stream()
				.map(Map.Entry::getKey).toArray()[parentCategoryId - 1]);
		model.addAttribute("members", user.getMemberMap());
		return "outcomes";
	}

	@PostMapping(path = "outcomes")
	String registerOutcome(@Validated Outcome outcome, BindingResult result, Model model,
			@AuthenticationPrincipal MoneygrOidcUser user, HttpServletResponse response) {
		if (result.hasErrors()) {
			return outcome.getOutcomeDate() == null ? showOutcomes(model, user, false)
					: showOutcomes(model, user, outcome.getOutcomeDate(), false);
		}
		if (StringUtils.isEmpty(outcome.getOutcomeBy())) {
			outcome.setOutcomeBy(user.getName());
		}
		this.outcomeRepository.save(outcome.update());
		Cookie cookie = new Cookie("creditCard", String.valueOf(outcome.isCreditCard()));
		response.addCookie(cookie);
		return "redirect:/outcomes/" + outcome.getOutcomeDate();
	}
}
