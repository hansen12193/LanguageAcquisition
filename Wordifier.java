/* 
 * Wordifier.java
 *
 * Implements methods for iteratively learning words from a 
 * character-segmented text file, and then evaluating how good they are
 *
 * Students may only use functionality provided in the packages
 *     java.lang
 *     java.util 
 *     java.io
 * 
 * Use of any additional Java Class Library components is not permitted 
 * 
 * Kara Yamaguchi Nathan Hansen
 * April 2014
 *
 */

import java.util.*;
import java.io.*;
import java.lang.*;

public class Wordifier {

    // loadSentences
    // Preconditions:
    //    - textFilename is the name of a plaintext input file
    // Postconditions:
    //  - A LinkedList<String> object is returned that contains
    //    all of the words in the input file, in order
    // Notes:
    //  - If opening any file throws a FileNotFoundException, print to standard error:
    //        "Error: Unable to open file " + textFilename
    //        (where textFilename contains the name of the problem file)
    //      and then exit with value 1 (i.e. System.exit(1))
    public static LinkedList<String> loadSentences( String textFilename ) {
        File inputFile = new File(textFilename);
        Scanner sc = null;
        try {
            sc = new Scanner(inputFile);
        } catch (FileNotFoundException ex) {
            System.out.println("The file " + inputFile + " cannot be opened.");
        }
        LinkedList<String> letters = new LinkedList<String>();
        while (sc.hasNext()) {
            letters.add(sc.next());
        }
        return letters;
    }

    // findNewWords
    // Preconditions:
    //    - bigramCounts maps bigrams* to the number of times the bigram appears in the data
    //    - scores maps bigrams to its bigram product score 
    //    - countThreshold is a threshold on the counts
    //    - probabilityThreshold is a threshold on the bigram product score 
    // Postconditions:
    //    - A HashSet is created and returned, containing all bigrams that meet the following criteria
    //        1) the bigram is a key in bigramCounts
    //        2) the count of the bigram is >= countThreshold
    //        3) the score of the bigram is >= probabilityThreshold
    public static HashSet<String> findNewWords( HashMap<String,Integer> bigramCounts, HashMap<String,Double> scores, int countThreshold, double probabilityThreshold ) {
        HashSet<String> goodBigrams = new HashSet<String>();
        Set<String> bigrams = bigramCounts.keySet();
        Iterator<String> it = bigrams.iterator();
        while (it.hasNext()) {
            String bigram = it.next();
            if(bigramCounts.containsKey(bigram) && bigramCounts.get(bigram) >= countThreshold && scores.get(bigram) >= probabilityThreshold) {
                //goodBigrams.add(bigram.replace(" ", ""));
                goodBigrams.add(bigram);
            }
        }

        return goodBigrams;
    }

    // resegment
    // Preconditions:
    //    - previousData is the LinkedList representation of the data
    //    - newWords is the HashSet containing the new words (after merging)
    // Postconditions:
    //    - A new LinkedList is returned, which contains the same information as
    //      previousData, but any pairs of words in the newWords set have been merged
    //      to a single entry (merge from left to right)
    //
    //      For example, if the previous linked list contained the following items:
    //         A B C D E F G H I
    //      and the newWords contained the entries "B C" and "G H", then the returned list would have 
    //         A BC D E F GH I
    public static LinkedList<String> resegment( LinkedList<String> previousData, HashSet<String> newWords ) {
        LinkedList<String> merged = new LinkedList<String>();
        List<String> previousDataArrayList = new ArrayList<String>(previousData);
        ListIterator<String> it = previousData.listIterator();
        int num = 1;
        while(num < previousData.size()) {
            String current = it.next();
            String next = previousDataArrayList.get(num);
            String possibleBigram = current + " " + next;
            if(newWords.contains(possibleBigram)) {
                merged.add(possibleBigram.replace(" ", ""));
                it.next();
                num++;
            } else {
                merged.add(current);
            }
            num ++;
        }
        if(num == previousData.size()) {
            merged.add(it.next());
        }
        return merged;
    }

    // computeCounts
    // Preconditions:
    //    - data is the LinkedList representation of the data
    //    - bigramCounts is an empty HashMap that has already been created
    // Postconditions:
    //    - bigramCounts maps each bigram appearing in the data to the number of times it appears

    public static void computeCounts(LinkedList<String> data, HashMap<String,Integer> bigramCounts ) {
        List<String> dataArrayList = new ArrayList<String>(data);
        ListIterator<String> it = data.listIterator();
        int num = 1;
        while (num < data.size()) {
            int count = 1;
            String bigram = it.next() + " " + dataArrayList.get(num);
            if (bigramCounts.containsKey(bigram)) {
                count = count + bigramCounts.get(bigram);
            }
            bigramCounts.put(bigram, count);
            num ++;
        }

    }



    // convertCountsToProbabilities 
    // Preconditions:
    //    - bigramCounts maps each bigram appearing in the data to the number of times it appears
    //    - bigramProbs is an empty HashMap that has already been created
    //    - unigramProbs is an empty HashMap that has already been created
    // Postconditions:
    //    - bigramProbs maps bigrams to their joint probability
    //        (where the joint probability of a bigram is the # times it appears over the total # bigrams)
    //    - unigramProbs maps words to their "marginal probability"
    //        (i.e. the frequency of each word over the total # bigrams)
    public static void convertCountsToProbabilities(HashMap<String,Integer> bigramCounts, HashMap<String,Double> bigramProbs, HashMap<String,Double> unigramProbs) {
        Set<String> keys = bigramCounts.keySet();
        Iterator<String> it = keys.iterator();
        Object[] tokens = keys.toArray();
        HashMap<String, Integer> unigramCount = new HashMap<String, Integer>();
        while (it.hasNext()) {
            String temp = it.next();
            String[] tempArray = temp.split(" ");
            int count = bigramCounts.get(temp);
            if (unigramCount.containsKey(tempArray[0])) {
                count = count + unigramCount.get(tempArray[0]);
            }
            unigramCount.put(tempArray[0], count);
        }
        int totalBigrams = findTotalBigrams(bigramCounts);
        for (int i = 0; i < keys.size(); i++) {
            double value = bigramCounts.get(tokens[i]);
            bigramProbs.put((String) tokens[i], (value/totalBigrams));

        }
        Set<String> uniKeys = unigramCount.keySet();
        Iterator<String> it1 = uniKeys.iterator();
        int totalUni = findTotalUnigrams(unigramCount);
        while (it1.hasNext()) {
            String temp = it1.next();
            unigramProbs.put(temp,(double) unigramCount.get(temp)/totalUni);
        }
    }

