package cl.gesvita.ws.obteneravancetargrres;

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
 * Servlet implementation class ObtenerAvancetarGrRes
 */
@WebServlet("/WSObtenerAvancetarGrRes/ObtenerAvancetarGrRes")
public class ObtenerAvancetarGrRes extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerAvancetarGrRes.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerAvancetarGrRes() {
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
            String[] camposytipos = { "ID_PROYECTO","I","NOMBRE","S","FECHA_CREACION","S",
                "ID_TIPO","I","OBSERVACION_ACTUAL","S","ID_PROYECTO_PADRE","I",
                "ID_PROYECTO_TEMPLATE","I","ID_AREA","I","NOMBRE_AREA","S",
                "CODI_GRUPO_PROYECTO","S","NOMBRE_GRUPO_PROYECTO","S","SLA_PLAZOS","I",
                "DOCUMENTOS_ADJUNTOS","S","AVANCE_REAL_PROYECTO","I","ID_AREA_CLIENTE","I",
                "NOMBRE_AREA_CLIENTE","S","ID_SUCURSAL","I","NOMBRE_SUCURSAL","S",
                "CODI_GRUPO_SUCURSAL","S","NOMBRE_GRUPO_SUCURSAL","S","ID_ESTADO_PROYECTO","I",
                "NOMBRE_ESTADO_PROYECTO","S","ID_PRIORIDAD","I","NOMBRE_PRIORIDAD","S",
                "ID_FORMULARIO_PROYECTO","I","URL_FORMULARIO_PROYECTO","S","ID_USUARIO_CREACION","I",
                "NOMBRE_USUARIO_CREACION","S","ID_REQUERIMIENTO","I","NOMBRE_REQUERIMIENTO","S",
                "ID_TIPOSOLICITUD","I","NOMBRE_SOLICITUD","S","ID_USUARIO_JEFE_PROYECTO","I",
                "NOMBRE_JEFEPROYECTO","S","ID_TAREA_ABUELO","I","NOMBRE_TAREA_ABUELO","S",
                "ID_TIPO_TAREA_ABUELO","I","NOMBRE_TIPO_ABUELO","S","ID_ESTADO_ABUELO","I",
                "AVANCE_REAL_ABUELO","I","ID_TAREA_TEMPLATE_ABUELO","I","FECHA_PLAN_INI_ABUELO","D",
                "FECHA_REAL_INIPROY","S","FECHA_REAL_FINPROY","S","CANTIDADTAREAABUELO","I" };
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_PROYECTO_AVANCETARGRRES WHERE ");
            String sqlAnd = "";
            for (int i = 0; i < camposytipos.length;i+=2) {
                if (m.containsKey(camposytipos[i].toLowerCase())){
                    if (camposytipos[i+1].charAt(0) == 'D') {
                        StringSql.append( sqlAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append( sqlAnd + camposytipos[i] + " = ?");
                    }
                    sqlAnd = " AND ";
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
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_AVANCETARGRRES WHERE "+
//"DECODE('"+m.get("id_proyecto")+"', 'null', '*', ID_PROYECTO) = DECODE('"+m.get("id_proyecto")+"', 'null', '*', "+m.get("id_proyecto")+") AND DECODE('"+m.get("nombre")+"', 'null', '*', NOMBRE) = DECODE('"+m.get("nombre")+"', 'null', '*', '"+m.get("nombre")+"') AND DECODE('"+m.get("fecha_creacion")+"', 'null', '*', FECHA_CREACION) = DECODE('"+m.get("fecha_creacion")+"', 'null', '*', '"+m.get("fecha_creacion")+"') AND DECODE('"+m.get("id_tipo")+"', 'null', '*', ID_TIPO) = DECODE('"+m.get("id_tipo")+"', 'null', '*', "+m.get("id_tipo")+") AND DECODE('"+m.get("observacion_actual")+"', 'null', '*', OBSERVACION_ACTUAL) = DECODE('"+m.get("observacion_actual")+"', 'null', '*', '"+m.get("observacion_actual")+"') AND DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', ID_PROYECTO_PADRE) = DECODE('"+m.get("id_proyecto_padre")+"', 'null', '*', "+m.get("id_proyecto_padre")+") AND DECODE('"+m.get("id_proyecto_template")+"', 'null', '*', ID_PROYECTO_TEMPLATE) = DECODE('"+m.get("id_proyecto_template")+"', 'null', '*', "+m.get("id_proyecto_template")+") AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("codi_grupo_proyecto")+"', 'null', '*', CODI_GRUPO_PROYECTO) = DECODE('"+m.get("codi_grupo_proyecto")+"', 'null', '*', '"+m.get("codi_grupo_proyecto")+"') AND DECODE('"+m.get("nombre_grupo_proyecto")+"', 'null', '*', NOMBRE_GRUPO_PROYECTO) = DECODE('"+m.get("nombre_grupo_proyecto")+"', 'null', '*', '"+m.get("nombre_grupo_proyecto")+"') AND DECODE('"+m.get("sla_plazos")+"', 'null', '*', SLA_PLAZOS) = DECODE('"+m.get("sla_plazos")+"', 'null', '*', "+m.get("sla_plazos")+") AND DECODE('"+m.get("documentos_adjuntos")+"', 'null', '*', DOCUMENTOS_ADJUNTOS) = DECODE('"+m.get("documentos_adjuntos")+"', 'null', '*', '"+m.get("documentos_adjuntos")+"') AND DECODE('"+m.get("avance_real_proyecto")+"', 'null', '*', AVANCE_REAL_PROYECTO) = DECODE('"+m.get("avance_real_proyecto")+"', 'null', '*', "+m.get("avance_real_proyecto")+") AND DECODE('"+m.get("id_area_cliente")+"', 'null', '*', ID_AREA_CLIENTE) = DECODE('"+m.get("id_area_cliente")+"', 'null', '*', "+m.get("id_area_cliente")+") AND DECODE('"+m.get("nombre_area_cliente")+"', 'null', '*', NOMBRE_AREA_CLIENTE) = DECODE('"+m.get("nombre_area_cliente")+"', 'null', '*', '"+m.get("nombre_area_cliente")+"') AND DECODE('"+m.get("id_sucursal")+"', 'null', '*', ID_SUCURSAL) = DECODE('"+m.get("id_sucursal")+"', 'null', '*', "+m.get("id_sucursal")+") AND DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', NOMBRE_SUCURSAL) = DECODE('"+m.get("nombre_sucursal")+"', 'null', '*', '"+m.get("nombre_sucursal")+"') AND DECODE('"+m.get("codi_grupo_sucursal")+"', 'null', '*', CODI_GRUPO_SUCURSAL) = DECODE('"+m.get("codi_grupo_sucursal")+"', 'null', '*', '"+m.get("codi_grupo_sucursal")+"') AND DECODE('"+m.get("nombre_grupo_sucursal")+"', 'null', '*', NOMBRE_GRUPO_SUCURSAL) = DECODE('"+m.get("nombre_grupo_sucursal")+"', 'null', '*', '"+m.get("nombre_grupo_sucursal")+"') AND DECODE('"+m.get("id_estado_proyecto")+"', 'null', '*', ID_ESTADO_PROYECTO) = DECODE('"+m.get("id_estado_proyecto")+"', 'null', '*', "+m.get("id_estado_proyecto")+") AND DECODE('"+m.get("nombre_estado_proyecto")+"', 'null', '*', NOMBRE_ESTADO_PROYECTO) = DECODE('"+m.get("nombre_estado_proyecto")+"', 'null', '*', '"+m.get("nombre_estado_proyecto")+"') AND DECODE('"+m.get("id_prioridad")+"', 'null', '*', ID_PRIORIDAD) = DECODE('"+m.get("id_prioridad")+"', 'null', '*', "+m.get("id_prioridad")+") AND DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', NOMBRE_PRIORIDAD) = DECODE('"+m.get("nombre_prioridad")+"', 'null', '*', '"+m.get("nombre_prioridad")+"') AND DECODE('"+m.get("id_formulario_proyecto")+"', 'null', '*', ID_FORMULARIO_PROYECTO) = DECODE('"+m.get("id_formulario_proyecto")+"', 'null', '*', "+m.get("id_formulario_proyecto")+") AND DECODE('"+m.get("url_formulario_proyecto")+"', 'null', '*', URL_FORMULARIO_PROYECTO) = DECODE('"+m.get("url_formulario_proyecto")+"', 'null', '*', '"+m.get("url_formulario_proyecto")+"') AND DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', ID_USUARIO_CREACION) = DECODE('"+m.get("id_usuario_creacion")+"', 'null', '*', "+m.get("id_usuario_creacion")+") AND DECODE('"+m.get("nombre_usuario_creacion")+"', 'null', '*', NOMBRE_USUARIO_CREACION) = DECODE('"+m.get("nombre_usuario_creacion")+"', 'null', '*', '"+m.get("nombre_usuario_creacion")+"') AND DECODE('"+m.get("id_requerimiento")+"', 'null', '*', ID_REQUERIMIENTO) = DECODE('"+m.get("id_requerimiento")+"', 'null', '*', "+m.get("id_requerimiento")+") AND DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', NOMBRE_REQUERIMIENTO) = DECODE('"+m.get("nombre_requerimiento")+"', 'null', '*', '"+m.get("nombre_requerimiento")+"') AND DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', ID_TIPOSOLICITUD) = DECODE('"+m.get("id_tiposolicitud")+"', 'null', '*', "+m.get("id_tiposolicitud")+") AND DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', NOMBRE_SOLICITUD) = DECODE('"+m.get("nombre_solicitud")+"', 'null', '*', '"+m.get("nombre_solicitud")+"') AND DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', ID_USUARIO_JEFE_PROYECTO) = DECODE('"+m.get("id_usuario_jefe_proyecto")+"', 'null', '*', "+m.get("id_usuario_jefe_proyecto")+") AND DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', NOMBRE_JEFEPROYECTO) = DECODE('"+m.get("nombre_jefeproyecto")+"', 'null', '*', '"+m.get("nombre_jefeproyecto")+"') AND DECODE('"+m.get("id_tarea_abuelo")+"', 'null', '*', ID_TAREA_ABUELO) = DECODE('"+m.get("id_tarea_abuelo")+"', 'null', '*', "+m.get("id_tarea_abuelo")+") AND DECODE('"+m.get("nombre_tarea_abuelo")+"', 'null', '*', NOMBRE_TAREA_ABUELO) = DECODE('"+m.get("nombre_tarea_abuelo")+"', 'null', '*', '"+m.get("nombre_tarea_abuelo")+"') AND DECODE('"+m.get("id_tipo_tarea_abuelo")+"', 'null', '*', ID_TIPO_TAREA_ABUELO) = DECODE('"+m.get("id_tipo_tarea_abuelo")+"', 'null', '*', "+m.get("id_tipo_tarea_abuelo")+") AND DECODE('"+m.get("nombre_tipo_abuelo")+"', 'null', '*', NOMBRE_TIPO_ABUELO) = DECODE('"+m.get("nombre_tipo_abuelo")+"', 'null', '*', '"+m.get("nombre_tipo_abuelo")+"') AND DECODE('"+m.get("id_estado_abuelo")+"', 'null', '*', ID_ESTADO_ABUELO) = DECODE('"+m.get("id_estado_abuelo")+"', 'null', '*', "+m.get("id_estado_abuelo")+") AND DECODE('"+m.get("avance_real_abuelo")+"', 'null', '*', AVANCE_REAL_ABUELO) = DECODE('"+m.get("avance_real_abuelo")+"', 'null', '*', "+m.get("avance_real_abuelo")+") AND DECODE('"+m.get("id_tarea_template_abuelo")+"', 'null', '*', ID_TAREA_TEMPLATE_ABUELO) = DECODE('"+m.get("id_tarea_template_abuelo")+"', 'null', '*', "+m.get("id_tarea_template_abuelo")+") AND DECODE('"+m.get("fecha_plan_ini_abuelo")+"', 'null', '*', FECHA_PLAN_INI_ABUELO) = DECODE('"+m.get("fecha_plan_ini_abuelo")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini_abuelo")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_iniproy")+"', 'null', '*', FECHA_REAL_INIPROY) = DECODE('"+m.get("fecha_real_iniproy")+"', 'null', '*', '"+m.get("fecha_real_iniproy")+"') AND DECODE('"+m.get("fecha_real_finproy")+"', 'null', '*', FECHA_REAL_FINPROY) = DECODE('"+m.get("fecha_real_finproy")+"', 'null', '*', '"+m.get("fecha_real_finproy")+"') AND DECODE('"+m.get("cantidadtareaabuelo")+"', 'null', '*', CANTIDADTAREAABUELO) = DECODE('"+m.get("cantidadtareaabuelo")+"', 'null', '*', "+m.get("cantidadtareaabuelo")+")");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_PROYECTO", rs.getString("ID_PROYECTO"));
               dataIndividual.put("NOMBRE", rs.getString("NOMBRE"));
               dataIndividual.put("FECHA_CREACION", rs.getString("FECHA_CREACION"));
               dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
               dataIndividual.put("OBSERVACION_ACTUAL", rs.getString("OBSERVACION_ACTUAL"));
               dataIndividual.put("ID_PROYECTO_PADRE", rs.getString("ID_PROYECTO_PADRE"));
               dataIndividual.put("ID_PROYECTO_TEMPLATE", rs.getString("ID_PROYECTO_TEMPLATE"));
               dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
               dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
               dataIndividual.put("CODI_GRUPO_PROYECTO", rs.getString("CODI_GRUPO_PROYECTO"));
               dataIndividual.put("NOMBRE_GRUPO_PROYECTO", rs.getString("NOMBRE_GRUPO_PROYECTO"));
               dataIndividual.put("SLA_PLAZOS", rs.getString("SLA_PLAZOS"));
               dataIndividual.put("DOCUMENTOS_ADJUNTOS", rs.getString("DOCUMENTOS_ADJUNTOS"));
               dataIndividual.put("AVANCE_REAL_PROYECTO", rs.getString("AVANCE_REAL_PROYECTO"));
               dataIndividual.put("ID_AREA_CLIENTE", rs.getString("ID_AREA_CLIENTE"));
               dataIndividual.put("NOMBRE_AREA_CLIENTE", rs.getString("NOMBRE_AREA_CLIENTE"));
               dataIndividual.put("ID_SUCURSAL", rs.getString("ID_SUCURSAL"));
               dataIndividual.put("NOMBRE_SUCURSAL", rs.getString("NOMBRE_SUCURSAL"));
               dataIndividual.put("CODI_GRUPO_SUCURSAL", rs.getString("CODI_GRUPO_SUCURSAL"));
               dataIndividual.put("NOMBRE_GRUPO_SUCURSAL", rs.getString("NOMBRE_GRUPO_SUCURSAL"));
               dataIndividual.put("ID_ESTADO_PROYECTO", rs.getString("ID_ESTADO_PROYECTO"));
               dataIndividual.put("NOMBRE_ESTADO_PROYECTO", rs.getString("NOMBRE_ESTADO_PROYECTO"));
               dataIndividual.put("ID_PRIORIDAD", rs.getString("ID_PRIORIDAD"));
               dataIndividual.put("NOMBRE_PRIORIDAD", rs.getString("NOMBRE_PRIORIDAD"));
               dataIndividual.put("ID_FORMULARIO_PROYECTO", rs.getString("ID_FORMULARIO_PROYECTO"));
               dataIndividual.put("URL_FORMULARIO_PROYECTO", rs.getString("URL_FORMULARIO_PROYECTO"));
               dataIndividual.put("ID_USUARIO_CREACION", rs.getString("ID_USUARIO_CREACION"));
               dataIndividual.put("NOMBRE_USUARIO_CREACION", rs.getString("NOMBRE_USUARIO_CREACION"));
               dataIndividual.put("ID_REQUERIMIENTO", rs.getString("ID_REQUERIMIENTO"));
               dataIndividual.put("NOMBRE_REQUERIMIENTO", rs.getString("NOMBRE_REQUERIMIENTO"));
               dataIndividual.put("ID_TIPOSOLICITUD", rs.getString("ID_TIPOSOLICITUD"));
               dataIndividual.put("NOMBRE_SOLICITUD", rs.getString("NOMBRE_SOLICITUD"));
               dataIndividual.put("ID_USUARIO_JEFE_PROYECTO", rs.getString("ID_USUARIO_JEFE_PROYECTO"));
               dataIndividual.put("NOMBRE_JEFEPROYECTO", rs.getString("NOMBRE_JEFEPROYECTO"));
               dataIndividual.put("ID_TAREA_ABUELO", rs.getString("ID_TAREA_ABUELO"));
               dataIndividual.put("NOMBRE_TAREA_ABUELO", rs.getString("NOMBRE_TAREA_ABUELO"));
               dataIndividual.put("ID_TIPO_TAREA_ABUELO", rs.getString("ID_TIPO_TAREA_ABUELO"));
               dataIndividual.put("NOMBRE_TIPO_ABUELO", rs.getString("NOMBRE_TIPO_ABUELO"));
               dataIndividual.put("ID_ESTADO_ABUELO", rs.getString("ID_ESTADO_ABUELO"));
               dataIndividual.put("AVANCE_REAL_ABUELO", rs.getString("AVANCE_REAL_ABUELO"));
               dataIndividual.put("ID_TAREA_TEMPLATE_ABUELO", rs.getString("ID_TAREA_TEMPLATE_ABUELO"));
               dataIndividual.put("FECHA_PLAN_INI_ABUELO", rs.getString("FECHA_PLAN_INI_ABUELO"));
               dataIndividual.put("FECHA_REAL_INIPROY", rs.getString("FECHA_REAL_INIPROY"));
               dataIndividual.put("FECHA_REAL_FINPROY", rs.getString("FECHA_REAL_FINPROY"));
               dataIndividual.put("CANTIDADTAREAABUELO", rs.getString("CANTIDADTAREAABUELO"));
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
//            if (datasource != null)
//            {
//                try {
//                        datasource.close();
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                    }
//            }
//            if (rs != null)
//            {
//                try {
//                    rs.close();
//                }
//                catch (SQLException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//
//            if (stmt != null){
//                try {
//                    stmt.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

}
