package am.ik.moneygr2.income;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class IncomeEventHandler {
	private final IncomeRepository incomeRepository;
	private final Logger log = LoggerFactory.getLogger(IncomeEventHandler.class);

	public IncomeEventHandler(IncomeRepository incomeRepository) {
		this.incomeRepository = incomeRepository;
	}

	public void handleIncome(Income income) {
		log.info("handle {}", income);
		if (income.getIncomeDate() == null) {
			income.setIncomeDate(LocalDate.now());
		}
		incomeRepository.save(income);
	}
}