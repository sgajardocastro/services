package cl.gesvita.ws.actualizarproyectoreplanifica;

import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import oracle.jdbc.OracleTypes;
import oracle.sql.CLOB;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;

/**
 * Servlet implementation class ActualizarProyectoReplanifica
 */
@WebServlet("/WSActualizarProyectoReplanifica/ActualizarProyectoReplanifica1")
public class ActualizarProyectoReplanifica1 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ActualizarProyectoReplanifica1.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarProyectoReplanifica1() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonObj = null;
        
        DataSource datasource;
        Connection conn = null;
        CallableStatement stmt = null;
        try {
            ObtenerLib.setLogParam(this.getClass());
            datasource = ObtenerLib.getDataSource(logger);
            jsonObj = ObtenerLib.readInput(logger,request);
            // logger.info("JSON cargado");
            Map<String,List<String>> p = ObtenerLib.ExtractParams(logger,jsonObj);
        } catch (GesvitaException ex ) {
            String msg = ex.getMessage();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"");
            sb.append(msg);
            sb.append("\"}");
            out.print( sb.toString());
            return;
        }
        Gson gson = new Gson();
        Map<String, Object> dataRetornar = new HashMap<String, Object>();

        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_REPLANIFICA(?,to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?)}");
            stmt.setFloat(1, Float.parseFloat(jsonObj.get("in_id_proyecto").toString().replace("\"", "")));
            stmt.setString(2, jsonObj.get("in_fecha_plan_ini_replan").toString().replace("\"", ""));
            stmt.setFloat(3, Float.parseFloat(jsonObj.get("in_id_usuario_quemodifica").toString().replace("\"", "")));
            stmt.setString(4, jsonObj.get("in_flag_replan_detenido").toString().replace("\"", ""));
            stmt.setString(5, jsonObj.get("in_flag_replan_ejecucion").toString().replace("\"", ""));
            stmt.registerOutParameter(6, Types.NUMERIC);
            stmt.registerOutParameter(7, Types.VARCHAR);
            stmt.execute();
            dataRetornar.put("out_codigo", stmt.getInt(6));
            dataRetornar.put("out_mensaje", stmt.getString(7).toString());
            String json = gson.toJson(dataRetornar);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (datasource != null) {
                try {
                    datasource.close();
                }
                catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
    }

}
