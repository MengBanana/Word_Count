# Word_Count

This program counts the words in a collection of books available 
online in plain text format from Project Gutenberg. The program 
uses an ExecutorService to run the counting tasks concurrently.

The problem is to provide a WordCounter class that implements
Callable<Integer>. The call() method counts the number of words
in its input and returns the count as an Integer.The WordCounter 
is used in main() to submit tasks to an ExecutorService.
  
The WordCounter is constructed with a URL that represents the URL 
of a book. The URL.openStream() method returns an InputStream that 
can be used to read lines through a BufferedReader. The reader must 
be closed after the last line is read.
 
Use a StringTokenizer on each input line to count the number of
words, and keep a running total for all the lines.
