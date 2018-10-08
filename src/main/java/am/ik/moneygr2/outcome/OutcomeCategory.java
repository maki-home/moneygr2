package am.ik.moneygr2.outcome;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class OutcomeCategory implements Serializable {
	@Id
	@NotNull
	private Integer categoryId;
	@NotNull
	private String categoryName;
	@ManyToOne
	@JoinColumn(name = "parent_category_id")
	@NotNull
	private ParentOutcomeCategory parentOutcomeCategory;

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public ParentOutcomeCategory getParentOutcomeCategory() {
		return parentOutcomeCategory;
	}

	public void setParentOutcomeCategory(ParentOutcomeCategory parentOutcomeCategory) {
		this.parentOutcomeCategory = parentOutcomeCategory;
	}
}