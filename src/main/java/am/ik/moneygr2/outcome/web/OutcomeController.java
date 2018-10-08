package am.ik.moneygr2.outcome.web;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategory;
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
import org.springframework.web.bind.annotation.*;

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
	public String showOutcomes(Model model,
			@AuthenticationPrincipal MoneygrOidcUser user) {
		LocalDate now = LocalDate.now();
		model.addAttribute("outcomeDate", now);
		return showOutcomes(model, user, now);
	}

	@GetMapping(path = "outcomes/{outcomeDate}")
	public String showOutcomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
		return showOutcomes(model, user, outcomeDate, Optional.of(outcomeDate));
	}

	@GetMapping(path = "outcomes", params = "fromDate")
	public String showOutcomes(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		List<Outcome> outcomes = this.outcomeRepository.findByOutcomeDate(fromDate, to);
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomes", outcomes);
		model.addAttribute("total",
				outcomes.stream().mapToLong(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user.asMember());
		model.addAttribute("categories", this.outcomeCategories());
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
		model.addAttribute("categories", this.outcomeCategories());
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
		Map<String, Map<Integer, String>> categories = this.outcomeCategories();
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
			return outcome.getOutcomeDate() == null ? showOutcomes(model, user)
					: showOutcomes(model, user, outcome.getOutcomeDate());
		}
		if (StringUtils.isEmpty(outcome.getOutcomeBy())) {
			outcome.setOutcomeBy(user.getName());
		}
		this.outcomeRepository.save(outcome.update());
		Cookie cookie = new Cookie("creditCard", String.valueOf(outcome.isCreditCard()));
		response.addCookie(cookie);
		return "redirect:/outcomes/" + outcome.getOutcomeDate();
	}

	private Map<String, Map<Integer, String>> outcomeCategories() {
		List<OutcomeCategory> categories = this.outcomeCategoryRepository.findAll();
		Map<String, Map<Integer, String>> cat = new LinkedHashMap<>();
		for (OutcomeCategory category : categories) {
			String key = category.getParentOutcomeCategory().getParentCategoryName();
			cat.computeIfAbsent(key, x -> new LinkedHashMap<>());
			cat.get(key).put(category.getCategoryId(), category.getCategoryName());
		}
		return cat;
	}
}
