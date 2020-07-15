package cl.gesvita.ws.actualizarproyectoprioridad;

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

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.regex.PatternSyntaxException;



/**
 * Servlet implementation class ActualizarProyectoPrioridad
 */
@WebServlet("/WSActualizarProyectoPrioridad/ActualizarProyectoPrioridad")
public class ActualizarProyectoPrioridad extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ActualizarProyectoPrioridad.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ActualizarProyectoPrioridad() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        CallableStatement stmt = null;
        String contenidoArray="";
        Gson gson = new Gson();
        Map<String, Object> dataRetornar = new HashMap<String, Object>();
        Clob clob = null;

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
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;

        Map<String, String> m = new HashMap<String, String>();

        try {
            StringBuilder sb = new StringBuilder();
            String s;

            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            //obtener json a array
            requestS = sb.toString();
            if (requestS.length() > 0)
            {
                requestS = requestS.substring(0, sb.length() - 1);
                requestS = requestS.substring(1);
            }
            String[] splitStr = null;

            int iTot = 0;
            
            if ((requestS.contains("[]")) || (requestS.contains("[\"\"]"))) {
            int ubiArr1 = requestS.indexOf("[");
            int ubiArr2 = requestS.indexOf("]");
            ubiArr2++;
            contenidoArray = requestS.substring(ubiArr1,ubiArr2);
            requestS = requestS.substring(0,ubiArr1);
          }
          else
            contenidoArray=" ";
          //logger.info(contenidoArray);
          requestS = requestS.replace("\"", "");
            
            if (requestS.length() > 0)
            {
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

          // BYSECURITY logger.info("JSON cargado");
        }
        catch (NullPointerException e1)
        {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
        }
        catch (IllegalArgumentException e2)
        {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        }
        catch (IndexOutOfBoundsException e3)
        {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. "+e3.getMessage());
        }
        catch (Exception e4)
        {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_SETUP_PRIORIDAD(?,?,?,?)}");
                        clob = conn.createClob();
                        stmt.setFloat(1, Float.parseFloat(m.get("in_id_proyecto")));
                        stmt.setFloat(2, Float.parseFloat(m.get("in_id_prioridad")));
                        stmt.registerOutParameter(3, Types.NUMERIC);
                        stmt.registerOutParameter(4, Types.VARCHAR);
                        stmt.execute();
                        dataRetornar.put("out_codigo", stmt.getInt(3));
                        dataRetornar.put("out_mensaje", stmt.getString(4).toString());
                String json = gson.toJson(dataRetornar);
                response.setHeader("access-control-allow-origin", "*");
                out.print(json);

        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
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
