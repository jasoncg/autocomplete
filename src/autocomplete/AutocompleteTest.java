package autocomplete;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
/**
 * Runs a selection of tests on AutocompleteProvider
 * @author jasoncg
 *
 */
class AutocompleteTest {
	/**
	 * More easily generate test samples
	 * 
	 * TestSample("training sample text", "testInputToken")
	 * 
	 * add candidates with
	 * addCandidate("candidateToken", candidateConfidence)
	 * 
	 * then test with
	 * runTest()
	 * 
	 * @author jasoncg
	 *
	 */
	private class TestSample {
		private String trainingSample;
		private String input;
		private List<Candidate> expectedResults = new ArrayList<Candidate>();
		
		public TestSample(String trainingSample, String input) {
			this.trainingSample=trainingSample;
			this.input=input;
		}
		public TestSample addCandidate(String word, int confidence) {
			expectedResults.add(new Candidate(word, confidence));
			Sort();
			return this;
		}
		private void Sort() {
			if(expectedResults==null)
				return;
			
			//Ensure test sample candidates are sorted
			Collections.sort(expectedResults, new Comparator<Candidate>() {
				@Override
				public int compare(Candidate c1, Candidate c2) {
					int result=-c1.getConfidence().compareTo(c2.getConfidence());
					
					if(result==0)
						result = c1.getWord().compareTo(c2.getWord());
					return result;
				}
			});
		}
		public void assertMatch(List<Candidate> resultCandidates) {
			assertNotEquals(resultCandidates, null, "Result candidates are null");
			assertEquals(resultCandidates.size(), expectedResults.size(), "Candidate result count mismatch: Expected "+expectedResults.size()+" but received "+resultCandidates.size());
			for(int i=0;i<resultCandidates.size();i++) {
				assertEquals(resultCandidates.get(i).getWord(), expectedResults.get(i).getWord(),"Candidate word mismatch: Expected "+expectedResults.get(i).getWord()+" but recieved "+resultCandidates.get(i).getWord());
				assertEquals(resultCandidates.get(i).getConfidence(), expectedResults.get(i).getConfidence(),"Candidate confidence mismatch: Expected "+expectedResults.get(i).getConfidence()+" but recieved "+resultCandidates.get(i).getConfidence());
			}
		}
		public void RunTest() {
			AutocompleteProvider provider=new AutocompleteProvider();
			provider.train(trainingSample);
			List<Candidate> results = provider.getWords(input);
			assertMatch(results);
		}
	}
	
	@Test
	public void testInput1() {
		TestSample testSample=new TestSample(
				"The third thing that I need to tell you is that this thing does not think thoroughly.",
				"thi");
		testSample.addCandidate("thing", 2)
					.addCandidate("think", 1)
					.addCandidate("third", 1)
					.addCandidate("this", 1);
				
		testSample.RunTest();
	}
	@Test
	public void testInput2() {
		TestSample testSample=new TestSample(
				"The third thing that I need to tell you is that this thing does not think thoroughly.",
				"nee");
		testSample.addCandidate("need", 1);
				
		testSample.RunTest();
	}

	@Test
	public void testInput3() {
		TestSample testSample=new TestSample(
				"The third thing that I need to tell you is that this thing does not think thoroughly.",
				"th");
		testSample.addCandidate("that", 2)
					.addCandidate("thing", 2)
					.addCandidate("think", 1)
					.addCandidate("this", 1)
					.addCandidate("third", 1)
					.addCandidate("the", 1)
					.addCandidate("thoroughly", 1);
				
		testSample.RunTest();
	}
	
	/// Additional Tests
	/**
	 * With no input expect no results
	 */
	@Test
	public void testInput4BlankInput() {
		TestSample testSample=new TestSample(
				"The third thing that I need to tell you is that this thing does not think thoroughly.",
				"");
				
		testSample.RunTest();
	}
	/**
	 * Non alpha characters expect not results
	 */
	@Test
	public void testInput5NonAlpha() {
		TestSample testSample=new TestSample(
				"The third thing that I need to tell you is that this thing does not think thoroughly.",
				"123412341234&^%*&%^");
				
		testSample.RunTest();
	}

	/**
	 *Adding shorter words that already exist should be supported
	 */
	@Test
	public void testInput6ShorterWords() {
		TestSample testSample=new TestSample(
				"Thats that thing that things have",
				"th");

		testSample.addCandidate("that", 2)
					.addCandidate("thing", 2)
					.addCandidate("thats", 1)
					.addCandidate("things", 1);
		
		testSample.RunTest();
	}
	/**
	 * Blank / untrained should return no results
	 */
	@Test
	public void testInput7BlankSample() {
		TestSample testSample=new TestSample(
				"",
				"th");
		
		testSample.RunTest();
	}
}
