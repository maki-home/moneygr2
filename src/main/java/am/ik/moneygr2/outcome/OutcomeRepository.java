package am.ik.moneygr2.outcome;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

public interface OutcomeRepository extends CrudRepository<Outcome, Long> {
	@Query("SELECT x FROM Outcome x JOIN FETCH x.outcomeCategory ORDER BY x.outcomeDate, x.outcomeCategory.categoryId")
	@Override
	Iterable<Outcome> findAll();

	List<Outcome> findByOutcomeNameContaining(@Param("outcomeName") String outcomeName);

	@Query("SELECT x FROM Outcome x JOIN FETCH x.outcomeCategory WHERE x.outcomeDate BETWEEN :fromDate AND :toDate ORDER BY x.outcomeCategory.categoryId")
	List<Outcome> findByOutcomeDate(
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("fromDate") LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("toDate") LocalDate toDate);

	@Query("SELECT x.outcomeDate AS outcomeDate, SUM(x.amount * x.quantity) AS subTotal FROM Outcome x WHERE x.outcomeDate BETWEEN :fromDate AND :toDate GROUP BY x.outcomeDate ORDER BY x.outcomeDate ASC")
	List<Outcome.SummaryByDate> findSummaryByDate(
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("fromDate") LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("toDate") LocalDate toDate);

	@Query("SELECT x.outcomeCategory.parentOutcomeCategory.parentCategoryName AS parentCategoryName, x.outcomeCategory.parentOutcomeCategory.parentCategoryId AS parentCategoryId, SUM(x.amount) AS subTotal FROM Outcome x WHERE x.outcomeDate BETWEEN :fromDate AND :toDate GROUP BY x.outcomeCategory.parentOutcomeCategory.parentCategoryId ORDER BY x.outcomeCategory.parentOutcomeCategory.parentCategoryId ASC")
	List<Outcome.SummaryByParentCategory> findSummaryByParentCategory(
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("fromDate") LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("toDate") LocalDate toDate);

	@Query("SELECT x FROM Outcome x JOIN FETCH x.outcomeCategory WHERE x.outcomeCategory.parentOutcomeCategory.parentCategoryId=:parentCategoryId AND x.outcomeDate BETWEEN :fromDate AND :toDate")
	List<Outcome> findByParentCategoryId(
			@Param("parentCategoryId") Integer parentCategoryId,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("fromDate") LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Param("toDate") LocalDate toDate);
}