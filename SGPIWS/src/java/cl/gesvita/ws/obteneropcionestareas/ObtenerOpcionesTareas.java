package cl.gesvita.ws.obteneropcionestareas;

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
 * Servlet implementation class ObtenerOpcionesTareas
 */
@WebServlet("/WSObtenerOpcionesTareas/ObtenerOpcionesTareas")
public class ObtenerOpcionesTareas extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerOpcionesTareas.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerOpcionesTareas() {
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
        PreparedStatement stmt = null;
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
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            SimpleDateFormat formato_ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
            String[] camposytipos = { "ID_AREA","S", "ID_AREA_CLIENTE","I", "NOMBRE_AREA_CLIENTE","S"};
            
            boolean hay_tarea = m.containsKey("id_tarea");
            int intTare = -1;
            if (hay_tarea) {
                intTare = Integer.parseInt(m.get("id_tarea"));
                // BYSECURITY logger.debug("Tarea = " + intTare + "; (params: 1,3,4,5,6)");
            } else {
                // BYSECURITY logger.debug("Sin id_tarea");
            }
            boolean hay_projecto = m.containsKey("id_proyecto");
            int intProj = -1;
            if (hay_projecto) {
                intProj = Integer.parseInt(m.get("id_proyecto"));
                // BYSECURITY logger.debug("Tarea = " + intProj + "; (params: 1,3,4,5,6)");
            } else {
                // BYSECURITY logger.debug("Sin id_proyecto");
            }
            
