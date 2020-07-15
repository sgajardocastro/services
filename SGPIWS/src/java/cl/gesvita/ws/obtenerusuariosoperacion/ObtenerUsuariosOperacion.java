package cl.gesvita.ws.obtenerusuariosoperacion;

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
 * Servlet implementation class ObtenerUsuariosOperacion
 */
@WebServlet("/WSObtenerUsuariosOperacion/ObtenerUsuariosOperacion")
public class ObtenerUsuariosOperacion extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerUsuariosOperacion.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerUsuariosOperacion() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
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
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);

        Map<String, Object> dataIndividual = null;
        Map<String, Object> dataGrupal = null;
        Connection connection = null;
        Statement  stmt = null;
        ResultSet rs = null;
        Gson gson = new Gson();
        int totalRegistro=0;

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
        requestS = requestS.replace("\"", "");
        requestS = requestS.replace("\'", "");
        if (requestS.length() > 0)
        {
            if (requestS.contains(","))
            {
                splitStr = requestS.split(",");
                for (String val : splitStr)
                {
                    String[] srSpl = val.split(":");
                    m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
                }
            }
            else
            {
                String[] srSpl = requestS.split(":");
                m.put(srSpl[0].trim().toString(), srSpl[1].trim().toString());
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
            connection = datasource.getConnection();
            String[] camposytipos = {"ID_EMPRESA","I","ID_TIPO","I","DESCRIPCION_TIPO","S",
                "ID_PROYECTO","I","CODI_GRUPO","S","ID_USUARIO","I","NOMBRE","S","NOMBRE_ROL","S",
                "CHECKASIGNADO","S","PROM_AVANCE_REAL","I","SLA_TIEMPO_RESPUESTA_TAREA_POND","I",
                "SUM_PORCENTAJE_ASIGNACION","I"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_USUARIOS_OPERACIONPROY" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_USUARIOS_OPERACIONPROY WHERE "
//                    + "DECODE('" + m.get("id_empresa") + "', 'null', '*', ID_EMPRESA) = DECODE('" + m.get("id_empresa") + "', 'null', '*', " + m.get("id_empresa") + ")"
//                    + " AND DECODE('" + m.get("id_tipo") + "', 'null', '*', ID_TIPO) = DECODE('" + m.get("id_tipo") + "', 'null', '*', " + m.get("id_tipo") + ")"
//                    + " AND DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', DESCRIPCION_TIPO) = DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', '" + m.get("descripcion_tipo") + "')"
//                    + " AND DECODE('" + m.get("id_proyecto") + "', 'null', '*', ID_PROYECTO) = DECODE('" + m.get("id_proyecto") + "', 'null', '*', " + m.get("id_proyecto") + ")"
//                    + " AND DECODE('" + m.get("codi_grupo") + "', 'null', '*', CODI_GRUPO) = DECODE('" + m.get("codi_grupo") + "', 'null', '*', '" + m.get("codi_grupo") + "')"
//                    + " AND DECODE('" + m.get("id_usuario") + "', 'null', '*', ID_USUARIO) = DECODE('" + m.get("id_usuario") + "', 'null', '*', " + m.get("id_usuario") + ")"
//                    + " AND DECODE('" + m.get("nombre") + "', 'null', '*', NOMBRE) = DECODE('" + m.get("nombre") + "', 'null', '*', '" + m.get("nombre") + "')"
//                    + " AND DECODE('" + m.get("nombre_rol") + "', 'null', '*', NOMBRE_ROL) = DECODE('" + m.get("nombre_rol") + "', 'null', '*', '" + m.get("nombre_rol") + "')"
//                    + " AND DECODE('" + m.get("checkasignado") + "', 'null', '*', CHECKASIGNADO) = DECODE('" + m.get("checkasignado") + "', 'null', '*', '" + m.get("checkasignado") + "')"
//                    + " AND DECODE('" + m.get("prom_avance_real") + "', 'null', '*', PROM_AVANCE_REAL) = DECODE('" + m.get("prom_avance_real") + "', 'null', '*', " + m.get("prom_avance_real") + ")"
//                    + " AND DECODE('" + m.get("sla_tiempo_respuesta_tarea_pond") + "', 'null', '*', SLA_TIEMPO_RESPUESTA_TAREA_POND) = DECODE('" + m.get("sla_tiempo_respuesta_tarea_pond") + "', 'null', '*', " + m.get("sla_tiempo_respuesta_tarea_pond") + ")"
//                    + " AND DECODE('" + m.get("sum_porcentaje_asignacion") + "', 'null', '*', SUM_PORCENTAJE_ASIGNACION) = DECODE('" + m.get("sum_porcentaje_asignacion") + "', 'null', '*', " + m.get("sum_porcentaje_asignacion") + ")");
//
//            dataGrupal = new HashMap<String, Object>();
//            while (rs.next())
//            {
//                totalRegistro++;
//                dataIndividual = new HashMap<String, Object>();
//               dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
//               dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
//               dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
//               dataIndividual.put("ID_PROYECTO", rs.getString("ID_PROYECTO"));
//               dataIndividual.put("CODI_GRUPO", rs.getString("CODI_GRUPO"));
//               dataIndividual.put("ID_USUARIO", rs.getString("ID_USUARIO"));
//               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
//               dataIndividual.put("NOMBRE_ROL", rs.getString("NOMBRE_ROL"));
//               dataIndividual.put("CHECKASIGNADO", rs.getString("CHECKASIGNADO"));
//               dataIndividual.put("PROM_AVANCE_REAL", rs.getString("PROM_AVANCE_REAL"));
//               dataIndividual.put("SLA_TIEMPO_RESPUESTA_TAREA_POND", rs.getString("SLA_TIEMPO_RESPUESTA_TAREA_POND"));
//               dataIndividual.put("SUM_PORCENTAJE_ASIGNACION", rs.getString("SUM_PORCENTAJE_ASIGNACION"));
//             dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
//            }
//            String json = gson.toJson(dataGrupal);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. "+e.getMessage());
            //e.printStackTrace();
        } finally {
            if (datasource != null)
            {
                try {
                        datasource.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }
            if (rs != null)
            {
                try {
                    rs.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }

            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
