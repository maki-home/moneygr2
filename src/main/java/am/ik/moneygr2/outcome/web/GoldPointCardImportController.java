package am.ik.moneygr2.outcome.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import am.ik.moneygr2.security.MoneygrOidcUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(path = "outcomes/goldpoint")
public class GoldPointCardImportController {
	private final ImportItemHelper importItemHelper;

	public GoldPointCardImportController(ImportItemHelper importItemHelper) {
		this.importItemHelper = importItemHelper;
	}

	@GetMapping
	public String form(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute ImportForm form, Model model) {
		model.addAttribute("user", user.asMember());
		return "goldpoint";
	}

	@PostMapping(params = "preview")
	public String preview(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute ImportForm form, Model model) throws Exception {
		return this.importItemHelper.preview(user, form, model, line -> {
			final List<String> columns = Arrays.asList(line.split(","));
			if (!columns.get(0).startsWith("20")) {
				return null;
			}
			final LocalDate outcomeDate = LocalDate.parse(columns.get(0).replace("/", "-"));
			final String outcomeName = columns.get(1).replace("ＰＡＹＰＡＹ＊", "").replace("セブンーイレブン", "セブンイレブン");
			final long amount = Long.parseLong(columns.get(2));
			return new ImportItem(outcomeDate, outcomeName, amount);
		}, "goldpoint");
	}

	@PostMapping(params = "import")
	public String bulkImport(@AuthenticationPrincipal MoneygrOidcUser user, @ModelAttribute ImportItemForm form, Model model, RedirectAttributes attributes) throws Exception {
		return this.importItemHelper.bulkImport(user, form, model, attributes, "goldpoint");
	}
}