            // Construir el SQL
            StringSql.append("SELECT distinct al.* FROM (");
            if (hay_tarea) {
                StringSql.append("SELECT * FROM VW_TAREA_OPCIONES");
                StringSql.append("  WHERE");
                StringSql.append(" ID_TAREA = ?");
                StringSql.append(" union all select distinct decode(ptta.id_tipo_tarea, 80, ptt.id_tarea, ptta.id_tarea)");
                StringSql.append(" id_tarea, tee.NOMBRE_ESTADO nombre_opcion, tee.ID_ESTADO||'-'||decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') id_opcion, tee.ID_ESTADO, trim(tee.NOMBRE_ESTADO||' '||decode(ptta.id_tipo_tarea, 80, '- '||ptt.nombre_tarea, '')) NOMBRE_ACCION_SIGUIENTE, decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') valor_output, tee.HEX_COLOR_FILL, decode(ptta.id_tipo_tarea, 80, ptt.nombre_tarea, '0') nombre_tarea_output, ptta.id_tipo_tarea from tb_tarea_estado tee,");
                StringSql.append("(select * from tb_proyecto_tarea where id_proyecto  = ?");  // Proyecto
                StringSql.append(" and  length(id_tarea_dependencia) != length(replace(id_tarea_dependencia,");
                StringSql.append(" to_char(?), '0'))");
                StringSql.append(" union all select * from tb_proyecto_tarea where id_tarea = ?");
                StringSql.append(" and (id_tarea_dependencia = '0' or id_tipo_tarea = 91)) ptt,(select * from tb_proyecto_tarea where id_tarea = ?");
                StringSql.append(" and id_tipo_tarea != 84) ptta where tee.id_estado = 3 and ptt.id_estado != 3) al, tb_tarea_transicion tt, tb_proyecto_tarea pt where al.id_estado = tt.id_estado_siguiente");
                StringSql.append(" and pt.ID_TAREA = ?");
                StringSql.append(" and pt.id_estado = tt.id_estado_actual and tt.id_tipo_tarea = decode(pt.id_tipo_tarea, tt.id_tipo_tarea, pt.id_tipo_tarea, 0) ORDER BY al.ID_ESTADO");
                stmt = connection.prepareStatement( StringSql.toString() );
                stmt.setInt(1, intTare);
                stmt.setInt(2, intProj);
                stmt.setInt(3, intTare);
                stmt.setInt(4, intTare);
                stmt.setInt(5, intTare);
                stmt.setInt(6, intTare);
            } else {
                StringSql.append("select distinct decode(ptta.id_tipo_tarea, 80, ptt.id_tarea, ptta.id_tarea)");
                StringSql.append(" id_tarea, tee.NOMBRE_ESTADO nombre_opcion, tee.ID_ESTADO||'-'||decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') id_opcion, tee.ID_ESTADO, trim(tee.NOMBRE_ESTADO||' '||decode(ptta.id_tipo_tarea, 80, '- '||ptt.nombre_tarea, '')) NOMBRE_ACCION_SIGUIENTE, decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') valor_output, tee.HEX_COLOR_FILL, decode(ptta.id_tipo_tarea, 80, ptt.nombre_tarea, '0') nombre_tarea_output, ptta.id_tipo_tarea from tb_tarea_estado tee,");
                StringSql.append("(select * from tb_proyecto_tarea where id_proyecto  = ?");  // Proyecto
                StringSql.append(" and  length(id_tarea_dependencia) != length(replace(id_tarea_dependencia,");
                StringSql.append(" to_char(null), '0'))");
                StringSql.append("union all select * from tb_proyecto_tarea where id_tarea = null");
                StringSql.append(" and (id_tarea_dependencia = '0' or id_tipo_tarea = 91)) ptt,(select * from tb_proyecto_tarea where id_tarea = null");
                StringSql.append(" and id_tipo_tarea != 84) ptta where tee.id_estado = 3 and ptt.id_estado != 3) al, tb_tarea_transicion tt, tb_proyecto_tarea pt where al.id_estado = tt.id_estado_siguiente");
                StringSql.append(" and pt.id_estado = tt.id_estado_actual and tt.id_tipo_tarea = decode(pt.id_tipo_tarea, tt.id_tipo_tarea, pt.id_tipo_tarea, 0) ORDER BY al.ID_ESTADO");
                stmt = connection.prepareStatement( StringSql.toString() );
                stmt.setInt(1, intProj);
            }
//	    StringSql.append("SELECT distinct al.* FROM (SELECT * FROM VW_TAREA_OPCIONES"
//          StringSql.append("  WHERE"
//          StringSql.append(" DECODE('"+m.get("id_tarea")+"', 'null', '*', ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+")");
//          StringSql.append(" union all select distinct decode(ptta.id_tipo_tarea, 80, ptt.id_tarea, ptta.id_tarea)");
//          StringSql.append(" id_tarea, tee.NOMBRE_ESTADO nombre_opcion, tee.ID_ESTADO||'-'||decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') id_opcion, tee.ID_ESTADO, trim(tee.NOMBRE_ESTADO||' '||decode(ptta.id_tipo_tarea, 80, '- '||ptt.nombre_tarea, '')) NOMBRE_ACCION_SIGUIENTE, decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') valor_output, tee.HEX_COLOR_FILL, decode(ptta.id_tipo_tarea, 80, ptt.nombre_tarea, '0') nombre_tarea_output, ptta.id_tipo_tarea from tb_tarea_estado tee,");
//          StringSql.append("(select * from tb_proyecto_tarea where id_proyecto  = ?");  // Proyecto
//          StringSql.append(" and  length(id_tarea_dependencia) != length(replace(id_tarea_dependencia,");
//          StringSql.append(" to_char(DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+")), '0'))");
//          StringSql.append("union all select * from tb_proyecto_tarea where id_tarea = "+m.get("id_tarea"));
//          StringSql.append(" and (id_tarea_dependencia = '0' or id_tipo_tarea = 91)) ptt,(select * from tb_proyecto_tarea where id_tarea = "+m.get("id_tarea"));
//          StringSql.append(" and id_tipo_tarea != 84) ptta where tee.id_estado = 3 and ptt.id_estado != 3) al, tb_tarea_transicion tt, tb_proyecto_tarea pt where al.id_estado = tt.id_estado_siguiente");
//          StringSql.append(" and DECODE('"+m.get("id_tarea")+"', 'null', '*', pt.ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+ ")");
//          StringSql.append(" and pt.id_estado = tt.id_estado_actual and tt.id_tipo_tarea = decode(pt.id_tipo_tarea, tt.id_tipo_tarea, pt.id_tipo_tarea, 0) ORDER BY al.ID_ESTADO");
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());
            rs = stmt.executeQuery();
