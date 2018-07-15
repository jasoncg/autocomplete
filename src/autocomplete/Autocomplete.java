package autocomplete;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Autocomplete user interface
 * 
 * @author jasoncg
 *
 */
public class Autocomplete {

	private static AutocompleteProvider ac = new AutocompleteProvider();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String option="";
		while(!option.equals("x")) {
			System.out.println("");
			System.out.println("Autocomplete Test");
			System.out.println("r	Provide training sample");
			System.out.println("t	Test for candidates");
			System.out.println("p	Print whole autocomplete tree");
			System.out.println("x	Exit program");
			System.out.print(": ");
			
			option = sc.nextLine();

			if(option.equals("p")) {
				Map<Character, Node> roots = ac.getDictionary();
				for(Character c: roots.keySet()) {
					Node node = roots.get(c);
					System.out.println("ROOT NODE["+c+"] "+node.toString(1));

				}
				
			} else
			if(option.equals("r")) {
				System.out.println("Training Sample:");
				String sample = sc.nextLine();
				
				ac.train(sample);
				System.out.println("Trained with ["+sample+"]");
			} else
			if(option.equals("t")) {
				System.out.println("Test Input:");
				String fragment = sc.nextLine();
				
				List<Candidate> results = ac.getWords(fragment);
				System.out.println("Found "+results.size()+" candidates:");

				for(Candidate c: results) {
					System.out.println("\t"+c.getWord()+"\t"+c.getConfidence());
				}
			} else if(option.equals("x")) {
				System.out.println("Exit");
			} else {
				System.out.println("Unrecognized Operation");
			}
		}
		sc.close();

	}
}