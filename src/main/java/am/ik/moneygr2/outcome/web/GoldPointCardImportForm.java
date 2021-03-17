package am.ik.moneygr2.outcome.web;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class GoldPointCardImportForm implements Serializable {
	private MultipartFile file;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "GoldPointCardImportForm{" +
				"file=" + file +
				'}';
	}
}
