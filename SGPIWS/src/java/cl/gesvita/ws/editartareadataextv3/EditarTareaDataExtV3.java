package cl.gesvita.ws.editartareadataextv3;

import cl.gesvita.ws.obtener.exception.GesvitaException;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;

/**
 * Servlet implementation class EditarTareaDataExtV3
 */
@WebServlet("/WSEditarTareaDataExtV3/EditarTareaDataExtV3")
public class EditarTareaDataExtV3 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(EditarTareaDataExtV3.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditarTareaDataExtV3() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        CallableStatement stmt = null;
        String contenidoArray = "";
        Gson gson = new Gson();
        Map<String, Object> dataRetornar = new HashMap<String, Object>();
        Clob clob = null;
        String sEstado1 = "";
        response.setContentType("application/json");
        String requestS = "";

        PrintWriter out = response.getWriter();
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Properties prop = new Properties();
        Properties propsLog4 = new Properties();
        PoolProperties p = new PoolProperties();

        InputStream inputStreamLog4 = this.getClass().getResourceAsStream("log4j.properties");
        propsLog4.load(inputStreamLog4);
        PropertyConfigurator.configure(propsLog4);
        String archivoPro = System.getProperty("catalina.base") + "/webapps/propiedades/db.properties";

        prop.load(new FileInputStream(archivoPro));

        p.setUrl(prop.getProperty("urlConexion"));
        p.setDriverClassName(prop.getProperty("driverClass"));
        p.setUsername(prop.getProperty("userName"));
        p.setPassword(prop.getProperty("password"));
        p.setJmxEnabled(Boolean.parseBoolean(prop.getProperty("jmxEnabled")));
        p.setTestWhileIdle(Boolean.parseBoolean(prop.getProperty("testWhileIdle")));
        p.setTestOnBorrow(Boolean.parseBoolean(prop.getProperty("testOnBorrow")));
        p.setValidationQuery(prop.getProperty("validationQuery"));
        p.setTestOnReturn(Boolean.parseBoolean(prop.getProperty("testOnReturn")));
        p.setValidationInterval(Long.parseLong(prop.getProperty("validationInterval")));
        p.setTimeBetweenEvictionRunsMillis(Integer.parseInt(prop.getProperty("timeBetweenEvictionRunsMillis")));
        p.setMaxActive(Integer.parseInt(prop.getProperty("maxActive")));
        p.setInitialSize(Integer.parseInt(prop.getProperty("initialSize")));
        p.setMaxWait(Integer.parseInt(prop.getProperty("maxWait")));
        p.setRemoveAbandonedTimeout(Integer.parseInt(prop.getProperty("removeAbandonedTimeout")));
        p.setMinEvictableIdleTimeMillis(Integer.parseInt(prop.getProperty("minEvictableIdleTimeMillis")));
        p.setMinIdle(Integer.parseInt(prop.getProperty("minIdle")));
        p.setLogAbandoned(Boolean.parseBoolean(prop.getProperty("logAbandoned")));
        p.setRemoveAbandoned(Boolean.parseBoolean(prop.getProperty("removeAbandoned")));
        p.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;
        JsonObject jsonObj = null;

        // Map<String, String> m = new HashMap<String, String>();
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            // BYSECURITY logger.info("Data Extendida Antes:");
            // BYSECURITY logger.info(sb);
            requestS = sb.toString();
            // BYSECURITY logger.info("Data Extendida con toString:");
            // BYSECURITY logger.info(requestS);

            /*
			  if (requestS.length() > 0)
			  {
				requestS = requestS.substring(0, sb.length() - 1);
				requestS = requestS.substring(1);
			  }
			  String[] splitStr = null;
			  
			  int iTot = 0;
			// BYSECURITY logger.info("Data Extendida antes replace:");
			// BYSECURITY logger.info(requestS);
             */
            JsonParser parser = new JsonParser();
            jsonObj = (JsonObject) parser.parse(requestS);

            /*
			  
			  requestS = requestS.replace("\\", "");
			  int buscarEstado1 = requestS.toLowerCase().indexOf("in_data_extendida");
			 // int posicionEstado1 = requestS.indexOf("[", buscarEstado1);
			  int posicionEstado1 = requestS.indexOf("{", buscarEstado1);
			  sEstado1 = requestS.substring(posicionEstado1, requestS.length() - 1 );
			// BYSECURITY logger.info("Data Extendida Despues:");
			// BYSECURITY logger.info(sEstado1);
			  
                          
                          
			  requestS = requestS.replace("\"", "");
			  if (requestS.length() > 0) {
				if (requestS.contains(","))
				{
				  splitStr = requestS.split(",");
				  for (String val : splitStr)
				  {
					String[] srSpl = val.split(":");
					if (srSpl.length == 2) {
					  m.put(srSpl[0].trim().toLowerCase().toString(), srSpl[1].trim().toString());
					}
				  }
				}
				else
				{
				  String[] srSpl = requestS.split(":");
				  m.put(srSpl[0].trim().toLowerCase().toString(), srSpl[1].trim().toString());
				}
			  }

             */
            // BYSECURITY logger.info("JSON cargado");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. " + e3.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Indice invalido\"}");
            return;
        } catch (MalformedJsonException ex) {
            logger.error("Ha ocurrido un error de tipo MalformedJsonException. " + ex.getMessage());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Json mal formado\"}");
            return;
        } catch (Exception e4) {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_TAREA_DATAEXT(?,?,?,?,?)}");
            clob = conn.createClob();
            stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_tarea")));
            stmt.setFloat(2, Float.parseFloat(readFieldString(jsonObj,"in_id_usuario_quemodifica")));
            clob.setString(1, readFieldString(jsonObj,"in_data_extendida"));
            stmt.setClob(3, clob);
            stmt.registerOutParameter(4, Types.NUMERIC);
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.execute();
            dataRetornar.put("out_codigo", stmt.getInt(4));
            dataRetornar.put("out_mensaje", stmt.getString(5));
            dataRetornar.put("resultadoEjecucion", "OK");
            sendData(response, out, dataRetornar);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            out.print("{\"resultadoEjecucion\":\"NOK\",\"mensaje\":\"Ocurrio un error\",\"mensajeTec\":\"Error\"}");
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
    }
    private void sendData(HttpServletResponse response, PrintWriter out, Map<String, Object> dataRetornar) {
        Gson gson = new Gson();
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
        String fieldlow = field.toLowerCase();
        if (!jsonObj.has(fieldlow))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
        JsonElement value = jsonObj.get(fieldlow);
        String salida = value.toString();
        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
        }
        return salida;
    }
}