//            stmt = connection.createStatement();
//
//	    rs = stmt.executeQuery("SELECT distinct al.* FROM (SELECT * FROM VW_TAREA_OPCIONES"
//                    + "  WHERE"
//                    + " DECODE('"+m.get("id_tarea")+"', 'null', '*', ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+")"
//                    + " union all select distinct decode(ptta.id_tipo_tarea, 80, ptt.id_tarea, ptta.id_tarea)"
//                    + " id_tarea, tee.NOMBRE_ESTADO nombre_opcion, tee.ID_ESTADO||'-'||decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') id_opcion, tee.ID_ESTADO, trim(tee.NOMBRE_ESTADO||' '||decode(ptta.id_tipo_tarea, 80, '- '||ptt.nombre_tarea, '')) NOMBRE_ACCION_SIGUIENTE, decode(ptta.id_tipo_tarea, 80, ptt.tarea_input, '0') valor_output, tee.HEX_COLOR_FILL, decode(ptta.id_tipo_tarea, 80, ptt.nombre_tarea, '0') nombre_tarea_output, ptta.id_tipo_tarea from tb_tarea_estado tee,"
//                    + "(select * from tb_proyecto_tarea where id_proyecto  = "+m.get("id_proyecto")
//                    + " and  length(id_tarea_dependencia) != length(replace(id_tarea_dependencia,"
//                    + " to_char(DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+")), '0'))"
//                    + "union all select * from tb_proyecto_tarea where id_tarea = "+m.get("id_tarea")
//                    + " and (id_tarea_dependencia = '0' or id_tipo_tarea = 91)) ptt,(select * from tb_proyecto_tarea where id_tarea = "+m.get("id_tarea")
//                    + " and id_tipo_tarea != 84) ptta where tee.id_estado = 3 and ptt.id_estado != 3) al, tb_tarea_transicion tt, tb_proyecto_tarea pt where al.id_estado = tt.id_estado_siguiente"
//                    + " and DECODE('"+m.get("id_tarea")+"', 'null', '*', pt.ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+ ")"
//                    + " and pt.id_estado = tt.id_estado_actual and tt.id_tipo_tarea = decode(pt.id_tipo_tarea, tt.id_tipo_tarea, pt.id_tipo_tarea, 0) ORDER BY al.ID_ESTADO");


            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
               totalRegistro++;
               dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_TAREA", rs.getString("ID_TAREA"));
               dataIndividual.put("NOMBRE_OPCION", rs.getString("NOMBRE_OPCION"));
               dataIndividual.put("ID_OPCION", rs.getString("ID_OPCION"));
               dataIndividual.put("ID_ESTADO", rs.getString("ID_ESTADO"));
               dataIndividual.put("NOMBRE_ACCION_SIGUIENTE", rs.getString("NOMBRE_ACCION_SIGUIENTE"));
               dataIndividual.put("VALOR_OUTPUT", rs.getString("VALOR_OUTPUT"));
               dataIndividual.put("HEX_COLOR_FILL", rs.getString("HEX_COLOR_FILL"));
               dataIndividual.put("NOMBRE_TAREA_OUTPUT", rs.getString("NOMBRE_TAREA_OUTPUT"));
             dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }
            String json = gson.toJson(dataGrupal);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. "+e.getMessage());
            //e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e.fillInStackTrace());
                }
            }

            try {
                if (connection!= null && !connection.isClosed()){
                    connection.close();
                    connection= null;
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
