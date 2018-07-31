package hamiguazzz.word;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public class WordTrace {
	private Word word;
	private int progress;
	private int easy;
	private int forget;
	private Set<String> tags;
	private LocalDate last_read_time;
	private LocalDate first_read_time;

	//region Construction
	public WordTrace(Word word, int progress, int easy, int forget, Set<String> tags, LocalDate last_read_time, LocalDate first_read_time) {
		this.word = word;
		this.progress = progress;
		this.easy = easy;
		this.forget = forget;
		this.tags = tags;
		this.last_read_time = last_read_time;
		this.first_read_time = first_read_time;
	}
	//endregion

	//region Generate Codes
	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setEasy(int easy) {
		this.easy = easy;
	}

	public void setForget(int forget) {
		this.forget = forget;
	}

	public void updateLastRead() {
		if (first_read_time == null) first_read_time = LocalDate.now();
		last_read_time = LocalDate.now();
	}

	public boolean addTag(String tag) {
		return tags.add(tag);
	}

	public Word getWord() {
		return word;
	}

	public int getProgress() {
		return progress;
	}

	public int getEasy() {
		return easy;
	}

	public int getForget() {
		return forget;
	}

	public Set<String> getTags() {
		return tags;
	}

	public LocalDate getLast_read_time() {
		return last_read_time;
	}

	public LocalDate getFirst_read_time() {
		return first_read_time;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WordTrace wordTrace = (WordTrace) o;
		return word.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(word);
	}

	@Override
	public String toString() {
		return "WordTrace{" +
				"word=" + word.getWord() +
				", progress=" + progress +
				", easy=" + easy +
				", forget=" + forget +
				", tags=" + tags +
				", last_read_time=" + last_read_time +
				", first_read_time=" + first_read_time +
				'}';
	}
	//endregion
}