    //Finds the total number of unigrams in the text
    private static int findTotalUnigrams (HashMap<String,Integer> unigramCount) {
        Collection<Integer> values = unigramCount.values();
        Iterator<Integer> it = values.iterator();
        int total = 0;
        while (it.hasNext()) {
            total += it.next();
        }
        return total;
    }

    //Finds the total number of bigrams in the text
    private static int findTotalBigrams (HashMap<String,Integer> bigramCounts) {
        Set<String> keys = bigramCounts.keySet();
        Object[] tokens = keys.toArray();
        int total = 0;
        for (int i = 0; i < keys.size(); i++) {
            total = total + bigramCounts.get(tokens[i]);
        }
        return total;
    }

    // getScores
    // Preconditions:
    //    - bigramProbs maps bigrams to to their joint probability
    //    - unigramProbs maps words to their "marginal probability"
    // Postconditions:
    //    - A new HashMap is created and returned that maps bigrams to
    //      their "bigram product scores", defined to be P(w1|w2)P(w2|w1)
    //      The above product is equal to P(w1,w2)/sqrt(P(w1)*P(w2)), which 
    //      is the form you will want to use
    public static HashMap<String,Double> getScores( HashMap<String,Double> bigramProbs, HashMap<String,Double> unigramProbs ) {
        HashMap<String,Double> bigramScores = new HashMap<String, Double>();
        Set<String> bigrams = bigramProbs.keySet();
        Iterator<String> it = bigrams.iterator();
        while (it.hasNext()) {
            String currBigram = it.next();
            if (currBigram.contains("<dummy>")) {
                bigramScores.put(currBigram, (double) -1);
            } else {
                Scanner sc = new Scanner(currBigram);
                Double bigramProb = bigramProbs.get(currBigram);
                String uni1 = sc.next();
                Double uni1Prob = unigramProbs.get(uni1);
                String uni2 = sc.next();
                Double uni2Prob = unigramProbs.get(uni2);
                bigramScores.put(currBigram, ((bigramProb)/(Math.sqrt(uni1Prob * uni2Prob))));
            }
        }
        return bigramScores;
    }

    // getVocabulary
    // Preconditions:
    //    - data is a LinkedList representation of the data
    // Postconditions:
    //    - A new HashMap is created and returned that maps words
    //      to the number of times they appear in the data
    public static HashMap<String,Integer> getVocabulary( LinkedList<String> data ) {
        Iterator<String> it = data.listIterator();
        HashMap<String, Integer> words = new HashMap<String, Integer>();
        while (it.hasNext()) {
            int count = 1;
            String currKey = it.next();
            if(words.containsKey(currKey)) {
                count = count + words.get(currKey);
            }
            words.put(currKey, count);
        }
        return words;
    }

    // loadDictionary
    // Preconditions:
    //    - dictionaryFilename is the name of a dictionary file
    //      the dictionary has one word per line
    // Postconditions:
    //    - A new HashSet is created and returned that contains
    //      all unique words appearing in the dictionary
    public static HashSet<String> loadDictionary( String dictionaryFilename ) {
        File inputFile = new File(dictionaryFilename);
        Scanner sc = null;
        try {
            sc = new Scanner(inputFile);
        } catch (FileNotFoundException ex) {
            System.out.println("The file " + inputFile + " cannot be opened.");
        }
        HashSet<String> dictionary = new HashSet<String>();
        while (sc.hasNext()) {
            dictionary.add(sc.next());
        }
        return dictionary;
    }

    // printNumWordsDiscovered
    // Preconditions:
    //    - vocab maps words to the number of times they appear in the data
    //    - dictionary contains the words in the dictionary
    // Postconditions:
    //    - Prints each word in vocab that is also in dictionary, in sorted order (alphabetical, ascending)
    //        Also prints the counts for how many times each such word occurs
    //    - Prints the number of unique words in vocab that are also in dictionary 
    //    - Prints the total of words in vocab (weighted by their count) that are also in dictionary 
    // Notes:
    //    - See example output for formatting
    public static void printNumWordsDiscovered( HashMap<String,Integer> vocab, HashSet<String> dictionary ) {
        Map<String, Integer> newMap = new TreeMap<String, Integer>(vocab);
        Set<String> alphaWords = newMap.keySet();
        Iterator<String> it = alphaWords.iterator();
        int totalNumWordsDiscovered = 0;
        int totalUniqueWords = 0;
        while(it.hasNext()) {
            String newWord = it.next();
            if(dictionary.contains(newWord)) {
                totalNumWordsDiscovered += vocab.get(newWord);
                totalUniqueWords++;
                System.out.println("Discovered " + newWord + " " + "(count " + vocab.get(newWord) + ")" );
            }
        }
        System.out.println("Number of Unique Words Discovered: " + totalUniqueWords);
        System.out.println("Total Number of Words Discovered: " + totalNumWordsDiscovered);
    }

}