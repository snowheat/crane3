package crane; /**
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

        port(1111);

        Sql2o mSql2o = new Sql2o("jdbc:sqlite:src/crane.sqlite", null, null);
        Connection mCon = mSql2o.open();

        staticFiles.location("/public");

        Gson gson                       = new Gson();

        // TODO Calculate something before
        // FIXME Bug when user input ..

        get("/", (rq, rs) -> {

            Map<String, Object> model     = new HashMap<>();

            try{
                String sql = "SELECT * FROM cross_section";
                List<CrossSection> crossSections = mCon.createQuery(sql).executeAndFetch(CrossSection.class);
                model.put("crossSections",crossSections);
            }catch(Exception e){
                e.printStackTrace();
            }


            try{
                String sql = "SELECT * FROM material";
                List<Material> materials = mCon.createQuery(sql).executeAndFetch(Material.class);
                model.put("materials",materials);
            }catch(Exception e){
                e.printStackTrace();
            }


            model.put("message", new BigDecimal(322.32));


            return new ModelAndView(model, "index.mustache"); // hello.mustache file is in resources/templates directory


        }, new MustacheTemplateEngine());


        post("/getresult", (rq, rs) -> {

            System.out.println(rq.queryParams("l_tx")+ " , , , " + rq.queryParams("l_ty"));
            Input input = new Input();
            input.setBeamLength(rq.queryParams("beam_length"));
            input.setForcePosition(rq.queryParams("force_position"));
            input.setMass(rq.queryParams("mass"));
            input.setMaterialID(rq.queryParams("material_id"));
            input.setCrossSectionID(rq.queryParams("cross_section_id"));
            input.setSimulationID(rq.queryParams("simulation_id"));
            input.setLTx(rq.queryParams("l_tx"));
            input.setLTy(rq.queryParams("l_ty"));

            LoadAnalyzer loadAnalyzer = new LoadAnalyzer(input);

            return loadAnalyzer.getResultObject();

        },gson::toJson);

        get("/get", (rq, rs) -> {


            Input input = new Input();
            input.setBeamLength("6");
            input.setForcePosition("4.21");
            input.setMass("2");
            input.setMaterialID("1");
            input.setCrossSectionID("3");
            input.setSimulationID("1");

            LoadAnalyzer loadAnalyzer = new LoadAnalyzer(input);



            return loadAnalyzer.getResultObject();

        },gson::toJson);
    }
}


