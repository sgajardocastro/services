package cl.gesvita.ws.obtenersubtareas;

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
 * Servlet implementation class ObtenerSubTareasOld
 */
@WebServlet("/WSObtenerSubTareas/ObtenerSubTareasOld")
public class ObtenerSubTareasOld extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerSubTareasOld.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerSubTareasOld() {
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
            String[] camposytipos = {
//                "ID_PROYECTO","I","ID_TAREA","I","NOMBRE_TAREA","S","ID_TIPO_TAREA","I","CODI_TAREA","S",
//                "ID_ESTADO","I","NOMBRE_ESTADO","S","TIPO_TAREA_NEGOCIO","S","ID_USUARIO_EJECUTOR","I",
//                "CODI_USUARIO_EJECUTOR","S","NOMBRE_USUARIO_EJECUTOR","S","FECHA_PLAN_INI","D",
//                "FECHA_PLAN_FIN","D","FECHA_REAL_INI","D","FECHA_REAL_FIN","D","FECHA_PLAN_INI_FMT","S",
//                "FECHA_PLAN_FIN_FMT","S","FECHA_REAL_INI_FMT","S","FECHA_REAL_FIN_FMT","S","FECHA_MODIFICACION_FMT","S",
//                "NOMBRE_USUARIO_MODIFICACION","S","ID_TAREA_PADRE","I","ID_TAREA_HIJO","I","NOMBRE_TAREA_HIJO","S",
//                "TIPO_TAREA_NEGOCIO_HIJO","S","ID_TIPO_TAREA_HIJO","I","CODI_TAREA_HIJO","S","ID_ESTADO_HIJO","I",
//                "NOMBRE_ESTADO_HIJO","S","ID_USUARIO_EJECUTOR_HIJO","I","CODI_USUARIO_EJECUTOR_HIJO","S",
//                "NOMBRE_USUARIO_EJECUTOR_HIJO","S","FECHA_PLAN_INI_HIJO","D","FECHA_PLAN_FIN_HIJO","D",
//                "FECHA_REAL_INI_HIJO","D","FECHA_REAL_FIN_HIJO","D","FECHA_PLAN_INI_FMT_HIJO","S","FECHA_PLAN_FIN_FMT_HIJO","S",
//                "FECHA_REAL_INI_FMT_HIJO","S","FECHA_REAL_FIN_FMT_HIJO","S","FECHA_MODIFICACION_FMT_HIJO","S",
//                "NOMBRE_USUARIO_MODIFICACION_HIJO","S","TAREA_OUTPUT_HIJO","S","ID_TAREA_NIETO","I","NOMBRE_TAREA_NIETO","S",
//                "CODI_TAREA_NIETO","S","ID_TIPO_TAREA_NIETO","I","ID_ESTADO_NIETO","I","NOMBRE_ESTADO_NIETO","S",
//                "ID_USUARIO_EJECUTOR_NIETO","I","CODI_USUARIO_EJECUTOR_NIETO","S","NOMBRE_USUARIO_EJECUTOR_NIETO","S",
//                "FECHA_PLAN_INI_NIETO","D","FECHA_PLAN_FIN_NIETO","D","FECHA_REAL_INI_NIETO","D","FECHA_REAL_FIN_NIETO","D",
//                "FECHA_PLAN_INI_FMT_NIETO","S","FECHA_PLAN_FIN_FMT_NIETO","S","FECHA_REAL_INI_FMT_NIETO","S",
//                "FECHA_REAL_FIN_FMT_NIETO","S","FECHA_MODIFICACION_FMT_NIETO","S","NOMBRE_USUARIO_MODIFICACION_NIETO","S",
//                "TAREA_OUTPUT_NIETO","S","DATA_EXTENDIDA_NIETO","S"

                                      "ID_PROYECTO","I","ID_TAREA","I","NOMBRE_TAREA","S","ID_TIPO_TAREA","I","CODI_TAREA","S",
                "ID_ESTADO","I","NOMBRE_ESTADO","S","TIPO_TAREA_NEGOCIO","S","ID_USUARIO_EJECUTOR","I",
                "CODI_USUARIO_EJECUTOR","S","NOMBRE_USUARIO_EJECUTOR","S","FECHA_PLAN_INI","D",
                "FECHA_PLAN_FIN","D","FECHA_REAL_INI","D","FECHA_REAL_FIN","D","FECHA_PLAN_INI_FMT","S",
                "FECHA_PLAN_FIN_FMT","S","FECHA_REAL_INI_FMT","S","FECHA_REAL_FIN_FMT","S","FECHA_MODIFICACION_FMT","S",
                "NOMBRE_USUARIO_MODIFICACION","S","ID_TAREA_PADRE","I","ID_TAREA_HIJO","I","NOMBRE_TAREA_HIJO","S",
                "TIPO_TAREA_NEGOCIO_HIJO","S","ID_TIPO_TAREA_HIJO","I","CODI_TAREA_HIJO","S","ID_ESTADO_HIJO","I",
                "NOMBRE_ESTADO_HIJO","S","ID_USUARIO_EJECUTOR_HIJO","I","CODI_USUARIO_EJECUTOR_HIJO","S",
                "NOMBRE_USUARIO_EJECUTOR_HIJO","S","FECHA_PLAN_INI_HIJO","D","FECHA_PLAN_FIN_HIJO","D",
                "FECHA_REAL_INI_HIJO","D","FECHA_REAL_FIN_HIJO","D","FECHA_PLAN_INI_FMT_HIJO","S","FECHA_PLAN_FIN_FMT_HIJO","S",
                "FECHA_REAL_INI_FMT_HIJO","S","FECHA_REAL_FIN_FMT_HIJO","S","FECHA_MODIFICACION_FMT_HIJO","S",
                "NOMBRE_USUARIO_MODIFICACION_HIJO","S","TAREA_OUTPUT_HIJO","S","DATA_EXTENDIDA_HIJO","S",
                "ID_TAREA_NIETO","I","NOMBRE_TAREA_NIETO","S",
                "CODI_TAREA_NIETO","S","ID_TIPO_TAREA_NIETO","I","ID_ESTADO_NIETO","I","NOMBRE_ESTADO_NIETO","S",
                "ID_USUARIO_EJECUTOR_NIETO","I","CODI_USUARIO_EJECUTOR_NIETO","S","NOMBRE_USUARIO_EJECUTOR_NIETO","S",
                "FECHA_PLAN_INI_NIETO","D","FECHA_PLAN_FIN_NIETO","D","FECHA_REAL_INI_NIETO","D","FECHA_REAL_FIN_NIETO","D",
                "FECHA_PLAN_INI_FMT_NIETO","S","FECHA_PLAN_FIN_FMT_NIETO","S","FECHA_REAL_INI_FMT_NIETO","S",
                "FECHA_REAL_FIN_FMT_NIETO","S","FECHA_MODIFICACION_FMT_NIETO","S","NOMBRE_USUARIO_MODIFICACION_NIETO","S",
                "TAREA_OUTPUT_NIETO","S","DATA_EXTENDIDA_NIETO","S"};

