package am.ik.moneygr2.outcome;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Component
public class OutcomeCategoryTrainer {
	private final Logger log = LoggerFactory.getLogger(OutcomeCategoryTrainer.class);

	private final JdbcTemplate jdbcTemplate;
	private final RestTemplate restTemplate;
	private final String mecabApiUrl;

	public OutcomeCategoryTrainer(JdbcTemplate jdbcTemplate, RestTemplateBuilder builder,
			@Value("${mecab-api.url}") String mecabApiUrl) {
		this.jdbcTemplate = jdbcTemplate;
		this.restTemplate = builder.build();
		this.mecabApiUrl = mecabApiUrl;
	}

	private List<String> extractWords(String outcomeName) {
		JsonNode node = restTemplate
				.getForObject(UriComponentsBuilder.fromUriString(mecabApiUrl)
						.queryParam("text", UriUtils.encode(outcomeName.trim(), "UTF-8"))
						.build(true).toUri(), JsonNode.class);
		List<String> words = StreamSupport.stream(node.get("nodes").spliterator(), false)
				.filter(n -> "名詞".equals(n.get("feature").get(0).asText())
						&& "一般".equals(n.get("feature").get(1).asText()))
				.map(n -> n.get("surface").asText()).filter(s -> s.length() > 1)
				.distinct().collect(Collectors.toList());
		return words.isEmpty() ? Collections.singletonList(outcomeName) : words;
	}

	public Map<String, Object> train(String outcomeName, Integer categoryId) {
		log.info("train {} => {}", outcomeName, categoryId);
		List<String> target = extractWords(outcomeName);
		int point = target.size() == 1 ? 10 : 1;
		jdbcTemplate.batchUpdate(
				"INSERT INTO predict(word, category_id, cnt) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE word = ?, cnt  = cnt + ?",
				target.stream().map(s -> new Object[] { s, categoryId, point, s, point })
						.collect(Collectors.toList()));
		return Map.of("words", target, "categoryId", categoryId);
	}

	public List<Map<String, Object>> predict(String outcomeName) {
		List<String> target = extractWords(outcomeName);
		Map<Integer, Integer> candidate = new HashMap<>();
		target.forEach(w -> {
			List<Map<String, Object>> ret = jdbcTemplate.queryForList(
					"SELECT category_id, cnt FROM predict WHERE word = ?", w);
			ret.forEach(x -> {
				Integer categoryId = (Integer) x.get("category_id");
				Integer cnt = (Integer) x.get("cnt");
				candidate.computeIfPresent(categoryId, (k, v) -> v + cnt + 10);
				candidate.putIfAbsent(categoryId, cnt);
			});
		});
		long sum = candidate.values().stream().mapToInt(Integer::intValue).sum();
		return candidate.entrySet().stream() //
				.map(e -> Map.<String, Object>of("categoryId", e.getKey(), "probability",
						((double) e.getValue()) / sum)) //
				.collect(Collectors.toList());
	}
}