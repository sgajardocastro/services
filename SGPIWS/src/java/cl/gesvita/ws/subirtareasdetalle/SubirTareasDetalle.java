package cl.gesvita.ws.subirtareasdetalle;

import cl.gesvita.ws.archivoplantilla.bean.db.ResultOut;
import cl.gesvita.ws.subirtareasdetalle.bean.Task;
import cl.gesvita.ws.subirtareasdetalle.bean.TasksAndWarnings;
import cl.gesvita.ws.subirtareasdetalle.bean.Warning;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

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
import com.google.gson.JsonParser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class SubirTareasDetalle
 */
@WebServlet("/WSSubirTareasDetalle/SubirTareasDetalle")
public class SubirTareasDetalle extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(SubirTareasDetalle.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirTareasDetalle() {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;

        Map<String, Object> dataRetornar = new HashMap<>();
        Gson gson = new Gson();

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj, jsonObj0;
        Task tasks[];
        Warning warns[];
//        List<Integer> mapaAscendencia[];

        try {
            // Inicializar Log4J
            ObtenerLib.setLogParam(this.getClass());
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
            // Leer la entrada
            jsonObj = ObtenerLib.readInput(logger, request);
            // BYSECURITY logger.info("entrada leida");

            // generar lista de tareas y avisos 1
            TasksAndWarnings tyw = leerListaTareas(logger, jsonObj);
            // BYSECURITY logger.info("lista de tareas leidas");
            tasks = tyw.getTasks();
            warns = tyw.getAvisos();

            // Generar mapa de asendencia (Dependencia inversa)
//             mapaAscendencia = calcularAscendencia(tasks);
        } catch (GesvitaException ex) {
            logger.error(ex.getMessage());
            dataRetornar.put("out_codigo", 9999);
            dataRetornar.put("out_mensaje", "Error de parseo");
            sendData(response, out, gson, dataRetornar);
            return;
        }
        String str;
        try {
            conn = datasource.getConnection();
            
            // Eliminar las tareas previas
            jsonObj0 = clearTasks(conn, jsonObj);
            // deleteAllTasks(conn, Long.parseLong(readFieldString(jsonObj,"in_id_proyecto")));

            Warning warns2[] = recorrerTareasGuardandoEnBD(conn, jsonObj, gson, tasks);
            // BYSECURITY logger.info("recorrer guardando en BD, done");
            // , mapaAscendencia);

            List<Warning> allwarn = new ArrayList<>();
            if (warns != null) {
                allwarn.addAll(Arrays.asList(warns));
            }
            if (warns2 != null) {
                allwarn.addAll(Arrays.asList(warns2));
            }
            for (Warning w : allwarn) {
                // BYSECURITY logger.info("Aviso: " + w.getMensaje());
            }

            // String jsonwarn = gson.toJson(allwarn);
            dataRetornar.put("out_codigo", 0);
            dataRetornar.put("out_mensaje", "Proceso OK");
            dataRetornar.put("out_warning", allwarn);
            sendData(response, out, gson, dataRetornar);
        } catch (SQLException e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
            sendData(response, out, gson, dataRetornar);
        } catch (Exception e) {
            logger.error(e.fillInStackTrace());
            dataRetornar.put("out_codigo", 9999);
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

    private Warning[] recorrerTareasGuardandoEnBD(Connection conn, JsonObject jsonObj, Gson gson, Task[] tasks) {
        //   },List<Integer> mapaAscendencia[]) {
        // Transformar las JSON con las tareas en lista de beans
        // Generar mapa de beans y matriz de dependencias
        // generar listado de Id de Tarea
        //
        // ****************************
        // BYSECURITY logger.info("JSON_GRAL = " + jsonObj.toString());

        List<Warning> avisos = new ArrayList<>();
        Warning aviso, ret[];
        List<Task> independientes = new ArrayList<>();
        List<Task> porrevisar;
        int nroDependencias[] = new int[tasks.length];
        Map<Integer, List<Task>> dependenciaInversa = new HashMap<>();
        int i, j;
        // Inicializar variables: nroDependencias, dependientes, independientes y dependenciaInversa  
        for (Task t : tasks) {
            i = t.getLinea() - 2;
            // logger.info("1.- Tarea # '" + t.getStrId() + "',name: " + t.getName() + ", padre:'" + t.getPadre() + "',predec:'" + t.getPredecesoras() + "', dependientes:" + t.getNumDependientes());
            // nroDependencias[i] = t.getNumDependientes();
            nroDependencias[i] = 0;
            if ((t.getPadre() == -1) && (t.getPredecesoras() == null)) {
                independientes.add(t);
            }
            // Si tiene padre
            if (t.getPadre() != -1) {
                nroDependencias[i]++;
                agregarDependenciaInversa(dependenciaInversa, t.getPadre(), t);
            }
            // Si hay tareas de las que dependa
            if ((t.getPredecesoras() != null) && (t.getPredecesoras().length > 0)) {
                nroDependencias[i] += t.getPredecesoras().length;
                // logger.info("# predecesoras : " + t.getPredecesoras().length);
                for (j = 0; j < t.getPredecesoras().length; j++) {
                    agregarDependenciaInversa(dependenciaInversa, t.getPredecesoras()[j], t);
                }
            }
        }
        // Almacenar los independientes
        j = 1;
        while (!independientes.isEmpty()) {
            // BYSECURITY logger.info("CICLO # " + j);
            // BYSECURITY logger.info("Independientes : " + independientes.size());
            porrevisar = new ArrayList();
            for (i = independientes.size() - 1; i >= 0; i--) {
                Task t = independientes.get(i);
                // BYSECURITY logger.info("2.- Tarea # '" + t.getStrId() + "'");
                // Almacenar en Base de datos
                aviso = null;
                try {
                    if (t.getIddb() == -1) {
                        aviso = almacenarDB(conn, jsonObj, t, tasks, gson);
                    }
                } catch (GesvitaException ex) {
                    logger.error(ex.fillInStackTrace());
                    aviso = new Warning(t.getLinea(), "No se pudo almacenar tarea");
                }
                if (aviso != null) {
                    avisos.add(aviso);
                }
                // BYSECURITY logger.info(" comienza añade x revisar");
                if (dependenciaInversa.containsKey(t.getLinea() - 2)) {
                    for (Task x : dependenciaInversa.get(t.getLinea() - 2)) {
                        porrevisar.add(x);
                    }
                }
                independientes.remove(i);
            }
            // BYSECURITY logger.info("x revisar : " + porrevisar.size());
            for (Task t : porrevisar) {
                i = t.getLinea() - 2;
                // BYSECURITY logger.info("3.- Tarea # '" + t.getStrId() + "' , (peso:" + nroDependencias[i] + ")");
                nroDependencias[i]--;
                if (nroDependencias[i] == 0) {
                    independientes.add(t);
                }
            }
            j++;
        }
        // Recorrer las tareas que no pudieron ser guardadas
        List<Task> noguardadas = new ArrayList<>();
        String separador = ";";
        String ciclo = "";
        for (Task t : tasks) {
            if (t.getIddb() == -1) {
                ciclo += separador + t.getLinea();
                noguardadas.add(t);
                separador = ";";
            }
        }
        if (noguardadas.size() != 0) {
            aviso = new Warning(tasks[tasks.length - 1].getLinea(), "Existe un ciclo de dependencias que involucra las lineas : " + ciclo);
            avisos.add(aviso);
        }
        if (avisos.isEmpty()) {
            return null;
        } else {
            ret = new Warning[avisos.size()];
            avisos.toArray(ret);
            return ret;
        }
    }

    private Warning almacenarDB(Connection conn, JsonObject jsonObj, Task t, Task[] tasks, Gson gson) throws GesvitaException {
        Warning ret = null;
        try {
//            stmt.setString(2, readFieldString(jsonTask, "in_fecha_plan_ini"));                 // TASK
//            stmt.setString(3, readFieldString(jsonTask, "in_fecha_plan_fin"));                 // TASK
//            stmt.setString(4, readFieldString(jsonTask, "in_fecha_real_ini"));                 // TASK
//            stmt.setString(5, readFieldString(jsonTask, "in_fecha_real_fin"));                 // TASK
//            str = readFieldString(jsonTask, "in_id_tarea_padre");                              // TASK
//            stmt.setString(7, readFieldString(jsonTask, "in_id_tarea_dependencia"));           // TASK
//            stmt.setString(8, readFieldString(jsonTask, "in_nombre_tarea"));                   // TASK
//            stmt.setFloat(10, Float.parseFloat(readFieldString(jsonTask, "in_duracion_real")));// TASK
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            Map<String, Object> dataentrada = new HashMap<>();
            int dias = -1;
            if (t.getFechaFin() != null && t.getFechaInicio() != null) {
                dias = (int) ((t.getFechaFin().getTime() - t.getFechaInicio().getTime()) / 86400000);
            }
            if (t.getFechaInicio() != null) {
                dataentrada.put("in_fecha_plan_ini", df.format(t.getFechaInicio()));
            } else {
                dataentrada.put("in_fecha_plan_ini", "");
            }
            if ((t.getFechaFin() != null) && (dias >= 0)) {
                dataentrada.put("in_fecha_plan_fin", df.format(t.getFechaFin()));
            } else {
                dataentrada.put("in_fecha_plan_fin", "");
            }
            if (t.getFechaInicio() != null) {
                dataentrada.put("in_fecha_real_ini", df.format(t.getFechaInicio()));
            } else {
                dataentrada.put("in_fecha_real_ini", "");
            }
            if ((t.getFechaFin() != null) && (dias >= 0)) {
                dataentrada.put("in_fecha_real_fin", df.format(t.getFechaFin()));
            } else {
                dataentrada.put("in_fecha_real_fin", "");
            }
            if (t.getPadre() != -1) {
                if (tasks[t.getPadre()].getIddb() != -1) {
                    dataentrada.put("in_id_tarea_padre", tasks[t.getPadre()].getIddb());
                } else {
                    dataentrada.put("in_id_tarea_padre", "");
                    ret = new Warning(t.getLinea(), "Tarea padre no identificada");
                }
            } else {
                dataentrada.put("in_id_tarea_padre", "");
            }
            StringBuilder in = new StringBuilder();
            String separador;
            if (t.getPredecesoras() != null) {
                separador = "";
                for (int i = 0; i < t.getPredecesoras().length; i++) {
                    if (t.getPredecesoras()[i] != -1 && tasks[t.getPredecesoras()[i]].getIddb() != -1) {
                        in.append(separador);
                        in.append(tasks[t.getPredecesoras()[i]].getIddb());
                        separador = ";";
                    } else {
                        // BYSECURITY logger.info("Warning: " + t);
                        ret = new Warning(t.getLinea(), "Existe dependencia no identificada");
                    }
                }

            }
            dataentrada.put("in_id_bpm", formatoNumOrden( t.getLinea() -1));
            
            dataentrada.put("in_id_tarea_dependencia", in.toString());
            dataentrada.put("in_nombre_tarea", t.getName());
            if (dias >= 0) {
                dataentrada.put("in_duracion_real", (1 + dias));
                dataentrada.put("in_duracion_planificada", (1 + dias));
            } else {
                dataentrada.put("in_duracion_real", "");
                dataentrada.put("in_duracion_planificada", "");
            }
            String json = gson.toJson(dataentrada);
            JsonParser parser = new JsonParser();
            JsonObject jsonTask = (JsonObject) parser.parse(json);

            String respuesta = subirTaskSP(conn, jsonObj, jsonTask, t);

            JsonObject jsonRespuesta = (JsonObject) parser.parse(respuesta);

            int iddb = Integer.parseInt(jsonRespuesta.get("out_id_tarea").toString());
            int errDB = Integer.parseInt(jsonRespuesta.get("out_codigo").toString());
            String msgdb = jsonRespuesta.get("out_mensaje").toString();
            if (errDB == 0) {
                t.setIddb(iddb);
            } else {
                throw new GesvitaException("Error PLSQL (" + errDB + "): " + msgdb);
            }
            return ret;
        } catch (SQLException ex) {
            throw new GesvitaException("Error SQL :" + ex.getMessage());
        }
    }

    private String subirTaskSP(Connection conn, JsonObject jsonGral, JsonObject jsonTask, Task t) throws SQLException {
        CallableStatement stmt = null;
        Clob clob;
        String str;
        Map<String, Object> dataRetornar = new HashMap<>();
        try {
            // BYSECURITY logger.info("JSON_TASK = " + jsonTask.toString());
            stmt = conn.prepareCall("{call PKG_MONITOR_INSERT.PROYECTO_TAREA_INS(?,to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),to_date(?, \'DD/MM/YYYY\'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            clob = conn.createClob();
            stmt.setFloat(1, Float.parseFloat(readFieldString(jsonGral, "in_id_proyecto")));
            stmt.setString(2, readFieldString(jsonTask, "in_fecha_plan_ini"));                 // TASK
            stmt.setString(3, readFieldString(jsonTask, "in_fecha_plan_fin"));                 // TASK
            stmt.setString(4, readFieldString(jsonTask, "in_fecha_real_ini"));                 // TASK
            stmt.setString(5, readFieldString(jsonTask, "in_fecha_real_fin"));                 // TASK
            str = readFieldString(jsonTask, "in_id_tarea_padre");                              // TASK
            if (!str.equals("")) {
                stmt.setFloat(6, Float.parseFloat(str));
            } else {
                stmt.setNull(6, java.sql.Types.NUMERIC);
            }
            stmt.setString(7, readFieldString(jsonTask, "in_id_tarea_dependencia"));           // TASK
            stmt.setString(8, readFieldString(jsonTask, "in_nombre_tarea"));                   // TASK
            stmt.setString(9, readFieldString(jsonGral, "in_descripcion_tarea"));
            str = readFieldString(jsonTask, "in_duracion_real");                               // TASK
            if (!str.equals("")) {
                stmt.setFloat(10, Float.parseFloat(str));
            } else {
                stmt.setNull(10, java.sql.Types.NUMERIC);
            }
            stmt.setFloat(11, Float.parseFloat(readFieldString(jsonGral, "in_id_tipo_tarea")));
            stmt.setFloat(12, Float.parseFloat(readFieldString(jsonGral, "in_id_prioridad")));
            stmt.setFloat(13, Float.parseFloat(readFieldString(jsonGral, "in_id_estado")));
            stmt.setFloat(14, Float.parseFloat(readFieldString(jsonGral, "in_avance_planificado")));
            stmt.setFloat(15, Float.parseFloat(readFieldString(jsonGral, "in_avance_real")));
            stmt.setString(16, readFieldString(jsonGral, "in_tarea_input"));
            stmt.setString(17, readFieldString(jsonGral, "in_tarea_output"));
            stmt.setFloat(18, Float.parseFloat(readFieldString(jsonGral, "in_id_formulario")));
            stmt.setFloat(19, Float.parseFloat(readFieldString(jsonGral, "in_id_usuario_responsable")));
            stmt.setFloat(20, Float.parseFloat(readFieldString(jsonGral, "in_id_usuario_ejecutor")));
            clob.setString(1, readFieldString(jsonGral, "in_data_extendida"));
            stmt.setClob(21, clob);
            stmt.setFloat(22, Float.parseFloat(readFieldString(jsonGral, "in_id_usuario_creacion")));
            stmt.setString(23, readFieldString(jsonTask, "in_id_bpm"));   // in_id_bpm : orden de despligue
            // Version original del campo en que se saca de un valor por defecto
            // stmt.setString(23, readFieldString(jsonGral, "in_id_bpm"));
            str = readFieldString(jsonTask, "in_duracion_planificada");                               // TASK
            if (!str.equals("")) {
                stmt.setFloat(24, Float.parseFloat(str));
            } else {
                stmt.setNull(24, java.sql.Types.NUMERIC);
            }
            stmt.setString(25, readFieldString(jsonGral, "in_tipo_tarea_negocio"));
            stmt.setFloat(26, Float.parseFloat(readFieldString(jsonGral, "in_id_tarea_template")));
            stmt.setFloat(27, Float.parseFloat(readFieldString(jsonGral, "in_id_proyecto_enlacetemplate")));
            stmt.setFloat(28, Float.parseFloat(readFieldString(jsonGral, "in_id_proyecto_enlace")));
            stmt.setFloat(29, Float.parseFloat(readFieldString(jsonGral, "in_porcentaje_asignacion")));
            stmt.setFloat(30, Float.parseFloat(readFieldString(jsonGral, "in_porcentaje_en_tarea_padre")));
            stmt.setString(31, readFieldString(jsonGral, "in_codi_tarea"));
            stmt.registerOutParameter(32, Types.NUMERIC);
            stmt.registerOutParameter(33, Types.NUMERIC);
            stmt.registerOutParameter(34, Types.VARCHAR);
            stmt.execute();
            int idbd = stmt.getInt(32);
            t.setIddb(idbd);
            dataRetornar.put("out_id_tarea", idbd);
            dataRetornar.put("out_codigo", stmt.getInt(33));
            dataRetornar.put("out_mensaje", stmt.getString(34));
            Gson gson = new Gson();
            return gson.toJson(dataRetornar);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error(e.fillInStackTrace());
                }
            }
        }

    }

    private ResultOut deleteAllTasks(Connection conn, long id_proyecto) throws IOException, SQLException {
        CallableStatement stmt = null;
        ResultOut salida = new ResultOut(0, -1, "");
        try {
            stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_ELIMINAR_TAREAS(?,?,?)}");
            stmt.setFloat(1, id_proyecto); // "in_id_proyecto"
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.execute();
            salida.setCode(stmt.getInt(2));
            salida.setDescription(stmt.getString(3));
            return salida;

        } catch (SQLException e) {
            logger.error("storePartDB : Exception: ", e.fillInStackTrace());
            StringBuilder sb = new StringBuilder();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.error("stmt.close() : Exception: ", e.fillInStackTrace());
                }
            }
        }
        return salida;
    }
    
    
    
    private JsonObject clearTasks(Connection conn,JsonObject jsonObj) throws SQLException {
        CallableStatement stmt = null;
        Map<String, Object> dataRetornar = new HashMap<>();

        stmt = conn.prepareCall("{call PKG_MONITOR_DELETE.PROYECTO_ELIMINAR_TAREAS(?,?,?)}");
        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj, "in_id_proyecto")));
        stmt.registerOutParameter(2, Types.NUMERIC);
        stmt.registerOutParameter(3, Types.VARCHAR);
        stmt.execute();
        int out_codigo = stmt.getInt(2);  /* 0 : OK */
        dataRetornar.put("out_codigo", out_codigo);
        dataRetornar.put("out_mensaje", stmt.getString(3));
        
        // logger.info("Codigo despues de borrar tareas previas: " + out_codigo + ",mensaje: " + dataRetornar.get("out_mensaje"));
        
        Gson gson = new Gson();
        String resp = gson.toJson(dataRetornar);
        JsonParser parser = new JsonParser();
        JsonObject jsonObjResp = (JsonObject) parser.parse(resp);
        return jsonObjResp;
    }

    private void sendData(HttpServletResponse response, PrintWriter out, Gson gson, Map<String, Object> dataRetornar) {
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

    private String readFieldString(JsonObject jsonObj, String field) {
        return readFieldString(jsonObj, field.toLowerCase(), "");
    }

    private String readFieldStringNormal(JsonObject jsonObj, String field) {
        return readFieldString(jsonObj, field, "");
    }

    private String readFieldString(JsonObject jsonObj, String field, String bydefault) {
        if (!jsonObj.has(field)) {
            return bydefault;
        }
        JsonElement value = jsonObj.get(field);
        String salida;
        if (value instanceof JsonArray) { // [,,,,,,,]
            salida = value.toString();
        } else if (value instanceof JsonObject) {   // {,,,,,,,}
            salida = value.toString();
        } else {
            salida = value.toString();
            if (salida.startsWith("\"") && salida.endsWith("\"")) {
                salida = salida.substring(1, salida.length() - 1);
            } else if (salida.startsWith("'") && salida.endsWith("'")) {
                salida = salida.substring(1, salida.length() - 1);
            }
        }
        return salida;
    }

    private TasksAndWarnings leerListaTareas(Logger logger, JsonObject jsonObj) {
        TasksAndWarnings ret = new TasksAndWarnings();
        JsonElement arrayJson = jsonObj.get("in_task");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        List<Task> lista = new ArrayList<>();
        List<Warning> listaWarnings = new ArrayList<>();
        Map keys = new HashMap();
        if (arrayJson instanceof JsonArray) {
            JsonArray arr = (JsonArray) arrayJson;
            for (int i = 0, linea = 2; i < arr.size(); i++, linea++) {
                JsonElement elem = arr.get(i);
                if (elem.isJsonObject()) {
                    JsonObject jsonRow = (JsonObject) elem;
                    String strid = readFieldStringNormal(jsonRow, "ID");
                    Date finicio;
                    Date ffin;
                    try {
                        finicio = df.parse(readFieldStringNormal(jsonRow, "FECHA_INI"));
                        ffin = df.parse(readFieldStringNormal(jsonRow, "FECHA_FIN"));
                    } catch (ParseException ex) {
                        // Warning formato fecha no corresponde!!!
                        listaWarnings.add(new Warning(linea, "Formato fecha no corresponde a DD/MM/YYYY"));
                        finicio = null;
                        ffin = null;
                    }
                    Task t = new Task(linea, // # de línea
                            -1, // id en la base de datos
                            strid, // id entrada de la tarea
                            readFieldStringNormal(jsonRow, "NOMBRE"), // Nombre de la tarea
                            finicio, // Fecha Inicio
                            ffin, // Fecha Fin
                            readFieldStringNormal(jsonRow, "PADRE"), // Tarea padre
                            readFieldStringNormal(jsonRow, "PREDECESORAS"));  // Tareas predecesoras
                    lista.add(t);
                    if (!keys.containsKey(strid)) {
                        keys.put(strid, i);
                    } else {
                        // Warning key already exist!
                        listaWarnings.add(new Warning(linea, "Llave '" + strid + "' repetida"));
                    }
                } else {
                    listaWarnings.add(new Warning(linea, "No pudo ser procesada"));
                    // ERROR : linea ignorada x error de conversión
                }
            }

            // Revisar todos los PADRES y PREDECESORAS para generar la lista de referencias
            lista.forEach((Task t) -> {

                String strpadre = t.getStrPadre();
                String strPred = t.getStrPredecesoras();
                if (!strpadre.equals("")) {
                    if (keys.containsKey(strpadre)) {
                        int nro = (Integer) (keys.get(strpadre));
                        t.setPadre(nro);
                        Task reftask = lista.get(nro);
                        reftask.setNumDependientes(reftask.getNumDependientes() + 1);
                    } else {
                        t.setPadre(-1);
                        listaWarnings.add(new Warning(t.getLinea(), "No existe tarea padre '" + strpadre + "'"));
                        // Warning: Id de padre no encontrado!!!
                    }
                } else {
                    t.setPadre(-1);
                }
                // BYSECURITY logger.info("calculo predec para " + t);
                String[] strPredecesoras = strPred.split(";");
                List<Integer> listaPredecesoras = new ArrayList<>();
                for (String pred : strPredecesoras) {
                    if (!pred.equals("")) {
                        if (keys.containsKey(pred)) {
                            // listaPredecesoras.add((Integer) (keys.get(pred)));
                            int nro = (Integer) (keys.get(pred));
                            listaPredecesoras.add(nro);
                            Task reftask = lista.get(nro);
                            reftask.setNumDependientes(reftask.getNumDependientes() + 1);
                        } else {
                            // Warning: Id de dependencia no encontrado!!!
                            listaWarnings.add(new Warning(t.getLinea(), "No existe tarea '" + pred + "' para dependencia"));
                        }
                    }
                }
                t.setPredecesoras(toIntArray(listaPredecesoras));
                // BYSECURITY logger.info("calculadas predec para " + t);
            });
            ret.setAvisos(toWarningArray(listaWarnings));
            ret.setTasks(toTaskArray(lista));
            return ret;

        } else if (arrayJson instanceof JsonObject) {
            JsonObject arr = (JsonObject) arrayJson;
            // Not implemented yet!!
            listaWarnings.add(new Warning(2, "Formato JSON incorrecto"));

        } else {
            listaWarnings.add(new Warning(2, "Formato entrada incorrecto"));
        }
        ret.setAvisos(toWarningArray(listaWarnings));
        ret.setTasks(toTaskArray(lista));
        return ret;

    }

    private Task[] toTaskArray(List<Task> list) {
        if (list.isEmpty()) {
            return null;
        }
        Task[] ret = new Task[list.size()];
        list.toArray(ret);
        return ret;
    }

    private Warning[] toWarningArray(List<Warning> list) {
        if (list.isEmpty()) {
            return null;
        }
        Warning[] ret = new Warning[list.size()];
        list.toArray(ret);
        return ret;
    }

    private int[] toIntArray(List<Integer> list) {
        if (list.isEmpty()) {
            return null;
        }
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }

    private void agregarDependenciaInversa(Map<Integer, List<Task>> dependenciaInversa, int padre, Task t) {
        List<Task> lista;
        if (dependenciaInversa.containsKey(padre)) {
            // BYSECURITY logger.info("agregarDependenciaInversa padre");
            lista = dependenciaInversa.get(padre);
            lista.add(t);
            dependenciaInversa.replace(padre, lista);
        } else {
            // BYSECURITY logger.info("agregarDependenciaInversa otro");
            lista = new ArrayList<>();
            lista.add(t);
            dependenciaInversa.put(padre, lista);
        }
    }

    private String formatoNumOrden(double num) {
        int desde = 2;
        if (num <0d) desde = 3;
        int entero = (int) num;
        double fraccion = num - entero;
        return String.format("%05d", entero) + "." + String.format("%014.12f", fraccion).substring(desde);
    }

//    private List<Integer>[] calcularAscendencia(Task[] tasks) {
//        List<Integer> ret[];
//        ret = new List[tasks.length];
//        int nrotareas;
//        int i;
//        Task t;
//        nrotareas = tasks.length;
//        for( i=0;i<nrotareas;i++ ){
//            t = tasks[i];
//            ret[i] = new ArrayList<>();
//            i++;
//        }
//        for( i=0;i<nrotareas;i++ ){
//            t = tasks[i];
//            int k = t.getPadre();
//            if (k!= -1){
//                ret[k].add(i);
//            }
//            if (t.getPredecesoras() != null){
//                for (int x : t.getPredecesoras()) {
//                    ret[x].add(i);
//                }
//            }
//            i++;
//        }
//        for( i=0;i<nrotareas;i++ ){
//            if (ret[i].isEmpty())
//                ret[i] = null;
//            i++;
//        }
//        return ret;
//    }
}