            String json = ObtenerLib.getDefaultObtenerResoultSet(logger, connection, camposytipos, "VW_PROYECTO_SUBTAREAS" , m);
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_PROYECTO_SUBTAREAS WHERE "+
//"DECODE('"+m.get("nombre_estado_hijo")+"', 'null', '*', NOMBRE_ESTADO_HIJO) = DECODE('"+m.get("nombre_estado_hijo")+"', 'null', '*', '"+m.get("nombre_estado_hijo")+"') AND DECODE('"+m.get("id_usuario_ejecutor_hijo")+"', 'null', '*', ID_USUARIO_EJECUTOR_HIJO) = DECODE('"+m.get("id_usuario_ejecutor_hijo")+"', 'null', '*', "+m.get("id_usuario_ejecutor_hijo")+") AND DECODE('"+m.get("codi_usuario_ejecutor_hijo")+"', 'null', '*', CODI_USUARIO_EJECUTOR_HIJO) = DECODE('"+m.get("codi_usuario_ejecutor_hijo")+"', 'null', '*', '"+m.get("codi_usuario_ejecutor_hijo")+"') AND DECODE('"+m.get("nombre_usuario_ejecutor_hijo")+"', 'null', '*', NOMBRE_USUARIO_EJECUTOR_HIJO) = DECODE('"+m.get("nombre_usuario_ejecutor_hijo")+"', 'null', '*', '"+m.get("nombre_usuario_ejecutor_hijo")+"') AND DECODE('"+m.get("fecha_plan_ini_hijo")+"', 'null', '*', FECHA_PLAN_INI_HIJO) = DECODE('"+m.get("fecha_plan_ini_hijo")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini_hijo")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_fin_hijo")+"', 'null', '*', FECHA_PLAN_FIN_HIJO) = DECODE('"+m.get("fecha_plan_fin_hijo")+"', 'null', '*', to_date('"+m.get("fecha_plan_fin_hijo")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_ini_hijo")+"', 'null', '*', FECHA_REAL_INI_HIJO) = DECODE('"+m.get("fecha_real_ini_hijo")+"', 'null', '*', to_date('"+m.get("fecha_real_ini_hijo")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_fin_hijo")+"', 'null', '*', FECHA_REAL_FIN_HIJO) = DECODE('"+m.get("fecha_real_fin_hijo")+"', 'null', '*', to_date('"+m.get("fecha_real_fin_hijo")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_ini_fmt_hijo")+"', 'null', '*', FECHA_PLAN_INI_FMT_HIJO) = DECODE('"+m.get("fecha_plan_ini_fmt_hijo")+"', 'null', '*', '"+m.get("fecha_plan_ini_fmt_hijo")+"') AND DECODE('"+m.get("fecha_plan_fin_fmt_hijo")+"', 'null', '*', FECHA_PLAN_FIN_FMT_HIJO) = DECODE('"+m.get("fecha_plan_fin_fmt_hijo")+"', 'null', '*', '"+m.get("fecha_plan_fin_fmt_hijo")+"') AND DECODE('"+m.get("fecha_real_ini_fmt_hijo")+"', 'null', '*', FECHA_REAL_INI_FMT_HIJO) = DECODE('"+m.get("fecha_real_ini_fmt_hijo")+"', 'null', '*', '"+m.get("fecha_real_ini_fmt_hijo")+"') AND DECODE('"+m.get("fecha_real_fin_fmt_hijo")+"', 'null', '*', FECHA_REAL_FIN_FMT_HIJO) = DECODE('"+m.get("fecha_real_fin_fmt_hijo")+"', 'null', '*', '"+m.get("fecha_real_fin_fmt_hijo")+"') AND DECODE('"+m.get("fecha_modificacion_fmt_hijo")+"', 'null', '*', FECHA_MODIFICACION_FMT_HIJO) = DECODE('"+m.get("fecha_modificacion_fmt_hijo")+"', 'null', '*', '"+m.get("fecha_modificacion_fmt_hijo")+"') AND DECODE('"+m.get("nombre_usuario_modificacion_hijo")+"', 'null', '*', NOMBRE_USUARIO_MODIFICACION_HIJO) = DECODE('"+m.get("nombre_usuario_modificacion_hijo")+"', 'null', '*', '"+m.get("nombre_usuario_modificacion_hijo")+"') AND DECODE('"+m.get("tarea_output_hijo")+"', 'null', '*', TAREA_OUTPUT_HIJO) = DECODE('"+m.get("tarea_output_hijo")+"', 'null', '*', '"+m.get("tarea_output_hijo")+"') AND DECODE('"+m.get("id_tarea_nieto")+"', 'null', '*', ID_TAREA_NIETO) = DECODE('"+m.get("id_tarea_nieto")+"', 'null', '*', "+m.get("id_tarea_nieto")+") AND DECODE('"+m.get("nombre_tarea_nieto")+"', 'null', '*', NOMBRE_TAREA_NIETO) = DECODE('"+m.get("nombre_tarea_nieto")+"', 'null', '*', '"+m.get("nombre_tarea_nieto")+"') AND DECODE('"+m.get("codi_tarea_nieto")+"', 'null', '*', CODI_TAREA_NIETO) = DECODE('"+m.get("codi_tarea_nieto")+"', 'null', '*', '"+m.get("codi_tarea_nieto")+"') AND DECODE('"+m.get("id_tipo_tarea_nieto")+"', 'null', '*', ID_TIPO_TAREA_NIETO) = DECODE('"+m.get("id_tipo_tarea_nieto")+"', 'null', '*', "+m.get("id_tipo_tarea_nieto")+") AND DECODE('"+m.get("id_estado_nieto")+"', 'null', '*', ID_ESTADO_NIETO) = DECODE('"+m.get("id_estado_nieto")+"', 'null', '*', "+m.get("id_estado_nieto")+") AND DECODE('"+m.get("nombre_estado_nieto")+"', 'null', '*', NOMBRE_ESTADO_NIETO) = DECODE('"+m.get("nombre_estado_nieto")+"', 'null', '*', '"+m.get("nombre_estado_nieto")+"') AND DECODE('"+m.get("id_usuario_ejecutor_nieto")+"', 'null', '*', ID_USUARIO_EJECUTOR_NIETO) = DECODE('"+m.get("id_usuario_ejecutor_nieto")+"', 'null', '*', "+m.get("id_usuario_ejecutor_nieto")+") AND DECODE('"+m.get("codi_usuario_ejecutor_nieto")+"', 'null', '*', CODI_USUARIO_EJECUTOR_NIETO) = DECODE('"+m.get("codi_usuario_ejecutor_nieto")+"', 'null', '*', '"+m.get("codi_usuario_ejecutor_nieto")+"') AND DECODE('"+m.get("nombre_usuario_ejecutor_nieto")+"', 'null', '*', NOMBRE_USUARIO_EJECUTOR_NIETO) = DECODE('"+m.get("nombre_usuario_ejecutor_nieto")+"', 'null', '*', '"+m.get("nombre_usuario_ejecutor_nieto")+"') AND DECODE('"+m.get("fecha_plan_ini_nieto")+"', 'null', '*', FECHA_PLAN_INI_NIETO) = DECODE('"+m.get("fecha_plan_ini_nieto")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini_nieto")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_fin_nieto")+"', 'null', '*', FECHA_PLAN_FIN_NIETO) = DECODE('"+m.get("fecha_plan_fin_nieto")+"', 'null', '*', to_date('"+m.get("fecha_plan_fin_nieto")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_ini_nieto")+"', 'null', '*', FECHA_REAL_INI_NIETO) = DECODE('"+m.get("fecha_real_ini_nieto")+"', 'null', '*', to_date('"+m.get("fecha_real_ini_nieto")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_fin_nieto")+"', 'null', '*', FECHA_REAL_FIN_NIETO) = DECODE('"+m.get("fecha_real_fin_nieto")+"', 'null', '*', to_date('"+m.get("fecha_real_fin_nieto")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_ini_fmt_nieto")+"', 'null', '*', FECHA_PLAN_INI_FMT_NIETO) = DECODE('"+m.get("fecha_plan_ini_fmt_nieto")+"', 'null', '*', '"+m.get("fecha_plan_ini_fmt_nieto")+"') AND DECODE('"+m.get("fecha_plan_fin_fmt_nieto")+"', 'null', '*', FECHA_PLAN_FIN_FMT_NIETO) = DECODE('"+m.get("fecha_plan_fin_fmt_nieto")+"', 'null', '*', '"+m.get("fecha_plan_fin_fmt_nieto")+"') AND DECODE('"+m.get("fecha_real_ini_fmt_nieto")+"', 'null', '*', FECHA_REAL_INI_FMT_NIETO) = DECODE('"+m.get("fecha_real_ini_fmt_nieto")+"', 'null', '*', '"+m.get("fecha_real_ini_fmt_nieto")+"') AND DECODE('"+m.get("fecha_real_fin_fmt_nieto")+"', 'null', '*', FECHA_REAL_FIN_FMT_NIETO) = DECODE('"+m.get("fecha_real_fin_fmt_nieto")+"', 'null', '*', '"+m.get("fecha_real_fin_fmt_nieto")+"') AND DECODE('"+m.get("fecha_modificacion_fmt_nieto")+"', 'null', '*', FECHA_MODIFICACION_FMT_NIETO) = DECODE('"+m.get("fecha_modificacion_fmt_nieto")+"', 'null', '*', '"+m.get("fecha_modificacion_fmt_nieto")+"') AND DECODE('"+m.get("nombre_usuario_modificacion_nieto")+"', 'null', '*', NOMBRE_USUARIO_MODIFICACION_NIETO) = DECODE('"+m.get("nombre_usuario_modificacion_nieto")+"', 'null', '*', '"+m.get("nombre_usuario_modificacion_nieto")+"') AND DECODE('"+m.get("tarea_output_nieto")+"', 'null', '*', TAREA_OUTPUT_NIETO) = DECODE('"+m.get("tarea_output_nieto")+"', 'null', '*', '"+m.get("tarea_output_nieto")+"') AND DECODE('"+m.get("data_extendida_nieto")+"', 'null', '*', DATA_EXTENDIDA_NIETO) = DECODE('"+m.get("data_extendida_nieto")+"', 'null', '*', '"+m.get("data_extendida_nieto")+"') AND DECODE('"+m.get("id_proyecto")+"', 'null', '*', ID_PROYECTO) = DECODE('"+m.get("id_proyecto")+"', 'null', '*', "+m.get("id_proyecto")+") AND DECODE('"+m.get("id_tarea")+"', 'null', '*', ID_TAREA) = DECODE('"+m.get("id_tarea")+"', 'null', '*', "+m.get("id_tarea")+") AND DECODE('"+m.get("nombre_tarea")+"', 'null', '*', NOMBRE_TAREA) = DECODE('"+m.get("nombre_tarea")+"', 'null', '*', '"+m.get("nombre_tarea")+"') AND DECODE('"+m.get("id_tipo_tarea")+"', 'null', '*', ID_TIPO_TAREA) = DECODE('"+m.get("id_tipo_tarea")+"', 'null', '*', "+m.get("id_tipo_tarea")+") AND DECODE('"+m.get("codi_tarea")+"', 'null', '*', CODI_TAREA) = DECODE('"+m.get("codi_tarea")+"', 'null', '*', '"+m.get("codi_tarea")+"') AND DECODE('"+m.get("id_estado")+"', 'null', '*', ID_ESTADO) = DECODE('"+m.get("id_estado")+"', 'null', '*', "+m.get("id_estado")+") AND DECODE('"+m.get("nombre_estado")+"', 'null', '*', NOMBRE_ESTADO) = DECODE('"+m.get("nombre_estado")+"', 'null', '*', '"+m.get("nombre_estado")+"') AND DECODE('"+m.get("tipo_tarea_negocio")+"', 'null', '*', TIPO_TAREA_NEGOCIO) = DECODE('"+m.get("tipo_tarea_negocio")+"', 'null', '*', '"+m.get("tipo_tarea_negocio")+"') AND DECODE('"+m.get("id_usuario_ejecutor")+"', 'null', '*', ID_USUARIO_EJECUTOR) = DECODE('"+m.get("id_usuario_ejecutor")+"', 'null', '*', "+m.get("id_usuario_ejecutor")+") AND DECODE('"+m.get("codi_usuario_ejecutor")+"', 'null', '*', CODI_USUARIO_EJECUTOR) = DECODE('"+m.get("codi_usuario_ejecutor")+"', 'null', '*', '"+m.get("codi_usuario_ejecutor")+"') AND DECODE('"+m.get("nombre_usuario_ejecutor")+"', 'null', '*', NOMBRE_USUARIO_EJECUTOR) = DECODE('"+m.get("nombre_usuario_ejecutor")+"', 'null', '*', '"+m.get("nombre_usuario_ejecutor")+"') AND DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', FECHA_PLAN_INI) = DECODE('"+m.get("fecha_plan_ini")+"', 'null', '*', to_date('"+m.get("fecha_plan_ini")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', FECHA_PLAN_FIN) = DECODE('"+m.get("fecha_plan_fin")+"', 'null', '*', to_date('"+m.get("fecha_plan_fin")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', FECHA_REAL_INI) = DECODE('"+m.get("fecha_real_ini")+"', 'null', '*', to_date('"+m.get("fecha_real_ini")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', FECHA_REAL_FIN) = DECODE('"+m.get("fecha_real_fin")+"', 'null', '*', to_date('"+m.get("fecha_real_fin")+"', 'dd/mm/yyyy')) AND DECODE('"+m.get("fecha_plan_ini_fmt")+"', 'null', '*', FECHA_PLAN_INI_FMT) = DECODE('"+m.get("fecha_plan_ini_fmt")+"', 'null', '*', '"+m.get("fecha_plan_ini_fmt")+"') AND DECODE('"+m.get("fecha_plan_fin_fmt")+"', 'null', '*', FECHA_PLAN_FIN_FMT) = DECODE('"+m.get("fecha_plan_fin_fmt")+"', 'null', '*', '"+m.get("fecha_plan_fin_fmt")+"') AND DECODE('"+m.get("fecha_real_ini_fmt")+"', 'null', '*', FECHA_REAL_INI_FMT) = DECODE('"+m.get("fecha_real_ini_fmt")+"', 'null', '*', '"+m.get("fecha_real_ini_fmt")+"') AND DECODE('"+m.get("fecha_real_fin_fmt")+"', 'null', '*', FECHA_REAL_FIN_FMT) = DECODE('"+m.get("fecha_real_fin_fmt")+"', 'null', '*', '"+m.get("fecha_real_fin_fmt")+"') AND DECODE('"+m.get("fecha_modificacion_fmt")+"', 'null', '*', FECHA_MODIFICACION_FMT) = DECODE('"+m.get("fecha_modificacion_fmt")+"', 'null', '*', '"+m.get("fecha_modificacion_fmt")+"') AND DECODE('"+m.get("nombre_usuario_modificacion")+"', 'null', '*', NOMBRE_USUARIO_MODIFICACION) = DECODE('"+m.get("nombre_usuario_modificacion")+"', 'null', '*', '"+m.get("nombre_usuario_modificacion")+"') AND DECODE('"+m.get("id_tarea_padre")+"', 'null', '*', ID_TAREA_PADRE) = DECODE('"+m.get("id_tarea_padre")+"', 'null', '*', "+m.get("id_tarea_padre")+") AND DECODE('"+m.get("id_tarea_hijo")+"', 'null', '*', ID_TAREA_HIJO) = DECODE('"+m.get("id_tarea_hijo")+"', 'null', '*', "+m.get("id_tarea_hijo")+") AND DECODE('"+m.get("nombre_tarea_hijo")+"', 'null', '*', NOMBRE_TAREA_HIJO) = DECODE('"+m.get("nombre_tarea_hijo")+"', 'null', '*', '"+m.get("nombre_tarea_hijo")+"') AND DECODE('"+m.get("tipo_tarea_negocio_hijo")+"', 'null', '*', TIPO_TAREA_NEGOCIO_HIJO) = DECODE('"+m.get("tipo_tarea_negocio_hijo")+"', 'null', '*', '"+m.get("tipo_tarea_negocio_hijo")+"') AND DECODE('"+m.get("id_tipo_tarea_hijo")+"', 'null', '*', ID_TIPO_TAREA_HIJO) = DECODE('"+m.get("id_tipo_tarea_hijo")+"', 'null', '*', "+m.get("id_tipo_tarea_hijo")+") AND DECODE('"+m.get("codi_tarea_hijo")+"', 'null', '*', CODI_TAREA_HIJO) = DECODE('"+m.get("codi_tarea_hijo")+"', 'null', '*', '"+m.get("codi_tarea_hijo")+"') AND DECODE('"+m.get("id_estado_hijo")+"', 'null', '*', ID_ESTADO_HIJO) = DECODE('"+m.get("id_estado_hijo")+"', 'null', '*', "+m.get("id_estado_hijo")+")");

            response.setHeader("access-control-allow-origin", "*");
            out.print(json);

        } catch (SQLException e) {
            //System.out.println("Connection Failed! Check output console");
            logger.error("Error durante la consulta a la base de datos. "+e.getMessage());
            //e.printStackTrace();
        } finally {
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
