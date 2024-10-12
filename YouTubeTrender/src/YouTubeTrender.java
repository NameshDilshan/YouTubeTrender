import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 *
 */
public class YouTubeTrender {

    public static void test1() throws FileNotFoundException {

        System.out.println("Performing Test 1");
        String filename = "data/youtubedata_malformed.json";
        int expectedSize = 50;

        System.out.println("Testing the file: " + filename);
        System.out.println("Expecting size of: " + expectedSize);

        // Read data
        JsonReader jsonReader = Json.createReader(new FileInputStream(filename));
        JsonObject jobj = jsonReader.readObject();

        // read the values of the item field
        JsonArray items = jobj.getJsonArray("items");

        System.out.println("Size of input: " + items.size());
        System.out.println("Sucess: " + (expectedSize == items.size()));

    }

    public static void test2() {

        System.out.println("Performing Test 2");
        String filename = "data/youtubedata_15_50.json";
        int expectedSize = 50;

        System.out.println("Testing the file: " + filename);
        System.out.println("Expecting size of: " + expectedSize);

        //YouTubeDataParser parser = new YouTubeDataParser();

        //List<model.YouTubeVideo> list = parser.parse(filename);

        //System.out.println("Found size: " + list.size());

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("YouTube Trender Application");

//        test1();
        test2();

    }

}
