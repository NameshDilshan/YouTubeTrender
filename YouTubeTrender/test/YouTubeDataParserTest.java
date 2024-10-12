import model.YouTubeVideo;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class YouTubeDataParserTest {

    @Test
    public void testValidFileParsing() throws Exception {
        YouTubeDataParser parser = new YouTubeDataParser();
        List<YouTubeVideo> videos = parser.parse("data/youtubedata_1_50.json");
        assertNotNull(videos);
        assertEquals(50, videos.size());  // Assuming the file has 50 videos
    }

    @Test
    public void testVideoAttributes() throws Exception {
        YouTubeDataParser parser = new YouTubeDataParser();
        List<YouTubeVideo> videos = parser.parse("data/youtubedata_1_50.json");
        YouTubeVideo firstVideo = videos.get(0);

        // Test for basic attributes
        assertEquals("My Movie", firstVideo.getChannel());
        assertEquals(10292900, firstVideo.getViewCount());
        assertEquals(508662, firstVideo.getLikeCount());  // New test for like count
        assertEquals(4875, firstVideo.getCommentCount()); // New test for comment count
    }

    @Test
    public void testFileNotFound() {
        YouTubeDataParser parser = new YouTubeDataParser();
        assertThrows(FileNotFoundException.class, () -> parser.parse("data/invalidfile.json"));
    }

    @Test
    public void testParsingSingleItemFile() throws Exception {
        YouTubeDataParser parser = new YouTubeDataParser();
        List<YouTubeVideo> videos = parser.parse("data/youtubedata_singleitem.json");
        assertNotNull(videos);
        assertEquals(1, videos.size());

        YouTubeVideo singleVideo = videos.get(0);
        assertEquals("This should have a really useful title", singleVideo.getTitle());
    }

    @Test
    public void testMalformedJsonFile() {
        YouTubeDataParser parser = new YouTubeDataParser();
        // Expecting a YouTubeDataParserException for malformed JSON
        assertThrows(YouTubeDataParserException.class, () -> parser.parse("data/youtubedata_malformed.json"));
    }
}
