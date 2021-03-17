package am.ik.moneygr2.outcome.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategoryRepository;
import am.ik.moneygr2.outcome.OutcomeRepository;
import am.ik.moneygr2.security.MoneygrOidcUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(path = "outcomes/goldpoint")
public class GoldPointCardImportController {
	private final OutcomeRepository outcomeRepository;

	private final OutcomeCategoryRepository outcomeCategoryRepository;

	public GoldPointCardImportController(OutcomeRepository outcomeRepository, OutcomeCategoryRepository outcomeCategoryRepository) {
		this.outcomeRepository = outcomeRepository;
		this.outcomeCategoryRepository = outcomeCategoryRepository;
	}

	@GetMapping
	public String form(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute GoldPointCardImportForm form, Model model) {
		model.addAttribute("user", user.asMember());
		return "goldpoint";
	}

	@PostMapping(params = "preview")
	public String preview(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute GoldPointCardImportForm form, Model model) throws Exception {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(form.getFile().getResource().getInputStream(), "Windows-31J"))) {
			final List<GoldPointCartImportItem> items = reader.lines()
					.map(line -> {
						final List<String> columns = Arrays.asList(line.split(","));
						if (!columns.get(0).startsWith("20")) {
							return null;
						}
						final LocalDate outcomeDate = LocalDate.parse(columns.get(0).replace("/", "-"));
						final String outcomeName = columns.get(1).replace("ＰＡＹＰＡＹ＊", "").replace("セブンーイレブン", "セブンイレブン");
						final long amount = Long.parseLong(columns.get(2));
						return new GoldPointCartImportItem(outcomeDate, outcomeName, amount);
					})
					.filter(Objects::nonNull)
					.filter(x -> x.getAmount() > 0)
					.sorted(Comparator.comparing(GoldPointCartImportItem::getOutcomeDate))
					.collect(Collectors.toList());
			final LocalDate startDate = items.get(0).getOutcomeDate();
			final LocalDate endDate = items.get(items.size() - 1).getOutcomeDate();
			final List<Outcome> outcomes = outcomeRepository.findByOutcomeDate(startDate, endDate);
			for (GoldPointCartImportItem item : items) {
				final List<Outcome> similar = outcomes.stream()
						.filter(outcome -> Objects.equals(item.getOutcomeDate(), outcome.getOutcomeDate()) &&
								Objects.equals(item.getAmount(), outcome.getAmount()))
						.collect(Collectors.toList());
				item.setSimilar(similar);
				item.setIncluded(similar.isEmpty());
				if (!similar.isEmpty()) {
					item.setOutcomeCategoryId(similar.get(0).getOutcomeCategory().getCategoryId());
				}
			}
			final GoldPointCartImportItemForm goldPointCartImportItemForm = new GoldPointCartImportItemForm();
			goldPointCartImportItemForm.setItems(items);
			model.addAttribute("goldPointCartImportItemForm", goldPointCartImportItemForm);
			model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
		}
		model.addAttribute("user", user.asMember());
		return "goldpoint";
	}

	@PostMapping(params = "import")
	public String bulkImport(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute GoldPointCartImportItemForm form, Model model, RedirectAttributes attributes) throws Exception {
		GoldPointCartImportItemForm goldPointCartImportItemForm = new GoldPointCartImportItemForm();
		final List<GoldPointCartImportItem> items = form.getItems().stream().filter(GoldPointCartImportItem::isIncluded).collect(Collectors.toList());
		goldPointCartImportItemForm.setItems(items);
		if (items.stream().anyMatch(x -> x.getOutcomeCategoryId() == null)) {
			model.addAttribute("user", user.asMember());
			model.addAttribute("goldPointCartImportItemForm", goldPointCartImportItemForm);
			model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
			return "goldpoint";
		}
		final List<Outcome> outcomes = items.stream().map(x -> x.toOutcome(user.getName()).update()).collect(Collectors.toList());
		this.outcomeRepository.saveAll(outcomes);
		attributes.addAttribute("fromDate", items.get(0).getOutcomeDate().toString());
		attributes.addAttribute("toDate", items.get(items.size() - 1).getOutcomeDate().toString());
		return "redirect:/outcomes";
	}
}
