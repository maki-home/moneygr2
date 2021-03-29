package am.ik.moneygr2.outcome.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategoryRepository;
import am.ik.moneygr2.outcome.OutcomeRepository;
import am.ik.moneygr2.security.MoneygrOidcUser;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
public class ImportItemHelper {
	private final OutcomeRepository outcomeRepository;

	private final OutcomeCategoryRepository outcomeCategoryRepository;

	public ImportItemHelper(OutcomeRepository outcomeRepository, OutcomeCategoryRepository outcomeCategoryRepository) {
		this.outcomeRepository = outcomeRepository;
		this.outcomeCategoryRepository = outcomeCategoryRepository;
	}

	public String preview(MoneygrOidcUser user, ImportForm form, Model model, Function<String, ImportItem> importItemMapper, String formViewName) throws Exception {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(form.getFile().getResource().getInputStream(), "Windows-31J"))) {
			final List<ImportItem> items = reader.lines()
					.map(importItemMapper)
					.filter(Objects::nonNull)
					.filter(x -> x.getAmount() > 0)
					.sorted(Comparator.comparing(ImportItem::getOutcomeDate))
					.collect(Collectors.toList());
			final LocalDate startDate = items.get(0).getOutcomeDate();
			final LocalDate endDate = items.get(items.size() - 1).getOutcomeDate();
			final List<Outcome> outcomes = outcomeRepository.findByOutcomeDate(startDate, endDate);
			for (ImportItem item : items) {
				final List<Outcome> similar = outcomes.stream()
						.filter(outcome -> Objects.equals(item.getOutcomeDate(), outcome.getOutcomeDate()) &&
								Objects.equals(item.getAmount(), outcome.getAmount() * outcome.getQuantity()))
						.collect(Collectors.toList());
				item.setSimilar(similar);
				item.setIncluded(similar.isEmpty());
				if (!similar.isEmpty()) {
					item.setOutcomeCategoryId(similar.get(0).getOutcomeCategory().getCategoryId());
				}
			}
			final ImportItemForm importItemForm = new ImportItemForm();
			importItemForm.setItems(items);
			model.addAttribute("importItemForm", importItemForm);
			model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
		}
		model.addAttribute("user", user.asMember());
		return formViewName;
	}

	public String bulkImport(MoneygrOidcUser user, ImportItemForm form, Model model, RedirectAttributes attributes, String formViewName) throws Exception {
		ImportItemForm importItemForm = new ImportItemForm();
		final List<ImportItem> items = form.getItems().stream().filter(ImportItem::isIncluded).collect(Collectors.toList());
		importItemForm.setItems(items);
		if (items.stream().anyMatch(x -> x.getOutcomeCategoryId() == null)) {
			model.addAttribute("user", user.asMember());
			model.addAttribute("importItemForm", importItemForm);
			model.addAttribute("categories", this.outcomeCategoryRepository.outcomeCategories());
			return formViewName;
		}
		final List<Outcome> outcomes = items.stream().map(x -> x.toOutcome(user.getName()).update()).collect(Collectors.toList());
		this.outcomeRepository.saveAll(outcomes);
		attributes.addAttribute("fromDate", items.get(0).getOutcomeDate().toString());
		attributes.addAttribute("toDate", items.get(items.size() - 1).getOutcomeDate().toString());
		return "redirect:/outcomes";
	}
}
