package cl.gesvita.ws.insertarempresa;

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
 * Servlet implementation class InsertarEmpresa
 */
@WebServlet("/WSInsertarEmpresa/InsertarEmpresa")
public class InsertarEmpresa extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(InsertarEmpresa.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public InsertarEmpresa() {
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
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_EMPRESA(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
                        clob = conn.createClob();
                        stmt.setString(1, m.get("in_nombre_empresa"));
                        stmt.setFloat(2, Float.parseFloat(m.get("in_id_pais")));
                        stmt.setString(3, m.get("in_rut_empresa"));
                        stmt.setString(4, m.get("in_razon_social"));
                        stmt.setString(5, m.get("in_zona_horaria"));
                        stmt.setFloat(6, Float.parseFloat(m.get("in_id_tipo_cambio")));
                        stmt.setFloat(7, Float.parseFloat(m.get("in_id_representante_legal")));
                        stmt.setString(8, m.get("in_giro"));
                        stmt.setString(9, m.get("in_direccion"));
                        stmt.setString(10, m.get("in_fono_contacto"));
                        stmt.setString(11, m.get("in_email"));
                        stmt.setString(12, m.get("in_latitud"));
                        stmt.setString(13, m.get("in_longitud"));
                        stmt.setFloat(14, Float.parseFloat(m.get("in_id_usuario_creacion")));
                        stmt.setString(15, m.get("in_estado_empresa"));
                        stmt.setString(16, m.get("in_nombre_contacto"));
                        stmt.setString(17, m.get("in_rol_contacto"));
                        stmt.setFloat(18, Float.parseFloat(m.get("in_empresa_padre")));
                        stmt.setString(19, m.get("in_comuna_empresa"));
                        stmt.setString(20, m.get("in_direccion_administrativa"));
                        stmt.setString(21, m.get("in_comuna_administrativa"));
                        stmt.setString(22, m.get("in_observacion_contacto"));
                        stmt.registerOutParameter(23, Types.NUMERIC);
                        stmt.registerOutParameter(24, Types.NUMERIC);
                        stmt.registerOutParameter(25, Types.VARCHAR);
                        stmt.execute();
                        dataRetornar.put("out_id_empresa", stmt.getInt(23));
                        dataRetornar.put("out_codigo", stmt.getInt(24));
                        dataRetornar.put("out_mensaje", stmt.getString(25).toString());
                String json = gson.toJson(dataRetornar);
                response.setHeader("access-control-allow-origin", "*");
                out.print(json);

        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
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

}
