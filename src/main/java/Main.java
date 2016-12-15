/**
 * Created by pusdalisbang on 12/15/16.
 */
import static spark.Spark.*;
import com.google.gson.Gson;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.math.BigDecimal;
import java.util.*;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {

        get("/", (rq, rs) -> {

            Map<String, Object> model     = new HashMap<>();


            model.put("message", new BigDecimal(322.32));


            return new ModelAndView(model, "index.mustache"); // hello.mustache file is in resources/templates directory


        }, new MustacheTemplateEngine());
    }
}


