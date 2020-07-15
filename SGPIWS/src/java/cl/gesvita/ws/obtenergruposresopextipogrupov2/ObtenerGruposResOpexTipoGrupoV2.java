package cl.gesvita.ws.obtenergruposresopextipogrupov2;

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
 * Servlet implementation class ObtenerGruposResOpexTipoGrupoV2
 */
@WebServlet("/WSObtenerGruposResOpexTipoGrupoV2/ObtenerGruposResOpexTipoGrupoV2")
public class ObtenerGruposResOpexTipoGrupoV2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(ObtenerGruposResOpexTipoGrupoV2.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObtenerGruposResOpexTipoGrupoV2() {
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
            String[] camposytipos = { "ID_EMPRESA","I","ID_TIPO","I","DESCRIPCION_TIPO","S","ID_POSICION_RELEVANCIA","I","ID_AREA","I","NOMBRE_AREA","S","CANTIDAD_PROYECTOS_TIPOGRUPO","I","CANTIDAD_GRUPOS_TIPOGRUPO","I","KPI_CANTIDAD_PROYECTOS","I","SLA_TIEMPO_RESPUESTA","I","DESC_SLA_TIEMPO_RESPUESTA","S","NOMBRE_JEFEAREA","S","CODI_USUARIO_JEFEAREA","S","ID_AREA_PADRE","I","NOMBRE_AREA_PADRE","S","NOMBRE_JEFEAREAPADRE","S","CODI_USUARIO_JEFEAREAPADRE","S","ID_TIPO_GRUPO","I","DESCRIPCION_TIPO_GRUPO","S","ID_GRUPO","I","CODI_GRUPO","S","DESCRIPCION_GRUPO","S","CANTIDAD_SUCURSALES_GRUPO","I","CANTIDAD_PROYECTOS_GRUPO","I","KPI_GRUPO_SLA_PTJE_CUMPLE","I","KPI_GRUPO_SLA_PTJE_ENRIESGO","I","KPI_GRUPO_SLA_PTJE_NOCUMPLE","I","KPI_GRUPO_AVANCE_PTJE_CUMPLE","I","KPI_GRUPO_AVANCE_PTJE_ENRIESGO","I","KPI_GRUPO_AVANCE_PTJE_NOCUMPLE","I" };
            
            // Construir el SQL
            StringSql.append("SELECT * FROM VW_GRUPOS_RESOPEXTIPOGRUPO avtar");
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

            StringSql.append(sqlWhereAnd + " ("); //PPAL INI
            StringSql.append(" exists ");
            StringSql.append("( ");
// 1.-- Check de Usuario participante en Mesa
            StringSql.append(" select usuario.id_usuario, usuario.id_area,ugrup.codi_grupo ");
            StringSql.append(" from vw_seguridad_usuario usuario, VW_SEGURIDAD_USERSGRUPO ugrup, vw_seguridad_grupo grupo, tb_grupo_tipo grtipo ");
            StringSql.append(" where usuario.id_usuario = ? ");
            StringSql.append(" and grupo.id_tipo_grupo = grtipo.id_tipo_grupo ");
            StringSql.append(" and  grtipo.NOMBRE_TIPO = 'MESADETRABAJO' ");
            StringSql.append(" and grupo.codi_grupo = ugrup.codi_grupo ");
            StringSql.append(" and ugrup.id_usuario  = usuario.id_usuario ");
            StringSql.append(" and avtar.id_area = grupo.id_area_grupo ");
            StringSql.append(" and avtar.codi_grupo = grupo.codi_grupo ");

            StringSql.append(" )");

            StringSql.append(" or ");
            StringSql.append(" exists ");
            StringSql.append("( ");
            // 2.-- Check de Usuario "Jefes" con participantes de su Área en Mesas        
            StringSql.append(" select usuario.id_usuario, usuario.id_area,ugrup.codi_grupo  ");
            StringSql.append(" from vw_seguridad_usuario usuario, VW_SEGURIDAD_USERSGRUPO ugrup, vw_seguridad_usuario usuarioGRdet, vw_seguridad_grupo grupo, tb_grupo_tipo grtipo  ");
            StringSql.append(" where usuario.id_usuario = ? ");
            StringSql.append(" and usuario.ID_ROL in (11,2,10)  ");
            StringSql.append("  and grupo.id_tipo_grupo = grtipo.id_tipo_grupo  ");
            StringSql.append("  and  grtipo.NOMBRE_TIPO = 'MESADETRABAJO'  ");
            StringSql.append("  and usuario.id_area = usuarioGRdet.id_area ");
            StringSql.append("  and grupo.codi_grupo = ugrup.codi_grupo ");
            StringSql.append("  and ugrup.id_usuario  = usuarioGRdet.id_usuario ");
            StringSql.append(" and avtar.id_area = grupo.id_area_grupo ");

            StringSql.append(" )");

            StringSql.append(" or ");
            StringSql.append(" exists ");
            StringSql.append("( ");
// 3. --Check de Usuario "Jefes" con participantes de sus SubÁrea en Mesas
            StringSql.append(" select usuario.id_usuario, usuario.id_area,ugrup.codi_grupo  ");
            StringSql.append(" from vw_seguridad_usuario usuario, VW_SEGURIDAD_USERSGRUPO ugrup, vw_seguridad_usuario usuarioGRdet, vw_seguridad_grupo grupo, tb_grupo_tipo grtipo  ");
            StringSql.append("  , tb_proyecto_area area ");
            StringSql.append(" where usuario.id_usuario = ?  ");
            StringSql.append("  and usuario.ID_ROL in (11,2,10)  ");
            StringSql.append("   and grupo.id_tipo_grupo = grtipo.id_tipo_grupo ");
            StringSql.append("   and  grtipo.NOMBRE_TIPO = 'MESADETRABAJO' ");
            StringSql.append("   and  usuario.id_area = area.id_area_padre ");
            StringSql.append("   and area.id_area = usuarioGRdet.id_area ");
            StringSql.append("   and grupo.codi_grupo = ugrup.codi_grupo ");
            StringSql.append("   and ugrup.id_usuario  = usuarioGRdet.id_usuario ");
            StringSql.append("   and avtar.id_area = grupo.id_area_grupo ");

            StringSql.append(" )");

            StringSql.append(" or ");
            StringSql.append(" exists ");
            // 4. --Check perfil gerente
            StringSql.append("(select 1 from vw_seguridad_usuario usuario");
            StringSql.append(" where usuario.id_usuario = ?");
            StringSql.append(" and usuario.ID_ROL = 10)");


            StringSql.append(" )"); //PPAL FIN



            // BYSECURITY logger.debug("Query NUEVA: " + StringSql.toString());

            stmt = connection.prepareStatement( StringSql.toString() );
            int j = 1;
            for (int i = 0; i < camposytipos.length; i+=2) {
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
            
            int intVal = Integer.parseInt(m.get("id_usuario"));
            stmt.setInt(j, intVal);
            stmt.setInt(j + 1, intVal);
            stmt.setInt(j + 2, intVal);
            stmt.setInt(j + 3, intVal);

            rs = stmt.executeQuery();
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery("SELECT * FROM VW_GRUPOS_RESOPEXTIPOGRUPO WHERE "+
//"DECODE('"+m.get("id_empresa")+"', 'null', '*', ID_EMPRESA) = DECODE('"+m.get("id_empresa")+"', 'null', '*', "+m.get("id_empresa")+") AND DECODE('"+m.get("id_tipo")+"', 'null', '*', ID_TIPO) = DECODE('"+m.get("id_tipo")+"', 'null', '*', "+m.get("id_tipo")+") AND DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', DESCRIPCION_TIPO) = DECODE('"+m.get("descripcion_tipo")+"', 'null', '*', '"+m.get("descripcion_tipo")+"') AND DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', ID_POSICION_RELEVANCIA) = DECODE('"+m.get("id_posicion_relevancia")+"', 'null', '*', "+m.get("id_posicion_relevancia")+") AND DECODE('"+m.get("id_area")+"', 'null', '*', ID_AREA) = DECODE('"+m.get("id_area")+"', 'null', '*', "+m.get("id_area")+") AND DECODE('"+m.get("nombre_area")+"', 'null', '*', NOMBRE_AREA) = DECODE('"+m.get("nombre_area")+"', 'null', '*', '"+m.get("nombre_area")+"') AND DECODE('"+m.get("cantidad_proyectos_tipogrupo")+"', 'null', '*', CANTIDAD_PROYECTOS_TIPOGRUPO) = DECODE('"+m.get("cantidad_proyectos_tipogrupo")+"', 'null', '*', "+m.get("cantidad_proyectos_tipogrupo")+") AND DECODE('"+m.get("cantidad_grupos_tipogrupo")+"', 'null', '*', CANTIDAD_GRUPOS_TIPOGRUPO) = DECODE('"+m.get("cantidad_grupos_tipogrupo")+"', 'null', '*', "+m.get("cantidad_grupos_tipogrupo")+") AND DECODE('"+m.get("kpi_cantidad_proyectos")+"', 'null', '*', KPI_CANTIDAD_PROYECTOS) = DECODE('"+m.get("kpi_cantidad_proyectos")+"', 'null', '*', "+m.get("kpi_cantidad_proyectos")+") AND DECODE('"+m.get("sla_tiempo_respuesta")+"', 'null', '*', SLA_TIEMPO_RESPUESTA) = DECODE('"+m.get("sla_tiempo_respuesta")+"', 'null', '*', "+m.get("sla_tiempo_respuesta")+") AND DECODE('"+m.get("desc_sla_tiempo_respuesta")+"', 'null', '*', DESC_SLA_TIEMPO_RESPUESTA) = DECODE('"+m.get("desc_sla_tiempo_respuesta")+"', 'null', '*', '"+m.get("desc_sla_tiempo_respuesta")+"') AND DECODE('"+m.get("nombre_jefearea")+"', 'null', '*', NOMBRE_JEFEAREA) = DECODE('"+m.get("nombre_jefearea")+"', 'null', '*', '"+m.get("nombre_jefearea")+"') AND DECODE('"+m.get("codi_usuario_jefearea")+"', 'null', '*', CODI_USUARIO_JEFEAREA) = DECODE('"+m.get("codi_usuario_jefearea")+"', 'null', '*', '"+m.get("codi_usuario_jefearea")+"') AND DECODE('"+m.get("id_area_padre")+"', 'null', '*', ID_AREA_PADRE) = DECODE('"+m.get("id_area_padre")+"', 'null', '*', "+m.get("id_area_padre")+") AND DECODE('"+m.get("nombre_area_padre")+"', 'null', '*', NOMBRE_AREA_PADRE) = DECODE('"+m.get("nombre_area_padre")+"', 'null', '*', '"+m.get("nombre_area_padre")+"') AND DECODE('"+m.get("nombre_jefeareapadre")+"', 'null', '*', NOMBRE_JEFEAREAPADRE) = DECODE('"+m.get("nombre_jefeareapadre")+"', 'null', '*', '"+m.get("nombre_jefeareapadre")+"') AND DECODE('"+m.get("codi_usuario_jefeareapadre")+"', 'null', '*', CODI_USUARIO_JEFEAREAPADRE) = DECODE('"+m.get("codi_usuario_jefeareapadre")+"', 'null', '*', '"+m.get("codi_usuario_jefeareapadre")+"') AND DECODE('"+m.get("id_tipo_grupo")+"', 'null', '*', ID_TIPO_GRUPO) = DECODE('"+m.get("id_tipo_grupo")+"', 'null', '*', "+m.get("id_tipo_grupo")+") AND DECODE('"+m.get("descripcion_tipo_grupo")+"', 'null', '*', DESCRIPCION_TIPO_GRUPO) = DECODE('"+m.get("descripcion_tipo_grupo")+"', 'null', '*', '"+m.get("descripcion_tipo_grupo")+"') AND DECODE('"+m.get("id_grupo")+"', 'null', '*', ID_GRUPO) = DECODE('"+m.get("id_grupo")+"', 'null', '*', "+m.get("id_grupo")+") AND DECODE('"+m.get("codi_grupo")+"', 'null', '*', CODI_GRUPO) = DECODE('"+m.get("codi_grupo")+"', 'null', '*', '"+m.get("codi_grupo")+"') AND DECODE('"+m.get("descripcion_grupo")+"', 'null', '*', DESCRIPCION_GRUPO) = DECODE('"+m.get("descripcion_grupo")+"', 'null', '*', '"+m.get("descripcion_grupo")+"') AND DECODE('"+m.get("cantidad_sucursales_grupo")+"', 'null', '*', CANTIDAD_SUCURSALES_GRUPO) = DECODE('"+m.get("cantidad_sucursales_grupo")+"', 'null', '*', "+m.get("cantidad_sucursales_grupo")+") AND DECODE('"+m.get("cantidad_proyectos_grupo")+"', 'null', '*', CANTIDAD_PROYECTOS_GRUPO) = DECODE('"+m.get("cantidad_proyectos_grupo")+"', 'null', '*', "+m.get("cantidad_proyectos_grupo")+") AND DECODE('"+m.get("kpi_grupo_sla_ptje_cumple")+"', 'null', '*', KPI_GRUPO_SLA_PTJE_CUMPLE) = DECODE('"+m.get("kpi_grupo_sla_ptje_cumple")+"', 'null', '*', "+m.get("kpi_grupo_sla_ptje_cumple")+") AND DECODE('"+m.get("kpi_grupo_sla_ptje_enriesgo")+"', 'null', '*', KPI_GRUPO_SLA_PTJE_ENRIESGO) = DECODE('"+m.get("kpi_grupo_sla_ptje_enriesgo")+"', 'null', '*', "+m.get("kpi_grupo_sla_ptje_enriesgo")+") AND DECODE('"+m.get("kpi_grupo_sla_ptje_nocumple")+"', 'null', '*', KPI_GRUPO_SLA_PTJE_NOCUMPLE) = DECODE('"+m.get("kpi_grupo_sla_ptje_nocumple")+"', 'null', '*', "+m.get("kpi_grupo_sla_ptje_nocumple")+") AND DECODE('"+m.get("kpi_grupo_avance_ptje_cumple")+"', 'null', '*', KPI_GRUPO_AVANCE_PTJE_CUMPLE) = DECODE('"+m.get("kpi_grupo_avance_ptje_cumple")+"', 'null', '*', "+m.get("kpi_grupo_avance_ptje_cumple")+") AND DECODE('"+m.get("kpi_grupo_avance_ptje_enriesgo")+"', 'null', '*', KPI_GRUPO_AVANCE_PTJE_ENRIESGO) = DECODE('"+m.get("kpi_grupo_avance_ptje_enriesgo")+"', 'null', '*', "+m.get("kpi_grupo_avance_ptje_enriesgo")+") AND DECODE('"+m.get("kpi_grupo_avance_ptje_nocumple")+"', 'null', '*', KPI_GRUPO_AVANCE_PTJE_NOCUMPLE) = DECODE('"+m.get("kpi_grupo_avance_ptje_nocumple")+"', 'null', '*', "+m.get("kpi_grupo_avance_ptje_nocumple")+")");

            dataGrupal = new HashMap<String, Object>();
            while (rs.next())
            {
                totalRegistro++;
                dataIndividual = new HashMap<String, Object>();
               dataIndividual.put("ID_EMPRESA", rs.getString("ID_EMPRESA"));
               dataIndividual.put("ID_TIPO", rs.getString("ID_TIPO"));
               dataIndividual.put("DESCRIPCION_TIPO", rs.getString("DESCRIPCION_TIPO"));
               dataIndividual.put("ID_POSICION_RELEVANCIA", rs.getString("ID_POSICION_RELEVANCIA"));
               dataIndividual.put("ID_AREA", rs.getString("ID_AREA"));
               dataIndividual.put("NOMBRE_AREA", rs.getString("NOMBRE_AREA"));
               dataIndividual.put("CANTIDAD_PROYECTOS_TIPOGRUPO", rs.getString("CANTIDAD_PROYECTOS_TIPOGRUPO"));
               dataIndividual.put("CANTIDAD_GRUPOS_TIPOGRUPO", rs.getString("CANTIDAD_GRUPOS_TIPOGRUPO"));
               dataIndividual.put("KPI_CANTIDAD_PROYECTOS", rs.getString("KPI_CANTIDAD_PROYECTOS"));
               dataIndividual.put("SLA_TIEMPO_RESPUESTA", rs.getString("SLA_TIEMPO_RESPUESTA"));
               dataIndividual.put("DESC_SLA_TIEMPO_RESPUESTA", rs.getString("DESC_SLA_TIEMPO_RESPUESTA"));
               dataIndividual.put("NOMBRE_JEFEAREA", rs.getString("NOMBRE_JEFEAREA"));
               dataIndividual.put("CODI_USUARIO_JEFEAREA", rs.getString("CODI_USUARIO_JEFEAREA"));
               dataIndividual.put("ID_AREA_PADRE", rs.getString("ID_AREA_PADRE"));
               dataIndividual.put("NOMBRE_AREA_PADRE", rs.getString("NOMBRE_AREA_PADRE"));
               dataIndividual.put("NOMBRE_JEFEAREAPADRE", rs.getString("NOMBRE_JEFEAREAPADRE"));
               dataIndividual.put("CODI_USUARIO_JEFEAREAPADRE", rs.getString("CODI_USUARIO_JEFEAREAPADRE"));
               dataIndividual.put("ID_TIPO_GRUPO", rs.getString("ID_TIPO_GRUPO"));
               dataIndividual.put("DESCRIPCION_TIPO_GRUPO", rs.getString("DESCRIPCION_TIPO_GRUPO"));
               dataIndividual.put("ID_GRUPO", rs.getString("ID_GRUPO"));
               dataIndividual.put("CODI_GRUPO", rs.getString("CODI_GRUPO"));
               dataIndividual.put("DESCRIPCION_GRUPO", rs.getString("DESCRIPCION_GRUPO"));
               dataIndividual.put("CANTIDAD_SUCURSALES_GRUPO", rs.getString("CANTIDAD_SUCURSALES_GRUPO"));
               dataIndividual.put("CANTIDAD_PROYECTOS_GRUPO", rs.getString("CANTIDAD_PROYECTOS_GRUPO"));
               dataIndividual.put("KPI_GRUPO_SLA_PTJE_CUMPLE", rs.getString("KPI_GRUPO_SLA_PTJE_CUMPLE"));
               dataIndividual.put("KPI_GRUPO_SLA_PTJE_ENRIESGO", rs.getString("KPI_GRUPO_SLA_PTJE_ENRIESGO"));
               dataIndividual.put("KPI_GRUPO_SLA_PTJE_NOCUMPLE", rs.getString("KPI_GRUPO_SLA_PTJE_NOCUMPLE"));
               dataIndividual.put("KPI_GRUPO_AVANCE_PTJE_CUMPLE", rs.getString("KPI_GRUPO_AVANCE_PTJE_CUMPLE"));
               dataIndividual.put("KPI_GRUPO_AVANCE_PTJE_ENRIESGO", rs.getString("KPI_GRUPO_AVANCE_PTJE_ENRIESGO"));
               dataIndividual.put("KPI_GRUPO_AVANCE_PTJE_NOCUMPLE", rs.getString("KPI_GRUPO_AVANCE_PTJE_NOCUMPLE"));
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