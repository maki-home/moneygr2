package am.ik.moneygr2.income;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IncomeCategoryRepository
		extends CrudRepository<IncomeCategory, Integer> {
	@Query("SELECT x FROM IncomeCategory x ORDER BY x.categoryId")
	List<IncomeCategory> findAll();
}