package autocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author jasoncg
 *
 * The trained structure is essentially a tree, with each node
 * containing a single letter as a key and a list of leaf nodes (suggestions), or 
 * more nodes if additional data is necessary  
 *
 */
public class AutocompleteProvider {

	/**
	 * Whitelist tokens to train.
	 * 
	 * I assume that only English letters a-z are use for training based on the training
	 * sample dropping the period symbol.
	 * 
	 * Additional characters can be whitelisted by modifying this regular expression.
	 */
	private String whitelistCharactersRegex = "[^a-z]";
	
	private Map<Character, Node> dictionary = new HashMap<Character, Node>();
	
	/**
	 * The core data structure of the trained data
	 * @return
	 */
	public  Map<Character, Node> getDictionary() {
		return dictionary;
	}
	
	/**
	 * returns list of candidates ordered by confidence
	 * @param fragment
	 * @return
	 */
	public List<Candidate> getWords(String fragment) {
		List<Candidate> results = new ArrayList<Candidate>();
		if(fragment==null||fragment.length()==0)
			return results;
		fragment = fragment.toLowerCase();

		char startChar = fragment.charAt(0);

		if(!dictionary.containsKey(startChar)) {
			//results.add(new Candidate(fragment, 0));
		} else {
			Node root = dictionary.get(startChar);
			root.getWords(fragment, 1, results);
			
		}

		Collections.sort(results, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate c1, Candidate c2) {
				int result=-c1.getConfidence().compareTo(c2.getConfidence());
				
				if(result==0)
					result = c1.getWord().compareTo(c2.getWord());
				return result;
			}
		});
		return results;
	}
	/**
	 * trains the algorithm with the provided passage
	 * @param passage
	 */
	public void train(String passage) {
		if(passage==null || passage.length()==0)
			return;
		passage = passage.toLowerCase();
		
		StringTokenizer st =new StringTokenizer(passage);
		
		while(st.hasMoreTokens()) {
			String token = st.nextToken().replaceAll(whitelistCharactersRegex, "");
			if(token==null||token.length()==0)
				continue;
			System.out.println("Training with token: "+token);
			
			Node node = null;
			
			char startChar = token.charAt(0);
			if(!dictionary.containsKey(startChar)) {
				node = new Node(token);
				dictionary.put(startChar, node);
			} else {
				node = dictionary.get(startChar);
				node.train(token, 1);
			}
		}
	}
}
