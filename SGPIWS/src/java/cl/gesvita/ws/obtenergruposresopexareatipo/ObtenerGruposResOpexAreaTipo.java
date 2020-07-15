package cl.gesvita.ws.obtenergruposresopexareatipo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

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
 * Servlet implementation class ObtenerGruposResOpexAreaTipo
 */
@WebServlet("/WSObtenerGruposResOpexAreaTipo/ObtenerGruposResOpexAreaTipo")
public class ObtenerGruposResOpexAreaTipo extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerGruposResOpexAreaTipo.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerGruposResOpexAreaTipo() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String requestS;

        PrintWriter out = response.getWriter();
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace(System.err);
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

        Map<String, Object> dataIndividual;
        Map<String, Object> dataGrupal;
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
            String[] camposytipos = {"ID_EMPRESA", "I", "ID_TIPO", "I", "DESCRIPCION_TIPO", "S", "ID_POSICION_RELEVANCIA", "I", "ID_AREA", "I", "NOMBRE_AREA", "S", "CANTIDAD_PROYECTOS_AREA", "I", "KPI_CANTIDAD_PROYECTOS", "I", "SLA_TIEMPO_RESPUESTA", "I", "DESC_SLA_TIEMPO_RESPUESTA", "S", "KPI_AREA_AVANCE_PTJE_CUMPLE", "I", "KPI_AREA_AVANCE_PTJE_ENRIESGO", "I", "KPI_AREA_AVANCE_PTJE_NOCUMPLE", "I", "KPI_AREA_SLA_PTJE_CUMPLE", "I", "KPI_AREA_SLA_PTJE_ENRIESGO", "I", "KPI_AREA_SLA_PTJE_NOCUMPLE", "I", "NOMBRE_JEFEAREA", "S", "CODI_USUARIO_JEFEAREA", "S", "ID_AREA_PADRE", "I", "NOMBRE_AREA_PADRE", "S", "NOMBRE_JEFEAREAPADRE", "S", "CODI_USUARIO_JEFEAREAPADRE", "S", "ID_TIPO_GRUPO", "I", "DESCRIPCION_TIPO_GRUPO", "S", "CANTIDAD_GRUPOS_TIPO", "I", "CANTIDAD_PROYECTOS_TIPO", "I", "PORCENTAJE_ASIGNACION_POND", "I", "KPI_ASIGNACION_ESTADO", "I"};

            // Construir el SQL
            StringSql.append("SELECT * FROM VW_GRUPOS_RESOPEXAREATIPO");
            String sqlWhereAnd = " WHERE ";
            for (int i = 0; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    if (camposytipos[i + 1].charAt(0) == 'D') {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = to_date( ? , 'dd/mm/yyyy')");
                    } else {
                        StringSql.append(sqlWhereAnd + camposytipos[i] + " = ?");
                    }
                    sqlWhereAnd = " AND ";
                }
            }
            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement(StringSql.toString());
            for (int i = 0, j = 1; i < camposytipos.length; i += 2) {
                if (m.containsKey(camposytipos[i].toLowerCase())) {
                    switch (camposytipos[i + 1].charAt(0)) {
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
                            stmt.setString(j, val);
                            break;
                    }
                    j++;
                }
            }

            rs = stmt.executeQuery();
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_GRUPOS_RESOPEXAREATIPO WHERE "+
//"DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("id_tipo")+"', 'null', '*', ID_TIPO) = DECODE('"+m.get("id_tipo")+"', 'null', '*', "+m.get("id_tipo")+") AND DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', DESCRIPCION_TIPO) = DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', '"+m.get("descripcion_tipo")+"') AND DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', ID_POSICION_RELEVANCIA) = DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', "+m.get("id_posicion_relevancia")+") AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("cantidad_proyectos_area")+"', 'null', '*', CANTIDAD_PROYECTOS_AREA) = DECODE('"+m.get("cantidad_proyectos_area")+"', 'null', '*', "+m.get("cantidad_proyectos_area")+") AND DECODE('"+m.get("kpi_cantidad_proyectos")+"', 'null', '*', KPI_CANTIDAD_PROYECTOS) = DECODE('"+m.get("kpi_cantidad_proyectos")+"', 'null', '*', "+m.get("kpi_cantidad_proyectos")+") AND DECODE('"+m.get("sla_tiempo_respuesta")+"', 'null', '*', SLA_TIEMPO_RESPUESTA) = DECODE('"+m.get("sla_tiempo_respuesta")+"', 'null', '*', "+m.get("sla_tiempo_respuesta")+") AND DECODE('"+m.get("desc_sla_tiempo_respuesta")+"', 'null', '*', DESC_SLA_TIEMPO_RESPUESTA) = DECODE('"+m.get("desc_sla_tiempo_respuesta")+"', 'null', '*', '"+m.get("desc_sla_tiempo_respuesta")+"') AND DECODE('"+m.get("kpi_area_avance_ptje_cumple")+"', 'null', '*', KPI_AREA_AVANCE_PTJE_CUMPLE) = DECODE('"+m.get("kpi_area_avance_ptje_cumple")+"', 'null', '*', "+m.get("kpi_area_avance_ptje_cumple")+") AND DECODE('"+m.get("kpi_area_avance_ptje_enriesgo")+"', 'null', '*', KPI_AREA_AVANCE_PTJE_ENRIESGO) = DECODE('"+m.get("kpi_area_avance_ptje_enriesgo")+"', 'null', '*', "+m.get("kpi_area_avance_ptje_enriesgo")+") AND DECODE('"+m.get("kpi_area_avance_ptje_nocumple")+"', 'null', '*', KPI_AREA_AVANCE_PTJE_NOCUMPLE) = DECODE('"+m.get("kpi_area_avance_ptje_nocumple")+"', 'null', '*', "+m.get("kpi_area_avance_ptje_nocumple")+") AND DECODE('"+m.get("kpi_area_sla_ptje_cumple")+"', 'null', '*', KPI_AREA_SLA_PTJE_CUMPLE) = DECODE('"+m.get("kpi_area_sla_ptje_cumple")+"', 'null', '*', "+m.get("kpi_area_sla_ptje_cumple")+") AND DECODE('"+m.get("kpi_area_sla_ptje_enriesgo")+"', 'null', '*', KPI_AREA_SLA_PTJE_ENRIESGO) = DECODE('"+m.get("kpi_area_sla_ptje_enriesgo")+"', 'null', '*', "+m.get("kpi_area_sla_ptje_enriesgo")+") AND DECODE('"+m.get("kpi_area_sla_ptje_nocumple")+"', 'null', '*', KPI_AREA_SLA_PTJE_NOCUMPLE) = DECODE('"+m.get("kpi_area_sla_ptje_nocumple")+"', 'null', '*', "+m.get("kpi_area_sla_ptje_nocumple")+") AND DECODE('"+m.get("nombre_jefearea")+"', 'null', '*', NOMBRE_JEFEAREA) = DECODE('"+m.get("nombre_jefearea")+"', 'null', '*', '"+m.get("nombre_jefearea")+"') AND DECODE('"+m.get("codi_usuario_jefearea")+"', 'null', '*', CODI_USUARIO_JEFEAREA) = DECODE('"+m.get("codi_usuario_jefearea")+"', 'null', '*', '"+m.get("codi_usuario_jefearea")+"') AND DECODE('"+m.get("id_area_padre")+"', 'null', '*', ID_AREA_PADRE) = DECODE('"+m.get("id_area_padre")+"', 'null', '*', "+m.get("id_area_padre")+") AND DECODE('"+m.get("nombre_area_padre")+"', 'null', '*', NOMBRE_AREA_PADRE) = DECODE('"+m.get("nombre_area_padre")+"', 'null', '*', '"+m.get("nombre_area_padre")+"') AND DECODE('"+m.get("nombre_jefeareapadre")+"', 'null', '*', NOMBRE_JEFEAREAPADRE) = DECODE('"+m.get("nombre_jefeareapadre")+"', 'null', '*', '"+m.get("nombre_jefeareapadre")+"') AND DECODE('"+m.get("codi_usuario_jefeareapadre")+"', 'null', '*', CODI_USUARIO_JEFEAREAPADRE) = DECODE('"+m.get("codi_usuario_jefeareapadre")+"', 'null', '*', '"+m.get("codi_usuario_jefeareapadre")+"') AND DECODE('"+m.get("id_tipo_grupo")+"', 'null', '*', ID_TIPO_GRUPO) = DECODE('"+m.get("id_tipo_grupo")+"', 'null', '*', "+m.get("id_tipo_grupo")+") AND DECODE('"+m.get("descripcion_tipo_grupo")+"', 'null', '*', DESCRIPCION_TIPO_GRUPO) = DECODE('"+m.get("descripcion_tipo_grupo")+"', 'null', '*', '"+m.get("descripcion_tipo_grupo")+"') AND DECODE('"+m.get("cantidad_grupos_tipo")+"', 'null', '*', CANTIDAD_GRUPOS_TIPO) = DECODE('"+m.get("cantidad_grupos_tipo")+"', 'null', '*', "+m.get("cantidad_grupos_tipo")+") AND DECODE('"+m.get("cantidad_proyectos_tipo")+"', 'null', '*', CANTIDAD_PROYECTOS_TIPO) = DECODE('"+m.get("cantidad_proyectos_tipo")+"', 'null', '*', "+m.get("cantidad_proyectos_tipo")+") AND DECODE('"+m.get("porcentaje_asignacion_pond")+"', 'null', '*', PORCENTAJE_ASIGNACION_POND) = DECODE('"+m.get("porcentaje_asignacion_pond")+"', 'null', '*', "+m.get("porcentaje_asignacion_pond")+") AND DECODE('"+m.get("kpi_asignacion_estado")+"', 'null', '*', KPI_ASIGNACION_ESTADO) = DECODE('"+m.get("kpi_asignacion_estado")+"', 'null', '*', "+m.get("kpi_asignacion_estado")+")");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next()) {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
                dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
                dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
                dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
                dataIndividual.put("ID_POSICION_RELEVANCIA", rs.getString("ID_POSICION_RELEVANCIA"));
                dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
                dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
                dataIndividual.put("CANTIDAD_PROYECTOS_AREA", rs.getString("CANTIDAD_PROYECTOS_AREA"));
                dataIndividual.put("KPI_CANTIDAD_PROYECTOS", rs.getString("KPI_CANTIDAD_PROYECTOS"));
                dataIndividual.put("SLA_TIEMPO_RESPUESTA", rs.getString("SLA_TIEMPO_RESPUESTA"));
                dataIndividual.put("DESC_SLA_TIEMPO_RESPUESTA", rs.getString("DESC_SLA_TIEMPO_RESPUESTA"));
                dataIndividual.put("KPI_AREA_AVANCE_PTJE_CUMPLE", rs.getString("KPI_AREA_AVANCE_PTJE_CUMPLE"));
                dataIndividual.put("KPI_AREA_AVANCE_PTJE_ENRIESGO", rs.getString("KPI_AREA_AVANCE_PTJE_ENRIESGO"));
                dataIndividual.put("KPI_AREA_AVANCE_PTJE_NOCUMPLE", rs.getString("KPI_AREA_AVANCE_PTJE_NOCUMPLE"));
                dataIndividual.put("KPI_AREA_SLA_PTJE_CUMPLE", rs.getString("KPI_AREA_SLA_PTJE_CUMPLE"));
                dataIndividual.put("KPI_AREA_SLA_PTJE_ENRIESGO", rs.getString("KPI_AREA_SLA_PTJE_ENRIESGO"));
                dataIndividual.put("KPI_AREA_SLA_PTJE_NOCUMPLE", rs.getString("KPI_AREA_SLA_PTJE_NOCUMPLE"));
                dataIndividual.put("NOMBRE_JEFEAREA", rs.getString("NOMBRE_JEFEAREA"));
                dataIndividual.put("CODI_USUARIO_JEFEAREA", rs.getString("CODI_USUARIO_JEFEAREA"));
                dataIndividual.put("ID_AREA_PADRE", rs.getString("ID_AREA_PADRE"));
                dataIndividual.put("NOMBRE_AREA_PADRE", rs.getString("NOMBRE_AREA_PADRE"));
                dataIndividual.put("NOMBRE_JEFEAREAPADRE", rs.getString("NOMBRE_JEFEAREAPADRE"));
                dataIndividual.put("CODI_USUARIO_JEFEAREAPADRE", rs.getString("CODI_USUARIO_JEFEAREAPADRE"));
                dataIndividual.put("ID_TIPO_GRUPO", rs.getString("ID_TIPO_GRUPO"));
                dataIndividual.put("DESCRIPCION_TIPO_GRUPO", rs.getString("DESCRIPCION_TIPO_GRUPO"));
                dataIndividual.put("CANTIDAD_GRUPOS_TIPO", rs.getString("CANTIDAD_GRUPOS_TIPO"));
                dataIndividual.put("CANTIDAD_PROYECTOS_TIPO", rs.getString("CANTIDAD_PROYECTOS_TIPO"));
                dataIndividual.put("PORCENTAJE_ASIGNACION_POND", rs.getString("PORCENTAJE_ASIGNACION_POND"));
                dataIndividual.put("KPI_ASIGNACION_ESTADO", rs.getString("KPI_ASIGNACION_ESTADO"));
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
