package am.ik.moneygr2.outcome;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OutcomeCategoryRepository
		extends CrudRepository<OutcomeCategory, Integer> {
	@Query("SELECT x FROM OutcomeCategory x JOIN FETCH x.parentOutcomeCategory ORDER BY x.categoryId")
	List<OutcomeCategory> findAll();


	default Map<String, Map<Integer, String>> outcomeCategories() {
		final List<OutcomeCategory> categories = this.findAll();
		final Map<String, Map<Integer, String>> cat = new LinkedHashMap<>();
		for (OutcomeCategory category : categories) {
			final String key = category.getParentOutcomeCategory().getParentCategoryName();
			cat.computeIfAbsent(key, x -> new LinkedHashMap<>());
			cat.get(key).put(category.getCategoryId(), category.getCategoryName());
		}
		return cat;
	}
}