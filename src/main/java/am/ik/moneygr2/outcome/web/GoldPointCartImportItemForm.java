package am.ik.moneygr2.outcome.web;

import java.io.Serializable;
import java.util.List;

public class GoldPointCartImportItemForm implements Serializable {
	private List<GoldPointCartImportItem> items;

	public List<GoldPointCartImportItem> getItems() {
		return items;
	}

	public void setItems(List<GoldPointCartImportItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "GoldPointCartImportItemForm{" +
				"items=" + items +
				'}';
	}
}
