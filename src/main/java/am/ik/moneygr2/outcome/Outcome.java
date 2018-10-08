package am.ik.moneygr2.outcome;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import am.ik.moneygr2.outcome.event.OutcomeUpdateEvent;
import org.hibernate.annotations.DynamicUpdate;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Outcome extends AbstractAggregateRoot implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long outcomeId;
	@NotNull
	private String outcomeName;
	@NotNull
	@Min(0)
	private Long amount;
	@NotNull
	@Min(0)
	private Integer quantity;
	@NotNull
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate outcomeDate;
	@NotNull
	private String outcomeBy;
	@ManyToOne
	@JoinColumn(name = "category_id")
	@NotNull
	private OutcomeCategory outcomeCategory;
	private boolean isCreditCard;

	// Audit
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@CreatedBy
	@Column(updatable = false)
	private String createdBy;
	@LastModifiedDate
	private Instant updatedAt;
	@LastModifiedBy
	private String updatedBy;

	public Outcome update() {
		super.registerEvent(new OutcomeUpdateEvent(this));
		return this;
	}

	public Long getOutcomeId() {
		return outcomeId;
	}

	public void setOutcomeId(Long outcomeId) {
		this.outcomeId = outcomeId;
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public LocalDate getOutcomeDate() {
		return outcomeDate;
	}

	public void setOutcomeDate(LocalDate outcomeDate) {
		this.outcomeDate = outcomeDate;
	}

	public String getOutcomeBy() {
		return outcomeBy;
	}

	public void setOutcomeBy(String outcomeBy) {
		this.outcomeBy = outcomeBy;
	}

	public OutcomeCategory getOutcomeCategory() {
		return outcomeCategory;
	}

	public void setOutcomeCategory(OutcomeCategory outcomeCategory) {
		this.outcomeCategory = outcomeCategory;
	}

	public boolean isCreditCard() {
		return isCreditCard;
	}

	public void setCreditCard(boolean creditCard) {
		isCreditCard = creditCard;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public interface InlineCategory {
		Long getOutcomeId();

		String getOutcomeName();

		Long getAmount();

		Integer getQuantity();

		LocalDate getOutcomeDate();

		String getOutcomeBy();

		OutcomeCategory getOutcomeCategory();

		boolean isCreditCard();
	}

	public interface SummaryByDate {
		LocalDate getOutcomeDate();

		Long getSubTotal();
	}

	public interface SummaryByParentCategory {
		Integer getParentCategoryId();

		String getParentCategoryName();

		Long getSubTotal();
	}

	@Override
	public String toString() {
		return "Outcome{" + "outcomeId=" + outcomeId + ", outcomeName='" + outcomeName
				+ '\'' + ", outcomeDate=" + outcomeDate + '}';
	}
}