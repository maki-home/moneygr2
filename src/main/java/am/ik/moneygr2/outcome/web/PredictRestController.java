package am.ik.moneygr2.outcome.web;

import java.util.List;
import java.util.Map;

import am.ik.moneygr2.outcome.OutcomeCategoryTrainer;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PredictRestController {
	private final OutcomeCategoryTrainer trainer;

	public PredictRestController(OutcomeCategoryTrainer trainer) {
		this.trainer = trainer;
	}

	@PostMapping(path = "v1/predict")
	public List<Map<String, Object>> predict(
			@RequestParam("outcomeName") String outcomeName) {
		return this.trainer.predict(outcomeName);
	}
}
