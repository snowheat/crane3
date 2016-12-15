/**
 * Created by pusdalisbang on 12/15/16.
 */
import static spark.Spark.*;
import com.google.gson.Gson;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Main {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}


