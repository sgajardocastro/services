package cl.gesvita.ws.subirganttfromproject;

import cl.gesvita.ws.subirganttfromproject.bean.TaskAndProp;
import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.obtener.bean.TipoParametro;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import cl.gesvita.ws.subirarchivoshp.util.BinaryFiles;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.mpxj.MPXJException;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Task;
import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.UniversalProjectReader;

/**
 * Servlet implementation class SubirGanttFromProject
 */
@WebServlet("/WSSubirGanttFromProject/SubirGanttFromProject")
public class SubirGanttFromProject extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(SubirGanttFromProject.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirGanttFromProject() {
        super();
    }

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        TipoParametro CAMPOSCOMUNES[] = {
            new TipoParametro("in_id_cache","ID CACHE","I",null,true),
            new TipoParametro("in_id_proyecto","ID PROYECTO","I",null,true),
            new TipoParametro("in_id_usuario","ID USUARIO","I",null,true)
        };
        Map<String,TipoParametro> listaParametros;
        String stringPatrones[] = { "F","^[\\d]+\\.[\\d]+$"
                ,"I","^[\\d\\,]+$"
                ,"D","^([012]?[1-9]|3[01])(\\/|-)([0]?[1-9]|[1][0-2])\\2(\\d{4})$"
                ,"P","^(100|(\\d{1,2})%)$"};
        Map<String,Pattern> listaPatrones;
        Connection conn = null;

        Map<String, Object> dataRetornar = new HashMap<>();
        Gson gson = new Gson();

        response.setContentType("application/json");

        String etapa = "Obtener Conexión";
        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj,jsonObjResp;

        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
            // Generar lista de patrones
            listaPatrones = generarPatrones(stringPatrones);
            // Generar mapa parámetros
            listaParametros = generarMapaParametros(CAMPOSCOMUNES);
            // Obtener Data Source
            datasource = ObtenerLib.getDataSource(logger);
            // BYSECURITY logger.info("Datasource obtenido");
        } catch (GesvitaException ex) {
            logger.error(ex.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Error en conexión a BD");
            sendData(response, out, gson, dataRetornar);
            return;
        }
        try {
            etapa = "Leer entrada request";
            // Leer la t_hija excel
            jsonObj = ObtenerLib.readInput(logger, request);
            // BYSECURITY
            logger.info("input:"  +jsonObj.toString());
            
            etapa = "Getting connection";
            conn = datasource.getConnection();
            
            etapa = "Leer project de BD";
            ProjectFile project = LeerExcel4DB(conn, jsonObj,listaParametros, listaPatrones);
    
            if (project == null) {
                throw new GesvitaException("No se seleccionó archivo project o el formato no corresponde (1)");
            }
            etapa = "Guardar en BD las tareas";
            jsonObjResp= guardarEnBDProject(logger, conn, jsonObj
                , listaParametros , listaPatrones , project);
            
            sendData(response, out, jsonObjResp);
        } catch (GesvitaException ex) {
            logger.error(etapa);
            logger.error(ex.getMessage());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", ex.getMessage());
            sendData(response, out, gson, dataRetornar);
        } catch (Exception e) {
            logger.error(etapa);
            logger.error(e.fillInStackTrace());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9997);
            dataRetornar.put("out_mensaje", "Exception");
            sendData(response, out, gson, dataRetornar);
        } finally {
            if (datasource != null) {
                try {
                    datasource.close();
                } catch (Exception e) {
                    logger.error(e.fillInStackTrace());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }
    }

    private void sendData(HttpServletResponse response, PrintWriter out, JsonObject jsonObj) {
        response.setHeader("access-control-allow-origin", "*");
        out.print(jsonObj.toString());
    }

    private void sendData(HttpServletResponse response, PrintWriter out, Gson gson, Map<String, Object> dataRetornar) {
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

    private Map<String, Pattern> generarPatrones(String[] stringPatrones) {
        Map<String, Pattern> salida = new HashMap<>();
        for (int i=0;i<stringPatrones.length ; i+=2){
            Pattern pattern_new;
            pattern_new = Pattern.compile(stringPatrones[i+1]);
            salida.put(stringPatrones[i], pattern_new);
        }
        return salida;
    }

    private long leeParamNum(JsonObject filaOut, String in_ITEM,
            Map<String,TipoParametro> listaParametros,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,listaParametros,listaPaterns);
        if (valor == null) {
            return 0;
        } else {
            return Long.parseLong(valor);
        }
    }

    private Date leeParamDate(JsonObject filaOut, String in_ITEM,
            Map<String,TipoParametro> listaParametros,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,listaParametros,listaPaterns);
        SimpleDateFormat sdf = new SimpleDateFormat("m/d/y");
        if (valor == null) {
            return null;
        } else {
            try {
                return sdf.parse(valor);
            } catch (ParseException ex) {
                throw new GesvitaException("'" + valor + "' no corresponde a formato de fecha");
            }
        }
    }
    
    private Float leeParamPerc(JsonObject filaOut, String in_ITEM,
            Map<String,TipoParametro> listaParametros,
            Map<String, Pattern> listaPaterns) throws GesvitaException {
        String valor = leeParamString(filaOut,in_ITEM,listaParametros,listaPaterns);
        
        
        if (valor == null || valor.length() < 2) {
            return 0F;
        } else if ("N/A".equals(valor)) {
            return null;
        } else {
            return Float.parseFloat(valor.substring(0,valor.length()-1));
        }
    }
    
    private String leeParamString(JsonObject filaOut, String in_String,
            Map<String, TipoParametro> mapParams,Map<String, Pattern> listaPaterns) throws GesvitaException {
        TipoParametro tipoParam = mapParams.get(in_String);
        if (tipoParam == null)
            throw new GesvitaException("Parámetro no definido: " + in_String);
        String campo = tipoParam.getCampo();
        String valor;
        
        if (tipoParam.isObligatorio()) {
            if (! filaOut.has(campo))
                throw new GesvitaException("Falta campo obligatorio: " + campo);
            valor =  extactParamString(filaOut,tipoParam.getCampo());
            if (!ValidarEntradaPatron(valor,tipoParam.getTipo(),listaPaterns)) {
                throw new GesvitaException("Campo obligatorio no cumple formato: " + campo);
            }
        } else {
            if (! filaOut.has(campo)) {
                valor = tipoParam.getDefaultValue();
            } else {
                valor = extactParamString(filaOut,campo);
                if (!ValidarEntradaPatron(valor,tipoParam.getTipo(),listaPaterns)) {
                    valor = tipoParam.getDefaultValue();
                }
            }
        }
        if ("null".equals(valor))
            return null;
        else
            return valor;
    }
    
    private boolean ValidarEntradaPatron(String entrada, String tipo,Map<String, Pattern> listaPaterns) {
        if (listaPaterns.containsKey(tipo)){
            Matcher m = listaPaterns.get(tipo).matcher(entrada);
            return m.matches();
        }else{
            return true;
        }
    }
    
    private String readParamString(JsonObject jsonObj, String field) throws GesvitaException {
        if (!jsonObj.has(field))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
        JsonElement value = jsonObj.get(field);
        String salida = value.toString();
        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
        }
        return salida;
    }
    
    private long readParamLong(JsonObject jsonObj, String field) throws GesvitaException {
        try {
            return Long.parseLong(readParamString(jsonObj, field));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected field: '" + field + "' is not a number");
        }
    }

    private JsonObject guardarEnBDProject(Logger logger, Connection conn, JsonObject jsonObj
            , Map<String,TipoParametro> listaParametros
            , Map<String,Pattern> listaPatrones
            , ProjectFile project) throws GesvitaException {
        long out_codigo = 0L;
        // String out_mensaje = "";
        CallableStatement stmt = null;
        long in_id_proyecto = leeParamNum(jsonObj, "ID PROYECTO", listaParametros, listaPatrones);
        long in_id_usuario = leeParamNum(jsonObj, "ID USUARIO", listaParametros, listaPatrones);
        Stack<TaskAndProp> pila;
        JsonObject jsonretorno = null;
        JsonObject item;
        int numitems = 0;
        double idtask;
        int MIN_NIVEL = 3;
        int MAX_NIVEL = 3;
        double firstcorrelativo = 1D;
        if (MIN_NIVEL == 1)
            firstcorrelativo = 0D;
        
        pila = leerTareasPrincipales(project,firstcorrelativo);
        try {
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_TAREA_INS("
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            while(!pila.empty()) {
                // Sacar la tarea del tope
                TaskAndProp tope = (TaskAndProp) pila.pop();
                Task t = tope.getTask();
                idtask = tope.getIdtask();
                long id_padre = tope.getIdPadre();
                int nivel = tope.getLevel();
                long id_actual;
                // Si el nivel es el menor
                if (nivel >= MIN_NIVEL ) {
                    
                    // Guardar la tarea
                    jsonretorno = subirTaskSP(conn, stmt , t , id_padre
                            ,in_id_proyecto, in_id_usuario, idtask , numitems );
                    id_actual = Long.parseLong( jsonretorno.get("out_id_tarea").toString());
                    out_codigo = Long.parseLong( jsonretorno.get("out_codigo").toString());
                    if (out_codigo != 0L) {
                        try {
                            vueltaAtras(conn,in_id_proyecto);
                        } catch (SQLException e) {}
                        item = new JsonObject();
                        item.addProperty("resultadoEjecucion", "NOK");
                        item.addProperty("out_codigo", out_codigo);
                        item.addProperty("out_mensaje", jsonretorno.get("out_mensaje").toString());
                        return item;
                    } else {
                        numitems++;
                    }
                } else {
                    id_actual = 0L;
                    idtask = 0D;
                }
                // No agregar hijos de nivel mayores al MAX_NIVEL
                if (nivel == MAX_NIVEL) {
                    continue;
                }
                // Poner los hijos de la tarea tope en la pila en el orden inverso
                AgregarTareasHijas(pila,t,id_actual,nivel
                        ,idtask
                        ,Math.pow(100D, 0.0D - nivel + MIN_NIVEL));
            }
        
            if (jsonretorno != null) {
                String str = jsonretorno.get("out_mensaje").toString();
                item = new JsonObject();
                if (out_codigo == 0){
                    item.addProperty("resultadoEjecucion", "OK");
                    item.addProperty("numdata", numitems);
                } else {
                    item.addProperty("resultadoEjecucion", "NOK");
                }
                item.addProperty("out_codigo", out_codigo);
                item.addProperty("out_mensaje", limpiezaString(str));
                return item;
            } else {
                item = new JsonObject();
                item.addProperty("resultadoEjecucion", "NOK");
                item.addProperty("out_codigo", 9999);
                item.addProperty("out_mensaje", "No se obtuvo ninguna respuesta de servicio para ingreso de tareas");
                return item;
            }
        } catch (SQLException ex) {
            String msg = "SQLException, en tarea : #" + numitems;
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (in_id_proyecto != 0L){
                try {
                    vueltaAtras(conn,in_id_proyecto);
                } catch (SQLException e) {}
            }
            throw new GesvitaException(msg);
        } catch (GesvitaException ex) {
            String msg = "Error en tarea # " + numitems + " : " +ex.getMessage();
            logger.error(msg);
            logger.error(ex.fillInStackTrace());
            if (in_id_proyecto != 0L){
                try {
                    vueltaAtras(conn,in_id_proyecto);
                } catch (SQLException e) {}
            }
            throw new GesvitaException(msg);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {}
        }
    }

    private JsonObject subirTaskSP(Connection conn, CallableStatement stmt, Task t,long in_id_tarea_padre
            ,long in_id_proyecto, long in_id_usuario, double idtask,int correlativo) throws GesvitaException, SQLException {
        Clob clob;
        JsonObject dataRetornar = new JsonObject();
        // BYSECURITY logger.info("JSON_TASK = " + jsonTask.toString());
        // stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_TAREA_INS(?,to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        // clob = conn.createClob();
        
        Double avance =null;
        if (t.getDuration().getDuration() != 0D) {
            avance = t.getActualWork().getDuration() * 100 / t.getDuration().getDuration();
        }
        
        long dif_dias = 1 + (t.getFinish().getTime() - t.getStart().getTime())/86400000L;

        stmt.setFloat(1, in_id_proyecto);
        setStatmentDate(stmt,2,t.getStart());           // Inicio Estimado
        setStatmentDate(stmt,3,t.getFinish());          // Termino Estimado
        setStatmentDate(stmt,4,t.getActualStart());           // Inicio Real
        setStatmentDate(stmt,5,t.getActualFinish());          // Termino Real
        if (in_id_tarea_padre == 0L) {
            stmt.setNull(6, java.sql.Types.NUMERIC);
            stmt.setString(7, "");                      // TASK: in_id_tarea_dependencia
            stmt.setString(8, t.getName());             // TASK: in_nombre_tarea
            setStatmentString(stmt,9, t.getNotes());    // Comentarios
            setStatmentLong(stmt,10,(long)t.getDuration().getDuration());    // Duración Real
            stmt.setFloat(11, 0);                       // Tipo tarea
            stmt.setFloat(12, 3);                       //  "in_id_prioridad"= 3,media
            stmt.setFloat(13, 1);                       // in_id_estado (1 : No iniciado)
            stmt.setNull(14, java.sql.Types.NUMERIC);   //  "in_avance_planificado"
            stmt.setNull(15, java.sql.Types.NUMERIC);   //  "in_avance_real"
        } else {
            stmt.setLong(6, in_id_tarea_padre);
            stmt.setString(7, "");           // TASK: in_id_tarea_dependencia
            stmt.setString(8, t.getName());             // TASK: in_nombre_tarea
            stmt.setString(9, t.getNotes());            // Comentarios
            stmt.setNull(10, java.sql.Types.NUMERIC);   // Duración Real
            stmt.setFloat(11, 0);                       // Tipo tarea : Normal
            stmt.setFloat(12, 3);                       //  "in_id_prioridad"= 3,media
            stmt.setNull(14, java.sql.Types.NUMERIC);   //  "in_avance_planificado"
            if (avance == null) {
                stmt.setFloat(13, 5);                       //  "in_id_estado"= 5 , N/A
                stmt.setNull(15, java.sql.Types.NUMERIC);   //  "in_avance_real"
            } else if (avance <= 0D) {
                stmt.setFloat(13, 1);                       //  "in_id_estado"= 1 , 0%
                stmt.setFloat(15,0F);                       // "in_avance_real"
            } else if (avance >= 100D) {
                stmt.setFloat(13, 3);                       //  "in_id_estado"= 3 , 100%
                stmt.setFloat(15,1F);                       // "in_avance_real"
            } else {
                stmt.setFloat(13, 2);                       //  "in_id_estado"= 2 , 0% < x < 100%
                stmt.setDouble(15, avance/100); // "in_avance_real"
            }
        }
        stmt.setNull(16, java.sql.Types.VARCHAR);          // "in_tarea_inbput"
        stmt.setNull(17, java.sql.Types.VARCHAR);          // "in_tarea_output"
        stmt.setFloat(18, 20);                             // Numerico : "in_id_formulario"
        stmt.setLong(19, in_id_usuario);                      // Numero : "in_id_usuario_responsable"
        stmt.setLong(20, in_id_usuario);                      // Numero : "in_id_usuario_ejecutor"
        clob = conn.createClob();
        clob.setString(1, "{ \"secuencia\": " + idtask + "}");                            // Clob : "in_data_extendida"
        stmt.setClob(21, clob);
        stmt.setLong(22, in_id_usuario);                      // Numerico : "in_id_usuario_creacion"
        stmt.setString(23, formatoNumOrden(correlativo));  // String : "in_id_bpm"
        // stmt.setLong(23, in_id_usuario);                      // Numerico : "in_id_usuario_ejecutor"
        stmt.setLong(24, dif_dias);              // Numerico : "in_duracion_planificada"
        stmt.setNull(25, java.sql.Types.VARCHAR);          // String: "in_tipo_tarea_negocio"
        stmt.setNull(26, java.sql.Types.NUMERIC);          // Numerico : "in_id_tarea_template"
        stmt.setNull(27, java.sql.Types.NUMERIC);          // Numerico : "in_id_proyecto_enlacetemplate"
        stmt.setNull(28, java.sql.Types.NUMERIC);          // Numerico : "in_id_proyecto_enlace"
        stmt.setNull(29, java.sql.Types.NUMERIC);          // Numerico : "in_porcentaje_asignacion"
        stmt.setNull(30, java.sql.Types.NUMERIC);          // Numerico : "in_porcentaje_en_tarea_padre"
        stmt.setString(31, "" + correlativo);       // Numerico : "in_codi_tarea"
        stmt.registerOutParameter(32, Types.NUMERIC);
        stmt.registerOutParameter(33, Types.NUMERIC);
        stmt.registerOutParameter(34, Types.VARCHAR);
        stmt.execute();
        dataRetornar.addProperty("out_id_tarea", stmt.getLong(32));
        dataRetornar.addProperty("out_codigo", stmt.getInt(33));
        dataRetornar.addProperty("out_mensaje", stmt.getString(34));
        return dataRetornar;
    }
    
    private void setStatmentDate(CallableStatement stmt, int i, Date fecha) throws SQLException {
        if (fecha == null) {
            stmt.setNull(i, java.sql.Types.DATE);
        } else {
            stmt.setDate(i, new java.sql.Date(fecha.getTime()));
        }
    }

    private void setStatmentLong(CallableStatement stmt, int i, Long data) throws SQLException {
        if (data == null) {
            stmt.setNull(i, java.sql.Types.NUMERIC);
        } else {
            stmt.setLong(i, data);
        }
    }

    private void setStatmentString(CallableStatement stmt, int i, String data) throws SQLException {
        if (data == null) {
            stmt.setNull(i, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(i, data);
        }
    }
    
    private void vueltaAtras(Connection conn, long id) throws SQLException{
            // BYSECURITY logger.info("Vuelta atras id " + id);
            CallableStatement stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_ELIMINAR_PROYECTOALL"
                    + "(?,?,?)}");
            stmt.setLong(1, id);
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
    }
    
    private String extactParamString(JsonObject jsonObj, String field) throws GesvitaException {
        JsonElement elem = jsonObj.get(field);
        return limpiezaString(elem.toString());
    }
    
    private String limpiezaString(String in) {
        if (in.startsWith("\"") && in.endsWith("\""))
            return in.substring(1, in.length()-1);
        else if (in.startsWith("'") && in.endsWith("'"))
            return in.substring(1, in.length()-1);
        else
            return in;
    }

    private ProjectFile LeerExcel4DB(Connection conn, JsonObject jsonObj,Map<String,TipoParametro> listaParametros,Map<String,Pattern> listaPatrones) throws GesvitaException {
        ProjectFile project;
        InputStream is;
        long id_archivo;
        ProjectReader reader = new UniversalProjectReader ();
        try {
            id_archivo = leeParamNum(jsonObj, "ID CACHE", listaParametros, listaPatrones);
            // logger.info("id_archivo = " + id_archivo);
            is = ConsultaArchivoDB(conn,id_archivo);
//            try {
//                logger.info("is " + is.available());
//            } catch (IOException ex) {
//                logger.error("IOException getting number of bytes available");
//            }
            project = reader.read(is);
        } catch (MPXJException ex) {
            logger.error(ex.fillInStackTrace());
            throw new GesvitaException("MPXJException : al leer archivo project");
        }
        return project;
    }

    private InputStream ConsultaArchivoDB(Connection conn,long id) throws GesvitaException {
        PreparedStatement stmt;
        InputStream salida = null;
        ResultSet rs;
        try {
            stmt = conn.prepareStatement("SELECT ID_CACHE, TIME_CREATION, TIME_UPDATE"
                    + " ,DESC_CONTENIDO, DESC_MIMETYPE, NOMBRE_ORIGINAL, NOMBRE_FINAL"
                    + " FROM tb_proyecto_cache WHERE ID_CACHE = ?");
            stmt.setLong(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String hexData = ClobUtil.clobToString(rs.getClob("DESC_CONTENIDO"));
                salida = new ByteArrayInputStream( BinaryFiles.hexStringToByteArray(hexData) );
                return salida;
            }
        } catch (SQLException e) {
            logger.error("Error durante el proceso del lectura de Project", e.fillInStackTrace());
            throw new GesvitaException("IOException : al leer archivo project");
        }
        return salida;
    }

    private Map<String, TipoParametro> generarMapaParametros(TipoParametro[] LISTAPARAMETROS) {
        Map<String, TipoParametro> salida = new HashMap<>();
        for (TipoParametro param_new : LISTAPARAMETROS){
            salida.put(param_new.getNombre(), param_new);
        }
        return salida;
    }

    private Stack<TaskAndProp> leerTareasPrincipales(ProjectFile project, double step) {
        Stack<TaskAndProp> salida = new Stack();
        List<Task> lista = project.getChildTasks();
        for (int i= lista.size()-1,j=1; i>=0;i--,j++) {
            Task task = lista.get(i);
            salida.push(new TaskAndProp(task, 0 , 1, step * j));
        }
        return salida;
    }
    
    private void AgregarTareasHijas(Stack<TaskAndProp> stack,Task task,long id_padre,int nivel
        ,double correl_padre,double jump) {
        List<Task> lista = task.getChildTasks();
        for (int i= lista.size()-1,j=1; i>=0;i--,j++) {
            Task hija = lista.get(i);
            stack.push(new TaskAndProp(hija,id_padre, nivel + 1, correl_padre + j * jump));
        }
    }

    private String formatoNumOrden(double num) {
        int desde = 2;
        if (num <0d) desde = 3;
        int entero = (int) num;
        double fraccion = num - entero;
        return String.format("%05d", entero) + "." + String.format("%014.12f", fraccion).substring(desde);
    }
}
