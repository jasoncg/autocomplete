package autocomplete;
/**
 * A word/confidence candidate pair
 * @author jasoncg
 *
 */
public class Candidate {
	private String word;
	private Integer confidence;
	
	public Candidate(String word, Integer confidence) {
		this.word = word;
		this.confidence=confidence;
	}
	
	// Returns the autocomplete candidate
	public String getWord() {
		return word;
	}
	// Returns the confidence for the candidate
	public Integer getConfidence() {
		return confidence;
	}
	@Override
	public String toString() {
		return "Candidate("+word+","+confidence+")";
	}
}
