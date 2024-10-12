import model.YouTubeVideo;

import java.util.*;

public class TrendingTopicAnalyzer {
    private Map<String, Integer> wordCounts;
    private Map<String, List<YouTubeVideo>> wordVideos;

    public TrendingTopicAnalyzer(List<YouTubeVideo> videos) {
        wordCounts = new HashMap<>();
        wordVideos = new HashMap<>();
        indexVideos(videos);
    }

    // Index words in video titles and descriptions
    private void indexVideos(List<YouTubeVideo> videos) {
        for (YouTubeVideo video : videos) {
            // Tokenize title and description
            String[] words = (video.getTitle() + " " + video.getDescription()).split("\\s+");
            for (String word : words) {
                word = word.trim(); // Remove leading/trailing spaces
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);

                if (!wordVideos.containsKey(word)) {
                    wordVideos.put(word, new ArrayList<>());
                }
                wordVideos.get(word).add(video);
            }
        }
    }

    // Get the count for a specific word
    public int getWordCount(String word) {
        return wordCounts.getOrDefault(word, 0);
    }

    // Get the list of videos associated with a specific word
    public List<YouTubeVideo> getVideosForWord(String word) {
        return wordVideos.getOrDefault(word, Collections.emptyList());
    }

    // Get the word with the highest count
    public String getMostUsedWord() {
        return wordCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Get a sorted list of words by count
    public List<String> getSortedWordsByCount() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(wordCounts.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<String> sortedWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedWords.add(entry.getKey() + ": " + entry.getValue());
        }
        return sortedWords;
    }
}
