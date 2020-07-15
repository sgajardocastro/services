package cl.gesvita.ws.insertarcatatipoxemp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

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

/**
 * Servlet implementation class InsertarCATATipoxemp
 */
@WebServlet("/WSInsertarCATATipoxemp/InsertarCATATipoxemp")
public class InsertarCATATipoxemp extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(InsertarCATATipoxemp.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarCATATipoxemp() {
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
        // Gson gson = new Gson();
        Map<String, Object> dataRetornar = new HashMap<>();
        Clob clob = null;

        response.setContentType("application/json");
        String requestS;

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

        Map<String, String> m = new HashMap<>();

        try {
            StringBuilder sb = new StringBuilder();
            String s;

            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            //obtener json a array
            requestS = sb.toString();
            if (requestS.length() > 0) {
                requestS = requestS.substring(0, sb.length() - 1);
                requestS = requestS.substring(1);
            }
            String[] splitStr;

            int iTot = 0;
            requestS = requestS.replace("\"", "");
            requestS = requestS.replace("\'", "");
            if (requestS.length() > 0) {
                if (requestS.contains(",")) {
                    splitStr = requestS.split(",");
                    for (String val : splitStr) {
                        String[] srSpl = val.split(":");
                        m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                    }

                } else {
                    String[] srSpl = requestS.split(":");
                    m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                }
            }

            // BYSECURITY logger.info("JSON cargado");
        } catch (NullPointerException e1) {
            logger.error("Ha ocurrido un error de tipo NullPointer. " + e1.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "NullPointerException");
            sendData(response, out , dataRetornar);
            return;
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "IllegalArgumentException");
            sendData(response, out , dataRetornar);
            return;
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. " + e3.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "IndexOutOfBoundsException");
            sendData(response, out , dataRetornar);
            return;
        } catch (Exception e4) {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Exception");
            sendData(response, out , dataRetornar);
            return;
        }
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.CATALOGO_TIPOXEMP(?,to_date(?, \'DD/MM/YYYY\'),?,?)}");
            clob = conn.createClob();
            stmt.setFloat(1, Float.parseFloat(m.get("in_id_empresa")));
            stmt.setString(2, m.get("in_fecha_vigenciahasta"));
            stmt.registerOutParameter(3, Types.NUMERIC);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            dataRetornar.put("out_codigo", stmt.getInt(3));
            dataRetornar.put("out_mensaje", stmt.getString(4));
            sendData(response, out , dataRetornar);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Exception");
            sendData(response, out , dataRetornar);
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
    
    private void sendData(HttpServletResponse response, PrintWriter out , Map<String, Object> dataRetornar) {
        Gson gson = new Gson();
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }
}
