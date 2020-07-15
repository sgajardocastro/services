package cl.gesvita.ws.obtenernotificaciones;

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
 * Servlet implementation class ObtenerNotificaciones
 */
@WebServlet("/WSObtenerNotificaciones/ObtenerNotificaciones")
public class ObtenerNotificaciones extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerNotificaciones.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerNotificaciones() {
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
        PreparedStatement stmt = null;
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
            StringBuilder StringSql = new StringBuilder();
            // Campos de la consulta con sus tipos
            SimpleDateFormat formato_ddmmyyyy = new SimpleDateFormat("dd/MM/yyyy");
            String[] camposytipos = { "ID_NOTIFICACION","I","NOMBRE_ALERTA","S","TIPO_ALERTA","I","NOMBRE_TIPO","S","DESCRIPCION_TIPO","S","DESCRIPCION_ALERTA","S","MODULO_ORIGEN","S","SUBMODULO_ORIGEN","S","NOMBRE_USUARIO","S","FECHA_ALERTA","D","FECHA_EVENTO","D","FECHA_ENVIO","D","SEVERIDAD_ALERTA","S","ID_EVENTO","I","ID_ALERTA","I"};
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_NOTIFICACION_OPERACION");
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append( sqlWhereAnd + camposytipos[i] + " = ?");
                    }
                    sqlWhereAnd = " AND ";
                }
            }
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement( StringSql.toString() );
            for (int i = 0, j = 1; i < camposytipos.length; i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    switch (camposytipos[i+1].charAt(0)) {
                        case 'I':
                            int intVal = Integer.parseInt(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val["+ j +"]= " + intVal);
                            stmt.setInt(j, intVal);
                            break;
                        case 'F':
                            float floatVal = Float.parseFloat(m.get(camposytipos[i].toLowerCase()));
                            // BYSECURITY logger.debug("val["+ j +"]= " + floatVal);
                            stmt.setFloat(j, floatVal);
                            break;
                        case 'S':
                        case 'D':
                            String val = m.get(camposytipos[i].toLowerCase());
                            // BYSECURITY logger.debug("val["+ j +"]= " + val);
                            stmt.setString(j, val );
                            break;
                    }
                    j++;
                }
            }
            
            rs = stmt.executeQuery();
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_NOTIFICACION_OPERACION WHERE "
//                    + "DECODE('" + m.get("id_notificacion") + "', 'null', '*', ID_NOTIFICACION) = DECODE('" + m.get("id_notificacion") + "', 'null', '*', " + m.get("id_notificacion") + ") AND DECODE('" + m.get("nombre_alerta") + "', 'null', '*', NOMBRE_ALERTA) = DECODE('" + m.get("nombre_alerta") + "', 'null', '*', '" + m.get("nombre_alerta") + "') AND DECODE('" + m.get("tipo_alerta") + "', 'null', '*', TIPO_ALERTA) = DECODE('" + m.get("tipo_alerta") + "', 'null', '*', " + m.get("tipo_alerta") + ") AND DECODE('" + m.get("nombre_tipo") + "', 'null', '*', NOMBRE_TIPO) = DECODE('" + m.get("nombre_tipo") + "', 'null', '*', '" + m.get("nombre_tipo") + "') AND DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', DESCRIPCION_TIPO) = DECODE('" + m.get("descripcion_tipo") + "', 'null', '*', '" + m.get("descripcion_tipo") + "') AND DECODE('" + m.get("descripcion_alerta") + "', 'null', '*', DESCRIPCION_ALERTA) = DECODE('" + m.get("descripcion_alerta") + "', 'null', '*', '" + m.get("descripcion_alerta") + "') AND DECODE('" + m.get("modulo_origen") + "', 'null', '*', MODULO_ORIGEN) = DECODE('" + m.get("modulo_origen") + "', 'null', '*', '" + m.get("modulo_origen") + "') AND DECODE('" + m.get("submodulo_origen") + "', 'null', '*', SUBMODULO_ORIGEN) = DECODE('" + m.get("submodulo_origen") + "', 'null', '*', '" + m.get("submodulo_origen") + "') AND DECODE('" + m.get("nombre_usuario") + "', 'null', '*', NOMBRE_USUARIO) = DECODE('" + m.get("nombre_usuario") + "', 'null', '*', '" + m.get("nombre_usuario") + "') AND DECODE('" + m.get("fecha_alerta") + "', 'null', '*', FECHA_ALERTA) = DECODE('" + m.get("fecha_alerta") + "', 'null', '*', to_date('" + m.get("fecha_alerta") + "', 'dd/mm/yyyy')) AND DECODE('" + m.get("severidad_alerta") + "', 'null', '*', SEVERIDAD_ALERTA) = DECODE('" + m.get("severidad_alerta") + "', 'null', '*', '" + m.get("severidad_alerta") + "') AND DECODE('" + m.get("id_evento") + "', 'null', '*', ID_EVENTO) = DECODE('" + m.get("id_evento") + "', 'null', '*', " + m.get("id_evento") + ") AND DECODE('" + m.get("id_alerta") + "', 'null', '*', ID_ALERTA) = DECODE('" + m.get("id_alerta") + "', 'null', '*', " + m.get("id_alerta") + ")");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_NOTIFICACION", rs.getString("ID_NOTIFICACION"));
                dataIndividual.put("NOMBRE_ALERTA", rs.getString("NOMBRE_ALERTA"));
                dataIndividual.put("TIPO_ALERTA", rs.getString("TIPO_ALERTA"));
                dataIndividual.put("NOMBRE_TIPO", rs.getString("NOMBRE_TIPO"));
                dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
                dataIndividual.put("DESCRIPCION_ALERTA", rs.getString("DESCRIPCION_ALERTA"));
                dataIndividual.put("MODULO_ORIGEN", rs.getString("MODULO_ORIGEN"));
                dataIndividual.put("SUBMODULO_ORIGEN", rs.getString("SUBMODULO_ORIGEN"));
                dataIndividual.put("NOMBRE_USUARIO", rs.getString("NOMBRE_USUARIO"));
                dataIndividual.put("FECHA_ALERTA", rs.getString("FECHA_ALERTA"));
                dataIndividual.put("SEVERIDAD_ALERTA", rs.getString("SEVERIDAD_ALERTA"));
                dataIndividual.put("ID_EVENTO", rs.getString("ID_EVENTO"));
                dataIndividual.put("ID_ALERTA", rs.getString("ID_ALERTA"));
                dataGrupal.put(String.valueOf(totalRegistro), dataIndividual);
            }
            String json = gson.toJson(dataGrupal);
            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. " + e.getMessage());
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
