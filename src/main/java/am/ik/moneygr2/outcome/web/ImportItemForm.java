package am.ik.moneygr2.outcome.web;

import java.io.Serializable;
import java.util.List;

public class ImportItemForm implements Serializable {
	private List<ImportItem> items;

	public List<ImportItem> getItems() {
		return items;
	}

	public void setItems(List<ImportItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "ImportItemForm{" +
				"items=" + items +
				'}';
	}
}
