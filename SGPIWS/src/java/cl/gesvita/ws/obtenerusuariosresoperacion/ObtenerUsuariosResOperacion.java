package cl.gesvita.ws.obtenerusuariosresoperacion;

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
 * Servlet implementation class ObtenerUsuariosResOperacion
 */
@WebServlet("/WSObtenerUsuariosResOperacion/ObtenerUsuariosResOperacion")
public class ObtenerUsuariosResOperacion extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerUsuariosResOperacion.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerUsuariosResOperacion() {
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
            String[] camposytipos = {"ID_EMPRESA","I","CODI_GRUPO","S","ID_GRUPO","I","DESCRIPCION_GRUPO","S",
                "KPI_CANTIDAD_PROYECTOS","I","SLA_TIEMPO_RESPUESTA","I","DESC_SLA_TIEMPO_RESPUESTA","S",
                "ID_USUARIO","I","NOMBRE","S","CODI_USUARIO","S","ID_AREA","I","NOMBRE_AREA","S","ID_ROL","I",
                "NOMBRE_ROL","S","ID_PERFIL","I","NOMBRE_PERFIL","S","CANTIDAD_SOLICITUDES","I",
                "PTJE_AVANCE_ESTADO","I","KPI_CAPACIDAD_ESTADO","I","SUM_PORCENTAJE_ASIGNACION","I"};
            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_SEGURIDAD_USERSGRUPOOPE" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_SEGURIDAD_USERSGRUPOOPE WHERE "
//                    + "DECODE('" + m.get("id_empresa") + "', 'null', '*', ID_EMPRESA) = DECODE('" + m.get("id_empresa") + "', 'null', '*', " + m.get("id_empresa") + ")"
//                    + " AND DECODE('" + m.get("codi_grupo") + "', 'null', '*', CODI_GRUPO) = DECODE('" + m.get("codi_grupo") + "', 'null', '*', '" + m.get("codi_grupo") + "')"
//                    + " AND DECODE('" + m.get("id_grupo") + "', 'null', '*', ID_GRUPO) = DECODE('" + m.get("id_grupo") + "', 'null', '*', " + m.get("id_grupo") + ")"
//                    + " AND DECODE('" + m.get("descripcion_grupo") + "', 'null', '*', DESCRIPCION_GRUPO) = DECODE('" + m.get("descripcion_grupo") + "', 'null', '*', '" + m.get("descripcion_grupo") + "')"
//                    + " AND DECODE('" + m.get("kpi_cantidad_proyectos") + "', 'null', '*', KPI_CANTIDAD_PROYECTOS) = DECODE('" + m.get("kpi_cantidad_proyectos") + "', 'null', '*', " + m.get("kpi_cantidad_proyectos") + ")"
//                    + " AND DECODE('" + m.get("sla_tiempo_respuesta") + "', 'null', '*', SLA_TIEMPO_RESPUESTA) = DECODE('" + m.get("sla_tiempo_respuesta") + "', 'null', '*', " + m.get("sla_tiempo_respuesta") + ")"
//                    + " AND DECODE('" + m.get("desc_sla_tiempo_respuesta") + "', 'null', '*', DESC_SLA_TIEMPO_RESPUESTA) = DECODE('" + m.get("desc_sla_tiempo_respuesta") + "', 'null', '*', '" + m.get("desc_sla_tiempo_respuesta") + "')"
//                    + " AND DECODE('" + m.get("id_usuario") + "', 'null', '*', ID_USUARIO) = DECODE('" + m.get("id_usuario") + "', 'null', '*', " + m.get("id_usuario") + ")"
//                    + " AND DECODE('" + m.get("nombre") + "', 'null', '*', NOMBRE) = DECODE('" + m.get("nombre") + "', 'null', '*', '" + m.get("nombre") + "')"
//                    + " AND DECODE('" + m.get("codi_usuario") + "', 'null', '*', CODI_USUARIO) = DECODE('" + m.get("codi_usuario") + "', 'null', '*', '" + m.get("codi_usuario") + "')"
//                    + " AND DECODE('" + m.get("id_area") + "', 'null', '*', ID_AREA) = DECODE('" + m.get("id_area") + "', 'null', '*', " + m.get("id_area") + ")"
//                    + " AND DECODE('" + m.get("nombre_area") + "', 'null', '*', NOMBRE_AREA) = DECODE('" + m.get("nombre_area") + "', 'null', '*', '" + m.get("nombre_area") + "')"
//                    + " AND DECODE('" + m.get("id_rol") + "', 'null', '*', ID_ROL) = DECODE('" + m.get("id_rol") + "', 'null', '*', " + m.get("id_rol") + ")"
//                    + " AND DECODE('" + m.get("nombre_rol") + "', 'null', '*', NOMBRE_ROL) = DECODE('" + m.get("nombre_rol") + "', 'null', '*', '" + m.get("nombre_rol") + "')"
//                    + " AND DECODE('" + m.get("id_perfil") + "', 'null', '*', ID_PERFIL) = DECODE('" + m.get("id_perfil") + "', 'null', '*', " + m.get("id_perfil") + ")"
//                    + " AND DECODE('" + m.get("nombre_perfil") + "', 'null', '*', NOMBRE_PERFIL) = DECODE('" + m.get("nombre_perfil") + "', 'null', '*', '" + m.get("nombre_perfil") + "')"
//                    + " AND DECODE('" + m.get("cantidad_solicitudes") + "', 'null', '*', CANTIDAD_SOLICITUDES) = DECODE('" + m.get("cantidad_solicitudes") + "', 'null', '*', " + m.get("cantidad_solicitudes") + ")"
//                    + " AND DECODE('" + m.get("ptje_avance_estado") + "', 'null', '*', PTJE_AVANCE_ESTADO) = DECODE('" + m.get("ptje_avance_estado") + "', 'null', '*', " + m.get("ptje_avance_estado") + ")"
//                    + " AND DECODE('" + m.get("kpi_capacidad_estado") + "', 'null', '*', KPI_CAPACIDAD_ESTADO) = DECODE('" + m.get("kpi_capacidad_estado") + "', 'null', '*', " + m.get("kpi_capacidad_estado") + ")"
//                    + " AND DECODE('" + m.get("sum_porcentaje_asignacion") + "', 'null', '*', SUM_PORCENTAJE_ASIGNACION) = DECODE('" + m.get("sum_porcentaje_asignacion") + "', 'null', '*', " + m.get("sum_porcentaje_asignacion") + ")");
//
//            dataGrupal = new HashMap<String, Object>();
//            while (rs.next())
//            {
//                totalRegistro++;
//                dataIndividual = new HashMap<String, Object>();
//               dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
//               dataIndividual.put("CODI_GRUPO", rs.getString("CODI_GRUPO"));
//               dataIndividual.put("ID_GRUPO", rs.getString("ID_GRUPO"));
//               dataIndividual.put("DESCRIPCION_GRUPO", rs.getString("DESCRIPCION_GRUPO"));
//               dataIndividual.put("KPI_CANTIDAD_PROYECTOS", rs.getString("KPI_CANTIDAD_PROYECTOS"));
//               dataIndividual.put("SLA_TIEMPO_RESPUESTA", rs.getString("SLA_TIEMPO_RESPUESTA"));
//               dataIndividual.put("DESC_SLA_TIEMPO_RESPUESTA", rs.getString("DESC_SLA_TIEMPO_RESPUESTA"));
//               dataIndividual.put("ID_USUARIO", rs.getString("ID_USUARIO"));
//               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
//               dataIndividual.put("CODI_USUARIO", rs.getString("CODI_USUARIO"));
//               dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
//               dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
//               dataIndividual.put("ID_ROL", rs.getString("ID_ROL"));
//               dataIndividual.put("NOMBRE_ROL", rs.getString("NOMBRE_ROL"));
//               dataIndividual.put("ID_PERFIL", rs.getString("ID_PERFIL"));
//               dataIndividual.put("NOMBRE_PERFIL", rs.getString("NOMBRE_PERFIL"));
//               dataIndividual.put("CANTIDAD_SOLICITUDES", rs.getString("CANTIDAD_SOLICITUDES"));
//               dataIndividual.put("PTJE_AVANCE_ESTADO", rs.getString("PTJE_AVANCE_ESTADO"));
//               dataIndividual.put("KPI_CAPACIDAD_ESTADO", rs.getString("KPI_CAPACIDAD_ESTADO"));
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
