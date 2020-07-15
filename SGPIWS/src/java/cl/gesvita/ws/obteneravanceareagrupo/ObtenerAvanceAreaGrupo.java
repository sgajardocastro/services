package cl.gesvita.ws.obteneravanceareagrupo;

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

import java.text.SimpleDateFormat;

import java.util.ArrayList;
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
 * Servlet implementation class ObtenerAvanceAreaGrupo
 */
@WebServlet("/WSObtenerAvanceAreaGrupo/ObtenerAvanceAreaGrupo")
public class ObtenerAvanceAreaGrupo extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerAvanceAreaGrupo.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerAvanceAreaGrupo() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Gson gson = new Gson();
        int totalRegistro = 0;

        Map<String, String> m = new HashMap<String, String>();
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
            String[] splitStr = null;

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
        } catch (IllegalArgumentException e2) {
            logger.error("Ha ocurrido un error de tipo IllegalArgument. " + e2.getMessage());
        } catch (IndexOutOfBoundsException e3) {
            logger.error("Ha ocurrido un error de tipo IndexOutOfBounds. " + e3.getMessage());
        } catch (Exception e4) {
            logger.error("Ha ocurrido un error. " + e4.getMessage());
        }

        try {
            connection = datasource.getConnection();
            String[] camposytipos = {"ID_EMPRESA","I","ID_PROYECTO","I","NOMBRE","S","FECHA_CREACION","S"
                    ,"ID_TIPO","I","OBSERVACION_ACTUAL","S","ID_PROYECTO_PADRE","I","ID_PROYECTO_TEMPLATE"
                    ,"I","ID_AREA","I","NOMBRE_AREA","S","CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO"
                    ,"S","SLA_PLAZOS","I","KPI_PROYECTO_AVANCE_STATUS","I","AVANCE_PLAN_PORTIEMPO","I"
                    ,"AVANCE_REAL_PROYECTO","I","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S","CODI_GRUPO_SUCURSAL"
                    ,"S","NOMBRE_GRUPO_SUCURSAL","S","ID_ESTADO_PROYECTO","I","NOMBRE_ESTADO_PROYECTO","S"
                    ,"ID_USUARIO_JEFE_PROYECTO","I","NOMBRE_JEFEPROYECTO","S","FECHA_REAL_INIPROY","S"
                    ,"FECHA_REAL_FINPROY","S","FECHA_PLAN_INIPROY","S","FECHA_PLAN_FINPROY","S"
                    ,"ID_AREA_EJECUTOR","I","NOMBRE_AREA_EJECUTOR","S","DATA_HITOS","S","CANTIDADTAREAS"
                    ,"I","CANTIDADUSUARIOSEJEC","I","CANT_KPI_TAREA_AVANCE_PTJE_CUMPLE","I"
                    ,"CANT_KPI_TAREA_AVANCE_PTJE_ENRIESGO","I","CANT_KPI_TAREA_AVANCE_PTJE_NOCUMPLE"
                    ,"I","KPI_AREA_AVANCE_ESTADO","I","AVANCE_REAL_PROM","I"};

            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_PROYECTO_AVANCEAREAGRUPO", m);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            logger.error("SQLException:" , e.fillInStackTrace());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException sqle) {
                logger.error(sqle.getMessage(), sqle.fillInStackTrace());
            }
            try {
                datasource.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e.fillInStackTrace());
            }
        }
    }

}
