package am.ik.moneygr2.outcome.web;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import am.ik.moneygr2.outcome.Outcome;
import am.ik.moneygr2.outcome.OutcomeCategory;

public class GoldPointCartImportItem implements Serializable {
	private LocalDate outcomeDate;

	private String outcomeName;

	private Long amount;

	private Integer outcomeCategoryId;

	private List<Outcome> similar;

	private boolean included;

	public GoldPointCartImportItem(LocalDate outcomeDate, String outcomeName, Long amount) {
		this.outcomeDate = outcomeDate;
		this.outcomeName = outcomeName;
		this.amount = amount;
	}

	public GoldPointCartImportItem() {
	}

	public LocalDate getOutcomeDate() {
		return outcomeDate;
	}

	public void setOutcomeDate(LocalDate outcomeDate) {
		this.outcomeDate = outcomeDate;
	}

	public String getOutcomeDateString() {
		return outcomeDate.toString();
	}

	public void setOutcomeDateString(String outcomeDate) {
		this.outcomeDate = LocalDate.parse(outcomeDate);
	}

	public String getOutcomeName() {
		return outcomeName;
	}

	public void setOutcomeName(String outcomeName) {
		this.outcomeName = outcomeName;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public Integer getOutcomeCategoryId() {
		return outcomeCategoryId;
	}

	public void setOutcomeCategoryId(Integer outcomeCategoryId) {
		this.outcomeCategoryId = outcomeCategoryId;
	}

	public List<Outcome> getSimilar() {
		return similar;
	}

	public void setSimilar(List<Outcome> similar) {
		this.similar = similar;
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public Outcome toOutcome(String username) {
		final Outcome outcome = new Outcome();
		outcome.setOutcomeDate(this.outcomeDate);
		outcome.setOutcomeName(this.outcomeName);
		outcome.setAmount(this.amount);
		final OutcomeCategory outcomeCategory = new OutcomeCategory();
		outcomeCategory.setCategoryId(this.outcomeCategoryId);
		outcome.setOutcomeCategory(outcomeCategory);
		outcome.setQuantity(1);
		outcome.setExpense(false);
		outcome.setOutcomeBy(username);
		outcome.setCreditCard(true);
		outcome.setCreatedBy(username);
		outcome.setUpdatedBy(username);
		outcome.setCreatedAt(Instant.now());
		outcome.setUpdatedAt(Instant.now());
		return outcome;
	}

	@Override
	public String toString() {
		return "GoldPointCartImportItem{" +
				"outcomeDate=" + outcomeDate +
				", outcomeName='" + outcomeName + '\'' +
				", amount=" + amount +
				", outcomeCategoryId=" + outcomeCategoryId +
				", similar=" + similar +
				", included=" + included +
				'}';
	}
}
