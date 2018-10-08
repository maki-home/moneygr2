package am.ik.moneygr2.report.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import am.ik.moneygr2.income.Income;
import am.ik.moneygr2.income.IncomeRepository;
import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeRepository;
import am.ik.moneygr2.security.MoneygrOidcUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {
	private final ObjectMapper objectMapper;
	private final IncomeRepository incomeRepository;
	private final OutcomeRepository outcomeRepository;

	public ReportController(ObjectMapper objectMapper, IncomeRepository incomeRepository,
			OutcomeRepository outcomeRepository) {
		this.objectMapper = objectMapper;
		this.incomeRepository = incomeRepository;
		this.outcomeRepository = outcomeRepository;
	}

	@GetMapping(path = "/report")
	String report(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@RequestParam Optional<Boolean> stack) {
		return report(model, user,
				LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
				Optional.empty(), stack);
	}

	@GetMapping(path = "/report", params = "fromDate")
	public String report(Model model, @AuthenticationPrincipal MoneygrOidcUser user,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
			@RequestParam Optional<Boolean> stack) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		List<Outcome.SummaryByDate> summaryByDate = this.outcomeRepository
				.findSummaryByDate(fromDate, to);
		List<Outcome.SummaryByParentCategory> summaryByParentCategory = this.outcomeRepository
				.findSummaryByParentCategory(fromDate, to);
		List<Income> incomes = this.incomeRepository.findByIncomeDate(fromDate, to);

		long outcomeTotal = summaryByDate.stream()
				.mapToLong(Outcome.SummaryByDate::getSubTotal).sum();
		long incomeTotal = incomes.stream().mapToLong(Income::getAmount).sum();
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("stack", stack.orElse(false));
		try {
			if (summaryByDate.size() <= 31) {
				Collections.reverse(summaryByDate);
				Collection<IncomeSummary> incomeSummaries = summarizeIncome(incomes,
						summaryByDate, false);
				model.addAttribute("outcomeSummaryByDate", summaryByDate);
				model.addAttribute("outcomeGraphData",
						this.objectMapper.writeValueAsString(summaryByDate));
				model.addAttribute("incomeGraphData",
						this.objectMapper.writeValueAsString(incomeSummaries));
			}
			else {
				List<Outcome.SummaryByDate> summaryByMonth = summarizeByMonth(
						summaryByDate);
				Collection<IncomeSummary> incomeSummaries = summarizeIncome(incomes,
						summaryByMonth, true);
				model.addAttribute("outcomeSummaryByMonth", summaryByMonth);
				model.addAttribute("incomeSummaryByMonth", incomeSummaries);
				model.addAttribute("outcomeGraphData",
						this.objectMapper.writeValueAsString(summaryByMonth));
				model.addAttribute("incomeGraphData",
						this.objectMapper.writeValueAsString(incomeSummaries));
			}
			model.addAttribute("outcomeSummaryByParentCategory", summaryByParentCategory);
			model.addAttribute("categoryGraphData",
					this.objectMapper.writeValueAsString(summaryByParentCategory));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		model.addAttribute("outcomeTotal", outcomeTotal);
		model.addAttribute("incomeTotal", incomeTotal);
		model.addAttribute("inout", incomeTotal - outcomeTotal);
		model.addAttribute("user", user.asMember());
		return "report";
	}

	private Collection<IncomeSummary> summarizeIncome(Collection<Income> incomes,
			List<Outcome.SummaryByDate> outcomes, boolean firstDayAsKey) {
		// initialize
		Map<LocalDate, IncomeSummary> sum = outcomes.stream().map(o -> {
			IncomeSummary income = new IncomeSummary();
			income.setIncomeDate(o.getOutcomeDate());
			return income;
		}).collect(Collectors.toMap(IncomeSummary::getIncomeDate, Function.identity(),
				(u, v) -> v, LinkedHashMap::new));

		incomes.forEach(income -> {
			LocalDate incomeDate = income.getIncomeDate();
			LocalDate key = firstDayAsKey
					? LocalDate.of(incomeDate.getYear(), incomeDate.getMonth(), 1)
					: incomeDate;
			IncomeSummary summary = sum.getOrDefault(key, new IncomeSummary());
			summary.setSubTotal(summary.getSubTotal() + income.getAmount());
		});
		return sum.values();
	}

	private List<Outcome.SummaryByDate> summarizeByMonth(
			List<Outcome.SummaryByDate> outcomes) {
		return outcomes.stream().collect(Collectors.groupingBy(x -> LocalDate
				.of(x.getOutcomeDate().getYear(), x.getOutcomeDate().getMonth(), 1)))
				.entrySet().stream().map(x -> new Outcome.SummaryByDate() {
					@Override
					public LocalDate getOutcomeDate() {
						return x.getKey();
					}

					@Override
					public Long getSubTotal() {
						return x.getValue().stream()
								.map(Outcome.SummaryByDate::getSubTotal)
								.mapToLong(Long::longValue).sum();
					}
				}).sorted(Comparator.comparing(Outcome.SummaryByDate::getOutcomeDate)
						.reversed())
				.collect(Collectors.toList());
	}

	static class IncomeSummary {
		private LocalDate incomeDate;
		private Long subTotal = 0L;

		public LocalDate getIncomeDate() {
			return incomeDate;
		}

		public Long getSubTotal() {
			return subTotal;
		}

		public void setIncomeDate(LocalDate incomeDate) {
			this.incomeDate = incomeDate;
		}

		public void setSubTotal(Long subTotal) {
			this.subTotal = subTotal;
		}
	}
}
