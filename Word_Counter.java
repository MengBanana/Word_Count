/*
 * This program counts the words in a collection of books available 
 * online in plain text format from Project Gutenberg. The program 
 * uses an ExecutorService to run the counting tasks concurrently.
 * 
 * The problem is to provide a WordCounter class that implements
 * Callable<Integer>. The call() method counts the number of words
 * in its input and returns the count as an Integer.The WordCounter 
 * is used in main() to submit tasks to an ExecutorService.
 * 
 * The WordCounter is constructed with a URL that represents the URL 
 * of a book. The URL.openStream() method returns an InputStream that 
 * can be used to read lines through a BufferedReader. The reader must 
 * be closed after the last line is read.
 * 
 * Use a StringTokenizer on each input line to count the number of
 * words, and keep a running total for all the lines.
 */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Word_Counter {
	/**
	 * This class counts words from a URL location.
	 */
	static class WordCounter implements Callable<Integer> {
		
		/** The URL of a book*/
		public URL url;
		
		/**
		 * The WordCounter is constructed with a URL that represents 
		 * the URL of a book
		 * @param url the URL of a book
		 */
		public WordCounter(URL url) {
			this.url = url;
		}
		
		/**
		 * Override of call function, counts the number of words
		 * in its input and returns the count as an Integer
		 * @throws Exception if errors occur
		 */
		@Override
		public Integer call() throws Exception {
			// read through BufferedReader
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			// need a string to save each line from the stream
			String s;
			// StringTokenizer param
			String delim = " ";
			// number of words initialization
			int res = 0;
			while ((s = in.readLine()) != null) { // keep reading until null
				// https://docs.oracle.com/javase/7/docs/api/java/util/StringTokenizer.html
				// https://stackoverflow.com/questions/16726121/use-stringtokenizer-to-count-number-of-words-and-characters-in-words
				StringTokenizer tokenizer = new StringTokenizer(s, delim, false);
				res += tokenizer.countTokens();
			}
			in.close(); // close BufferedReader
			return res;
		}
	}

	/**
	 * Get map of books and their URLs to word count.
	 * @return a map of books and their URLs
	 */
	static Map<String, URL> getBookList() {
		// create book list with Jane Austen novels on Project Gutenberg
		Map<String, URL> books = new HashMap<>();
		try {
			books.put("Pride and Prejudice", 
					  new URL("https://www.gutenberg.org/files/1342/1342-0.txt"));
			books.put("Emma", 
					  new URL("https://www.gutenberg.org/files/158/158-0.txt"));
			books.put("Persuation", 
					  new URL("http://www.gutenberg.org/cache/epub/105/pg105.txt"));
			books.put("Sense and Sensibility", 
					  new URL("http://www.gutenberg.org/cache/epub/161/pg161.txt"));
		} catch (Exception e) {
			System.out.println(e);
		}
		return books;
	}
	
	/**
	 * Get map of books and their expected word counts.
	 * @return a map of books and their expected word counts.
	 */
	static Map<String, Integer> getExpectedWordCounts() {
		// create book list with Jane Austin novels on Project Gutenberg
		Map<String,Integer> counts = new HashMap<>();
		counts.put("Pride and Prejudice", 124592);
		counts.put("Emma", 160458);
		counts.put("Persuation",  86307);
		counts.put("Sense and Sensibility",  121590);
		return counts;
	}

	/**
	 * Count the number of words in a list of books, given their URLs.
	 * 
	 * @param args unused
	 */
	public static void main(String[] args) {
		System.out.println("Start\n");
		
		// get a cached thread pools service
		ExecutorService executor= Executors.newCachedThreadPool();

		// get the map of books and prepare a map of lengths
		Map<String,URL> books = getBookList();

		// submit a tasks for each book to count its words
		Map<String,Future<Integer>> wordCounts = 
				new HashMap<String, Future<Integer>>(books.size());
		for (Map.Entry<String,URL> entry : books.entrySet()) {
			wordCounts.put(entry.getKey(), 
						   executor.submit(new WordCounter(entry.getValue()))); 
			// entry.getValue will get url, so WordCounter constructor is with url
		}

		// wait for the tasks to complete
		try {
			System.out.print("Counting words ");
			executor.shutdown();

			// show progress bar while waiting
			while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				System.out.print(".");
			}
			System.out.println();
		} catch (InterruptedException e) {
			System.out.println();
			System.out.println(e);
		}
		System.out.println();

		Map<String,Integer> expectedCounts = getExpectedWordCounts();
		
		// report word counts for the books
		int totalCount = 0;
		int totalExpected = 0;
		for (Map.Entry<String, Future<Integer>> entry : wordCounts.entrySet()) {
			try {
				String book = entry.getKey();
				int count = entry.getValue().get();
				int expected = expectedCounts.get(book);
				totalCount += count;
				totalExpected += expected;
				// report word count for this book
				System.out.printf("%s: %d words (expected %d)\n", entry.getKey(), count, expected);
			} catch (InterruptedException | ExecutionException e) {
				// report error for this book
				System.out.printf("%s: %s", entry.getKey(), e);
			}
		}
		System.out.printf("\nTotal: %d words (expected %d)\n", totalCount, totalExpected);
		
		System.out.println("\nEnd");
	}

}


