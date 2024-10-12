import model.YouTubeVideo;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class YouTubeDataParser {

    // Parse the JSON file and return a list of model.YouTubeVideo objects
    public List<YouTubeVideo> parse(String filename) throws YouTubeDataParserException, FileNotFoundException {
        List<YouTubeVideo> videos = new ArrayList<>();

        try (JsonReader reader = Json.createReader(new FileInputStream(filename))) {
            JsonObject jsonObject = reader.readObject();
//            JsonArray items = jsonObject.asJsonArray();
//            if(jsonObject.containsKey("items")){
            JsonArray items = jsonObject.getJsonArray("items");
//            }else{
//                items.add(0, jsonObject);
//            }

            for (JsonObject item : items.getValuesAs(JsonObject.class)) {
                JsonObject snippet = item.getJsonObject("snippet");
                JsonObject statistics = item.getJsonObject("statistics");

                // Extract video information
                String title = snippet.getString("title");
                String channel = snippet.getString("channelTitle");
                String date = snippet.getString("publishedAt");
                String description = snippet.getString("description");
                int viewCount = Integer.parseInt(statistics.getString("viewCount"));

                // New: Extract additional fields
                String videoId = item.getString("id");  // Video ID
                String channelId = snippet.getString("channelId");  // Channel ID
                String thumbnailUrl = snippet.getJsonObject("thumbnails").getJsonObject("medium").getString("url");  // Thumbnail URL
                String categoryId = snippet.getString("categoryId", "");  // Optional field: Category ID
                String liveBroadcastContent = snippet.getString("liveBroadcastContent", "none");  // Optional field: Live broadcast content

                // New: Extract like count and comment count
                int likeCount = statistics.containsKey("likeCount") ? Integer.parseInt(statistics.getString("likeCount")) : 0;
                int commentCount = statistics.containsKey("commentCount") ? Integer.parseInt(statistics.getString("commentCount")) : 0;

                YouTubeVideo video = new YouTubeVideo(channel, date, title, description, viewCount, likeCount, commentCount,
                        videoId, channelId, thumbnailUrl, categoryId, liveBroadcastContent);
                videos.add(video);
            }

        } catch (FileNotFoundException ex) {
            throw ex;  // Propagate FileNotFoundException
        } catch (Exception ex) {
            throw new YouTubeDataParserException("Error parsing the file: " + ex.getMessage());
        }

        return videos;
    }
}
