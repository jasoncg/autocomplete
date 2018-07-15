package autocomplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jasoncg
 *
 * The trained structure is essentially a tree, with each node
 * containing a single letter as a key and a list of leaf nodes (suggestions), or 
 * more nodes if additional data is necessary  
 *
 */
public class Node {
	/**
	 * links to the next character in the string
	 */
	private Map<Character, Node> children = null;

	/**
	 * If this is set, then this node is a potential candidate
	 * If not set, then this is just a placeholder.
	 * 
	 * For example, "that" would consist of up to 4 nodes.  The nodes for
	 * "t", "th", and "tha" would not have candidate strings since those aren't
	 * actual words.
	 */
	private String nodeCandidate;
	private int trainedCount = 1;
	
	public Node(String candidate) {
		this.nodeCandidate = candidate;
	}

	@Override
	public String toString() {
		return toString(0);
	}
	public String toString(int depth) {
		String ws = depth==0?"":String.format(String.format("%%%ds", depth), " ");
		
		String result=ws+"| Node["+(nodeCandidate!=null?nodeCandidate:"*")+"]";
		if(children!=null) {
			result+="\n";
			for(Character c : children.keySet()) {
				result+=ws+" ["+c+"]\n";
				result+=children.get(c).toString(depth+1)+"\n";
			}
		}
		return result;
	}
	/**
	 * The next set of words in the chain indexed by the next letter
	 * @return
	 */
	public Map<Character, Node> getChildren() {
		return children;
	}
	/**
	 * If this is a candidate node, the trained word.  Or null if this is
	 * not a candidate node.
	 * @return
	 */
	public String getCandidate() {
		return nodeCandidate;
	}
	/**
	 * The number of times this candidate has been trained
	 * @return
	 */
	public int getTrainedCount() {
		return this.trainedCount;
	}
	/**
	 * Used when splitting nodes to reset the confidence level for this candidate node
	 * @param trainedCount
	 */
	public void setTrainedCount(int trainedCount) {
		this.trainedCount = trainedCount;
	}

	/**
	 * Generates a list of Candidates for the given fragment string
	 * 
	 * @param fragment the fragment to search for candidates
	 * @param depth the depth of this node
	 * @param results the list the results will be added to (or null to generate a new list)
	 * @return the list the results were added to
	 */
	public List<Candidate> getWords(String fragment, int depth, List<Candidate> results) {
		//if(fragment.length()<=depth)
		//	return results;
		if(fragment==null||fragment.length()==0)
			return results;
		
		if(results==null)
			results = new ArrayList<Candidate>();
		
		//This would allow "think" to return as a possibility for "thinking"
		//if(candidate!=null && candidate.length()>=depth)	 
		
		//If this node has a candidate (and isn't SHORTER than the fragment), add to list
		if(nodeCandidate!=null && nodeCandidate.length()>=fragment.length())
			results.add(new Candidate(nodeCandidate, trainedCount));
		
		//If there are any child nodes, scan any that match the fragment
		if(children!=null) {
			if(depth<fragment.length()) {
				char nextChar = fragment.charAt(depth);
				if(children.containsKey(nextChar)) {
					children.get(nextChar).getWords(fragment, depth+1, results);
				}
			} else {
				for(Node child:children.values()) {
					child.getWords(fragment, depth+1, results);
				}
			}
			
		}			
		return results;
	}
	
	public Node train(String token) {
		return train(token, 0);
	}
	/**
	 * Converts a candidate node into a non-candidate node.
	 * 
	 * This is necessary when adding a training a new word
	 * that is shorter than an existing word.
	 * 
	 * For example, if there is a candidate node for "things"
	 * and "thing" is added then "thing" needs to be injected
	 * in the tree closer to the root than "things"
	 * @param index The index into the nodeCandidate string we are splitting at
	 */
	protected void splitNode(int index) {
		//System.out.println("splitNode("+index+")");
		if(nodeCandidate==null)
			return;
		
		if(index>=nodeCandidate.length())
			return;
		String token = nodeCandidate;
		nodeCandidate=null;
		Node node = train(token, index);
		if(node==null) {
			System.out.println("node.splitNode("+token+", "+index+") Error: unable to add token");
		} else 
			node.setTrainedCount(trainedCount-1);
	}
	/**
	 * Trains a new token in this part of the hierarchy
	 * @param newToken The token to add
	 * @param depth The index of the newToken string 
	 * @return The candidate node newToken was added to
	 */
	protected Node train(String newToken, int depth) {
		Node result = null;
		//System.out.println("Node["+(nodeCandidate!=null?nodeCandidate:"*")+"].train("+newToken+", "+depth+")");
		if(newToken.length()<depth)
			return result;
		//If this node already has a candidate, but it conflicts with the new token,
		//then split this node 
		if(nodeCandidate!=null) {
			if(nodeCandidate.length()>newToken.length() ||
			   !newToken.startsWith(nodeCandidate)) {
				//!candidate.substring(0, depth).equals(token.substring(0, depth))) {
				//there is a conflict: this node can not contain the new token, so
				//unmark this node as a candidate and push it down the tree
				splitNode(depth);
				
				
				if(depth>=newToken.length()-1) {

					//Make this a candidate node
					nodeCandidate=newToken;
					this.trainedCount=1;
					newToken = null;
					result = this;
					return result;
				}
			} 
		} else {
			
			if(depth==newToken.length()) {
				//Make this a candidate node
				nodeCandidate=newToken;
				this.trainedCount=1;
				newToken = null;
				result = this;
			}
		}
		trainedCount+=1;
		
		if(newToken!=null) {
			if(children==null) {
				children = new HashMap<Character, Node>();
			}
			char currentChar = newToken.charAt(depth);
			
			if(children.containsKey(currentChar)) {
				Node node = children.get(currentChar);
				if(node.getCandidate()!=null && node.getCandidate().equals(newToken)) {
					//token has been added more than once, increase confidence since the word is more common
					node.setTrainedCount(node.getTrainedCount()+1);
					result = node;
				} else {
					//Add the token as a child of a previously added node
					result = node.train(newToken, depth+1);
				}
			} else {
				//Create a leaf node directly below this one
				result = new Node(newToken);
				children.put(currentChar, result);
			}
		}
		return result;
	}
}

