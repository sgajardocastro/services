package cl.gesvita.ws.subircapex;

import cl.gesvita.util.ClobUtil;
import cl.gesvita.ws.subircapex.bean.EntradaCapex;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import cl.gesvita.ws.obtener.lib.ObtenerLib;
import cl.gesvita.ws.subircapex.bean.Warning;
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
import com.google.gson.JsonParser;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class SubirCapex
 */
@WebServlet("/WSSubirCapex/SubirCapexOLD")
public class SubirCapex1 extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static Logger logger = Logger.getLogger(SubirCapex1.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubirCapex1() {
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

        String CABECERAS[] = {"id","partida","presupuesto","gasto real","saldo","porcentaje"};
        String CABECERASDB[] = {"id","partida","presupuesto","gasto_real","saldo"};
        Pattern pattern_num;
        Pattern pattern_entero;
        String regex_num = "^[\\d]+\\.[\\d]+$";
        pattern_num = Pattern.compile(regex_num);
        String regex_entero = "^[\\d\\,]+$";
        pattern_entero = Pattern.compile(regex_entero);

        Connection conn = null;

        Map<String, Object> dataRetornar = new HashMap<>();
        Gson gson = new Gson();

        response.setContentType("application/json");

        String etapa = "Obtener Conexión";
        PrintWriter out = response.getWriter();
        DataSource datasource;
        JsonObject jsonObj;
        EntradaCapex tasks[];
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
            etapa = "Leer entrada request";
            // Leer la entrada excel
            jsonObj = ObtenerLib.readInput(logger, request);
            // BYSECURITY logger.info("entrada leida");
            
            
            // Extraer JsonObject correspondiente a excel
            etapa = "Extraer JSON de excel";
            JsonElement arrayJsonArch  = jsonObj.get("in_task");
            JsonArray jsonObj_arch;

            if (arrayJsonArch == null || arrayJsonArch instanceof JsonArray) {
                jsonObj_arch = (JsonArray)arrayJsonArch;
            } else {
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde");
            }
            etapa = "Decodificar JSON de excel";
            JsonArray jsonObjExcel = decodificarColumnasExcel(logger,jsonObj_arch,
                    CABECERAS, CABECERASDB, pattern_num , pattern_entero);
            // JsonElement jsonObjExcel = leerJsonDeExcel(logger,jsonObj);
            
            // Generar Lista de beans con la entrada leida del excel
            Map mapExcel = generarIndiceID(logger,jsonObjExcel);
            
            // Leer data de base de datos
            etapa = "Leer data de BD";
            conn = datasource.getConnection();
            JsonElement jsonObjDB = leerJsonDeDB(logger,conn,jsonObj);
            
            // BYSECURITY logger.info("Intermedio");
            // BYSECURITY logger.info(jsonObjDB);
            
            // Actualizar los valores
            etapa = "Procesar data";
            int count = actualizarValores(logger,jsonObjDB,mapExcel,
                    CABECERAS, CABECERASDB, pattern_num , pattern_entero );
            
            // BYSECURITY logger.info("Final");
            // BYSECURITY logger.info("A Guardar: " + jsonObjDB.toString());
            // Imprimir el jsonObjDB con los cambios
            
            // Guardar la salida
            etapa = "Guardar data en BD";
            guardarEnBd(conn,jsonObj, jsonObjDB );
            
            
            
            
            // guardarJsonEnDB(logger,conn,jsonObjDB);
            
            // Leer entrada

            // generar lista de tareas y avisos 1
//            TasksAndWarnings tyw = leerCapex(logger, jsonObj);
//            // BYSECURITY logger.info("lista de tareas leidas");
//            tasks = tyw.getTasks();
//            warns = tyw.getAvisos();


//            List<Warning> allwarn = new ArrayList<>();
//            if (warns != null) {
//                allwarn.addAll(Arrays.asList(warns));
//            }
//            if (warns2 != null) {
//                allwarn.addAll(Arrays.asList(warns2));
//            }
//            for (Warning w : allwarn) {
//                // BYSECURITY logger.info("Aviso: " + w.getMensaje());
//            }

            // String jsonwarn = gson.toJson(allwarn);
            dataRetornar.put("resultadoEjecucion","OK");
            dataRetornar.put("out_codigo", 0);
            dataRetornar.put("out_mensaje", "Proceso OK");
//            dataRetornar.put("out_warning", allwarn);
            sendData(response, out, gson, dataRetornar);
        } catch (SQLException e) {
            logger.error(etapa);
            logger.error(e.fillInStackTrace());
            dataRetornar.put("resultadoEjecucion","NOK");
            dataRetornar.put("out_codigo", 9998);
            dataRetornar.put("out_mensaje", "SQLException");
            sendData(response, out, gson, dataRetornar);
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

    private void sendData(HttpServletResponse response, PrintWriter out, Gson gson, Map<String, Object> dataRetornar) {
        String json = gson.toJson(dataRetornar);
        response.setHeader("access-control-allow-origin", "*");
        out.print(json);
    }

//    private TasksAndWarnings leerCapex(Logger logger, JsonObject jsonObj) {
//        TasksAndWarnings ret = new TasksAndWarnings();
//        JsonElement arrayJson = jsonObj.get("in_task");
//        List<EntradaCapex> lista = new ArrayList<>();
//        List<Warning> listaWarnings = new ArrayList<>();
//        Map keys = new HashMap();
//    }

    private Map<Double,JsonObject> generarIndiceID(Logger logger, JsonElement jsonObj) {
        Map<Double,JsonObject> map = new HashMap();
        if (jsonObj instanceof JsonArray) {
            JsonArray arr;
            arr = (JsonArray) jsonObj;
            for (JsonElement fila:arr) {
                if (fila instanceof JsonObject) {
                    JsonObject filaObj = (JsonObject) fila;
                    Double idfila = leeDoubleFromJsonObject("id",filaObj);

                    if (idfila != 0D && !map.containsKey(idfila)) {
                        map.put(idfila,filaObj);
                    }
                }
            }
        }
        return map;
    }

    // Aquellos que no se puede leer el campo quedan como 0.
    private Double leeDoubleFromJsonObject(String id, JsonObject jsonObj) {
        if (!jsonObj.has(id))
            return 0D;
        String data = jsonObj.get(id).toString().trim();
        if (  (data.startsWith("\"") && data.endsWith("\""))
           || (data.startsWith("'")  && data.endsWith("'"))  ){
            data = data.substring(1,data.length()-1);
        }
        try {
            return Double.parseDouble(data);
        } catch (NumberFormatException ex) {
            return 0D;
        }
    }

    private int actualizarValores(Logger logger, JsonElement jsonObjDB, Map<Double,JsonObject> mapExcel,
            String CABECERAS[], String CABECERASDB[],
            Pattern pattern_num, Pattern pattern_entero ) {
        if (jsonObjDB instanceof JsonArray) {
            JsonArray arrDB;
            arrDB = (JsonArray) jsonObjDB;
            Set<Double> cjto_keys = mapExcel.keySet();
            
            // BYSECURITY logger.info("Actualizar valores");
            for (JsonElement filaDB:arrDB) {
                // BYSECURITY logger.info("fila BD " +filaDB.toString());
                if (filaDB instanceof JsonObject) {
                    JsonObject filaObjDb = (JsonObject) filaDB;
                    Double idfila = leeDoubleFromJsonObject("id",filaObjDb);

                    if (idfila != 0D && mapExcel.containsKey(idfila)) {
                        JsonObject filaExcel = mapExcel.get(idfila);
                        // BYSECURITY logger.info("fila Ex " +filaExcel.toString());
                        // BYSECURITY logger.info("Match by id = " + idfila);
                        ActualizaCamposFila(filaObjDb , filaExcel);
                        cjto_keys.remove(idfila);
                    } else {
                        ;
                        // BYSECURITY logger.info("Don't Match id = " + idfila);
                    }
                }
            }
            // BYSECURITY logger.info("Agregar filas no encontradas");
            // BYSECURITY logger.info("Keys: " + cjto_keys.toString());
            for ( Double k : cjto_keys) {
                // logger.info("K = " + k);
                if (k!= 0D) {
                    // logger.info("lee " + k);
                    JsonElement elem = mapExcel.get(k);
                    // logger.info(elem.toString());
                    if ((elem == null) || !(elem instanceof JsonObject)){
                        continue;
                    }
                    // logger.info("Clonar !!!");
                    JsonObject o = cloneFilaMasAsterisco(CABECERASDB,(JsonObject) elem , pattern_num, pattern_entero);
                    if (o != null){
                        // logger.info("añadir : " + o.toString());
                        arrDB.add(o);
                    }
                    // logger.info("Nulo");
                }
            }
        }
        return 0;
        
    }

    private void ActualizaCamposFila(JsonObject jsonObjDB, JsonObject filaExcel) {
        String CABECERASDB[] = {"presupuesto","gasto_real","saldo"};
        for (String field : CABECERASDB){
            ActualizaCampoIntegerFila(jsonObjDB,filaExcel,field);
        }
    }
    
    private void ActualizaCampoIntegerFila(JsonObject jsonObjDB, JsonObject filaExcel,String field) {
        if (filaExcel.has(field)) {
            Long valorL = leeLongFromJsonObject(field,filaExcel);
            if (jsonObjDB.has(field)) {
                jsonObjDB.remove(field);
            }
            jsonObjDB.addProperty(field, valorL);
        }
    }

    private Long leeLongFromJsonObject(String field, JsonObject jsonObj) {
        String data = jsonObj.get(field).getAsString();
        try {
            return Long.parseLong(data);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
    
    // Leer el excel y transformarlo en un JSON entendible
    private JsonArray decodificarColumnasExcel(Logger logger, JsonArray arr,
            String CABECERAS[], String CABECERASDB[],
            Pattern pattern_num, Pattern pattern_entero ) throws GesvitaException {
        JsonArray salida = new JsonArray();
        Pattern pattern_percent;
        String regex_percent = "^[\\d]+\\.[\\d]{2}\\%$";
        pattern_percent = Pattern.compile(regex_percent);
        boolean cabecera = false;
        boolean cabeceralista = false;
//        int i=-1;
        String keycabeceras[] = new String[CABECERAS.length];
        for (JsonElement fila:arr) {
            if (fila instanceof JsonObject) {
                JsonObject filaObj = (JsonObject) fila;
                if (!cabecera){
                    Set<String> keys = filaObj.keySet();
                    // Para cada llave dentro de la filaDB
                    for (String k : keys) {
                        String val = (filaObj.get(k).getAsString()).trim();
                        if (!cabecera && val.toLowerCase().equals(CABECERAS[1])) {
                            int encontrado= 0;
                            for (String k2 : keys) {
                                val = (filaObj.get(k2).getAsString()).trim();
                                for (int z =1 ;z <CABECERAS.length-1;z++) {
                                    if (val.toLowerCase().equals(CABECERAS[z])) {
                                        keycabeceras[z] = "" + k2;
                                        encontrado++;
                                    }
                                }
                                if ( encontrado == CABECERAS.length - 2) {
                                    cabecera = true;
                                    break;
                                }
                            } // end for keys2
                        } // End If
                        if (cabecera){
                            break;
                        }
                    } // end for keys
                } else if (!cabeceralista){
                    Set<String> keys = filaObj.keySet();
                    for (String k : keys) {
                        if (k.equals(keycabeceras[1])
                            || k.equals(keycabeceras[2])
                            || k.equals(keycabeceras[3])
                            || k.equals(keycabeceras[4])) {
                            continue;
                        }
                        String val = (filaObj.get(k).getAsString()).trim();
                        // Revisar que el valor que contenga es del tipo numerico
                        Matcher m = pattern_num.matcher(val);
                        if (m.find()) {
                           keycabeceras[0] = "" + k;
                           cabeceralista = true;
                           keycabeceras[keycabeceras.length-1] = "-";
                        }
                        Matcher m2 = pattern_percent.matcher(val);
                        if (m2.find()) {
                           keycabeceras[keycabeceras.length-1] = "" + k;
                        }
                    }
                }
                if (cabeceralista){
                    JsonObject e = processFila(keycabeceras, CABECERASDB, filaObj, pattern_num, pattern_entero);
                    // Si aparece la linea TOTAL , dejar de leer el excel
                    String x = leeCadenaFilaCapex(filaObj, keycabeceras[1]);
                    if (x!= null && x.trim().equalsIgnoreCase("TOTAL"))
                        break;
                    salida.add(e);
                }
                // 
            } else {
                throw new GesvitaException("No se seleccionó archivo excel o el formato no corresponde (2)");
            }
        }
        return salida;
    }
    
    private JsonObject processFila(String keycabeceras[], String keycabecerasdb[], JsonObject fila, Pattern pattern_num, Pattern pattern_entero){
        JsonObject salida = new JsonObject();
        
        salida.addProperty(keycabecerasdb[0], leeNumeroFilaCapex(fila, keycabeceras[0], pattern_num));
        salida.addProperty(keycabecerasdb[1], leeCadenaFilaCapex(fila, keycabeceras[1]));

        salida.addProperty(keycabecerasdb[2], leeEnteroFilaCapex(fila, keycabeceras[2], pattern_entero));
        salida.addProperty(keycabecerasdb[3], leeEnteroFilaCapex(fila, keycabeceras[3], pattern_entero));
        salida.addProperty(keycabecerasdb[4], leeEnteroFilaCapex(fila, keycabeceras[4], pattern_entero));
        // salida.addProperty(keycabeceras[5],   leePorcentajeFilaCapex(filaDB, keycabeceras[5]));
        return salida;
    }
    
    private JsonObject cloneFilaMasAsterisco(String keycabeceras[], JsonObject fila, Pattern pattern_num, Pattern pattern_entero){
        JsonObject salida = new JsonObject();
        double valD;
        String valS;
        valD = leeNumeroFilaCapex(fila, keycabeceras[0], pattern_num);
        // logger.info("D");
        if (valD != 0D)
            salida.addProperty(keycabeceras[0], valD );
        // logger.info("S");
        valS = leeCadenaFilaCapex(fila, keycabeceras[1]);
        // logger.info("S1");
        if (valS != null)
            salida.addProperty(keycabeceras[1], "* " + valS);
        else
            return null;
        // logger.info("L");
        salida.addProperty(keycabeceras[2], leeEnteroFilaCapex(fila, keycabeceras[2], pattern_entero));
        salida.addProperty(keycabeceras[3], leeEnteroFilaCapex(fila, keycabeceras[3], pattern_entero));
        salida.addProperty(keycabeceras[4], leeEnteroFilaCapex(fila, keycabeceras[4], pattern_entero));
        return salida;
    }
    
    private double leeNumeroFilaCapex(JsonObject fila, String field, Pattern pattern_num) {
        if (fila.has(field)) {
            String val = fila.get(field).getAsString().trim();
            Matcher m = pattern_num.matcher(val);
            if (m.find()) {
                return Double.parseDouble(val);
            } else {
                return 0D;
            }
        } else {
            return 0D;
        }
    }

    private String leeCadenaFilaCapex(JsonObject fila, String field) {
        if (fila.has(field)) {
            String val = fila.get(field).toString();
            if (val == null)
                return null;
            val = val.trim();
            if (val.equalsIgnoreCase("Null"))
                return null;
            for (int i=0;i<5;i++) {
                if ((val.startsWith("\\") && val.endsWith("\\"))
                    || (val.startsWith("\"") && val.endsWith("\""))
                    || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                } else if ((val.startsWith("\\\"") && val.endsWith("\\\""))){
                    val = val.substring(2, val.length() - 2);
                } else break;
            }
            return val;
        } else {
            return null;
        }
    }

    private long leeEnteroFilaCapex(JsonObject fila, String field, Pattern pattern_entero) {
        if (fila.has(field)) {
            String val = fila.get(field).getAsString().trim();
            Matcher m = pattern_entero.matcher(val);
            if (m.find()) {
                return Long.parseLong(val.replaceAll(",", ""));
            } else {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    private JsonElement leerJsonDeDB(Logger logger, Connection conn, JsonObject jsonObj) throws GesvitaException , SQLException {
        String[] camposytipos = {"ID_PROYECTO","I","ID_TAREA_HIJO","I","DATA_EXTENDIDA_HIJO","C"};
        JsonObject json_consulta = new JsonObject();
        if (jsonObj.has("in_id_proyecto") && jsonObj.has("in_id_tarea") && jsonObj.has("in_id_usuario") ) {
            json_consulta.addProperty("id_proyecto", limpiaNum(jsonObj.get("in_id_proyecto").toString()));
            json_consulta.addProperty("id_tarea_hijo", limpiaNum(jsonObj.get("in_id_tarea").toString()));
        } else {
            throw new GesvitaException("Los campos in_id_proyecto, in_id_usuario e in_id_tarea son obligatorios");
        }
        // BYSECURITY logger.info ("Entrada Consulta" + json_consulta.toString());
        // VW_PROYECTO_SUBTAREAS
        
        
        String json;
        // Alternativa 1: Consulta a BD sin uso de biblioteca
         json = consultaDB(logger,conn,json_consulta); 
        // Alternativa 2: Consulta a BD con uso de biblioteca
//        json = ObtenerLib.getDefaultObtenerResoultSetExt(logger, conn,
//                camposytipos, "VW_PROYECTO_SUBTAREAS" , json_consulta ,
//                " ID_PROYECTO, ID_TAREA_HIJO, DATA_EXTENDIDA_HIJO " , "" , null);
        
        // json = "[{\"id\":\"1.0\",\"partida\":\"Derechos municipales y permisos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.0\",\"partida\":\"Proyectos de diseño\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.01\",\"partida\":\"Arquitectura\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.02\",\"partida\":\"Cálculo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.03\",\"partida\":\"Revisor independiente\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.04\",\"partida\":\"Topografia\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.05\",\"partida\":\"Mecánica de suelos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.06\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.07\",\"partida\":\"Iluminación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.08\",\"partida\":\"Sistema de seguridad contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.09\",\"partida\":\"Instalaciones de clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.1\",\"partida\":\"Pavimentos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.11\",\"partida\":\"Instalaciones sanitarias\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.12\",\"partida\":\"Impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.13\",\"partida\":\"Cargas combustibles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.14\",\"partida\":\"prevención (CCTV y sistema anti hurto) (Incluido en capex de prevención)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.15\",\"partida\":\"Copia planos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.0\",\"partida\":\"Construcción\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1\",\"partida\":\"Excavaciones masivas y rellenos compactados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.15\",\"partida\":\"Obra Gruesa\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.151\",\"partida\":\"Trabajos preliminares\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1511\",\"partida\":\"Demoliciones, desmontajes, retiro escombros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.1512\",\"partida\":\"Replanteo topográfico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.152\",\"partida\":\"Excavaciones menores (fundaciones, estanques, zanjas, rampas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.153\",\"partida\":\"Obras de Hormigón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.154\",\"partida\":\"Estructura metálica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.155\",\"partida\":\"Cubiertas y revestimientos metálicos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.156\",\"partida\":\"Obras exteriores en terreno propio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.157\",\"partida\":\"Instalaciones (Pasadas, obras civiles y soportes de equipos por Constructora)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.2\",\"partida\":\"Instalaciones sanitarias (incluye gas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.25\",\"partida\":\"Terminaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.0\",\"partida\":\"Adicionales / Imprevistos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.3\",\"partida\":\"Inspección técnica de Obra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.35\",\"partida\":\"Obras de impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Equipos de elevación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.41\",\"partida\":\"Contrato obras eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.42\",\"partida\":\"Subestaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.43\",\"partida\":\"Grupo generador\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.44\",\"partida\":\"Inspección técnica eléctrico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.45\",\"partida\":\"Luminaria\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.5\",\"partida\":\"Instalaciones de Clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.51\",\"partida\":\"Contrato obras climatización\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.52\",\"partida\":\"Equipos Rooftops/Chillers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.53\",\"partida\":\"Equipos Mini split\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.54\",\"partida\":\"Control centralizado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.55\",\"partida\":\"Instalaciones sistema contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Compras de G. de Proyectos en construcción (No incluye Suministros de equipos de instalaciones de ingeniería)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Letreros institucionales en fachada tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"TOTEM\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Correo neumático bidireccional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Docklever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Puertas de Boveda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Tolva pasa valores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Arriendo de contenedores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Decapado y abrillantado de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,0\",\"partida\":\"Habilitación y Equipamiento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,1\",\"partida\":\"Gerencia de Proyectos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1001\",\"partida\":\"Rack de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1002\",\"partida\":\"Estantería Cantilever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1003\",\"partida\":\"Estantería Hi-Cube\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1004\",\"partida\":\"Estantería Out-rriger\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1005\",\"partida\":\"Estentería de Góndola\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1006\",\"partida\":\"Paneles perforados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1007\",\"partida\":\"Entrepaños, planchas trupan\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1008\",\"partida\":\"Accesorios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1009\",\"partida\":\"Check Outs\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.101\",\"partida\":\"Portillones y guías\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento mascotas vivas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1013\",\"partida\":\"Instalaciones electricas exhibiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1015\",\"partida\":\"Caseta caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1016\",\"partida\":\"Mueble caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1017\",\"partida\":\"Muebles especiales patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1018\",\"partida\":\"Perfilera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario tienda (oficinas, arriendo herramientas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento de cosina\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1021\",\"partida\":\"Caja fuerte tesorería\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1022\",\"partida\":\"Armario de seguridad especial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1023\",\"partida\":\"Elevador de plataforma\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1024\",\"partida\":\"Grúas electricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1025\",\"partida\":\"Máquina dimensionadora de alfombra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1026\",\"partida\":\"Máquina dimensionadora de linoleos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1027\",\"partida\":\"Máquina de cables eléctricos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1028\",\"partida\":\"Trasnspaleta electrica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1029\",\"partida\":\"Tronzadora de perfiles de acero\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1031\",\"partida\":\"Máquinas dimensionadoras de madera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1032\",\"partida\":\"Máquina compactadora de cartones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1033\",\"partida\":\"Máquina de tintometría\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1037\",\"partida\":\"Luminaria Segunda etapa (en Racks)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.104\",\"partida\":\"Inspección técnica etapa habilitación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2\",\"partida\":\"Gerencia de administración y adquisiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario de casino\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2001\",\"partida\":\"Sillas, sillones y mesas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2002\",\"partida\":\"Música ambiental\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2003\",\"partida\":\"Lockers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2004\",\"partida\":\"Canastos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2005\",\"partida\":\"Carro con porta bebe\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2006\",\"partida\":\"Carro electrico para minusvalidos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2007\",\"partida\":\"Carro niño tipo auto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2008\",\"partida\":\"Carro cliente auto servicio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2009\",\"partida\":\"Carro de arrastre nacional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.201\",\"partida\":\"Carros Boston\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2011\",\"partida\":\"Carros placas + tableros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2012\",\"partida\":\"Carros SP4\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2013\",\"partida\":\"Escaleras de fibra 8 peldaños\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2014\",\"partida\":\"Escaleras tipo avión\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2015\",\"partida\":\"Sillas de ruedas clientes con canasto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2016\",\"partida\":\"Traspaletas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2017\",\"partida\":\"Napoleón de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2018\",\"partida\":\"Máquina reensecadora de cemento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2019\",\"partida\":\"Turnomatic\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.202\",\"partida\":\"Dispensador de tohallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2021\",\"partida\":\"Máquina de café\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2022\",\"partida\":\"Máquina de Fax\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2023\",\"partida\":\"Televisores 21\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2024\",\"partida\":\"DVD\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2025\",\"partida\":\"Data Show con telón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2026\",\"partida\":\"camara fotográfica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2027\",\"partida\":\"Máquina cortadora de monedas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2028\",\"partida\":\"Máquina cortadora de billetes\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2029\",\"partida\":\"Máquina trituradora de papel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.203\",\"partida\":\"Calculadora para arqueo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2031\",\"partida\":\"Calculador de bolsillo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2032\",\"partida\":\"Dispensador de papel higienico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2033\",\"partida\":\"Máquina enzunchadora\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2034\",\"partida\":\"Fundas extensión uñas grúa horquilla\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3\",\"partida\":\"Gerencia de servicios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.31\",\"partida\":\"Máquina Ingleteadora y mesón en patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.32\",\"partida\":\"Dotación de equipos sala de dimensionado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3201\",\"partida\":\"Mesa corte de vidrio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3202\",\"partida\":\"Atril Montado en riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3203\",\"partida\":\"Atril Fijo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3204\",\"partida\":\"Carro para atriles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3205\",\"partida\":\"Riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3206\",\"partida\":\"Carro de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3207\",\"partida\":\"Máq. / Pulidora de vidro. (Metral modelo SL)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3208\",\"partida\":\"Elementos para cortar vidrio \",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3209\",\"partida\":\"Máq. de corte circular\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.321\",\"partida\":\"mueble perforadora de bisagras\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3211\",\"partida\":\"Atriles de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.33\",\"partida\":\"Dotación arriendo de herramientas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.4\",\"partida\":\"Gerencia de Prevención (CCTV y Sistema Anti Hurto)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.5\",\"partida\":\"Gerencia de sistemas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.6\",\"partida\":\"Gerencia de Diseño de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.7\",\"partida\":\"Bonos y Viáticos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"}]";
        // json= "[{\"ID_PROYECTO\":17995,\"ID_TAREA_HIJO\":261620,\"DATA_EXTENDIDA_HIJO\":[{\"id\":\"1.0\",\"partida\":\"Derechos municipales y permisos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.0\",\"partida\":\"Proyectos de diseño\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.01\",\"partida\":\"Arquitectura\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.02\",\"partida\":\"Cálculo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.03\",\"partida\":\"Revisor independiente\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.04\",\"partida\":\"Topografia\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.05\",\"partida\":\"Mecánica de suelos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.06\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.07\",\"partida\":\"Iluminación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.08\",\"partida\":\"Sistema de seguridad contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.09\",\"partida\":\"Instalaciones de clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.1\",\"partida\":\"Pavimentos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.11\",\"partida\":\"Instalaciones sanitarias\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.12\",\"partida\":\"Impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.13\",\"partida\":\"Cargas combustibles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.14\",\"partida\":\"prevención (CCTV y sistema anti hurto) (Incluido en capex de prevención)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"2.15\",\"partida\":\"Copia planos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.0\",\"partida\":\"Construcción\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1\",\"partida\":\"Excavaciones masivas y rellenos compactados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.15\",\"partida\":\"Obra Gruesa\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.151\",\"partida\":\"Trabajos preliminares\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.1511\",\"partida\":\"Demoliciones, desmontajes, retiro escombros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.1512\",\"partida\":\"Replanteo topográfico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.152\",\"partida\":\"Excavaciones menores (fundaciones, estanques, zanjas, rampas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.153\",\"partida\":\"Obras de Hormigón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.154\",\"partida\":\"Estructura metálica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.155\",\"partida\":\"Cubiertas y revestimientos metálicos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.156\",\"partida\":\"Obras exteriores en terreno propio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.157\",\"partida\":\"Instalaciones (Pasadas, obras civiles y soportes de equipos por Constructora)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.2\",\"partida\":\"Instalaciones sanitarias (incluye gas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.25\",\"partida\":\"Terminaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0.0\",\"partida\":\"Adicionales / Imprevistos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.3\",\"partida\":\"Inspección técnica de Obra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.35\",\"partida\":\"Obras de impacto vial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Equipos de elevación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.4\",\"partida\":\"Instalaciones eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.41\",\"partida\":\"Contrato obras eléctricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.42\",\"partida\":\"Subestaciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.43\",\"partida\":\"Grupo generador\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.44\",\"partida\":\"Inspección técnica eléctrico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.45\",\"partida\":\"Luminaria\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.5\",\"partida\":\"Instalaciones de Clima\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.51\",\"partida\":\"Contrato obras climatización\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.52\",\"partida\":\"Equipos Rooftops/Chillers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.53\",\"partida\":\"Equipos Mini split\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.54\",\"partida\":\"Control centralizado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"3.55\",\"partida\":\"Instalaciones sistema contra incendios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Compras de G. de Proyectos en construcción (No incluye Suministros de equipos de instalaciones de ingeniería)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Letreros institucionales en fachada tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"TOTEM\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Correo neumático bidireccional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Docklever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Puertas de Boveda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Tolva pasa valores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Arriendo de contenedores\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Decapado y abrillantado de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,0\",\"partida\":\"Habilitación y Equipamiento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4,1\",\"partida\":\"Gerencia de Proyectos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1001\",\"partida\":\"Rack de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1002\",\"partida\":\"Estantería Cantilever\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1003\",\"partida\":\"Estantería Hi-Cube\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1004\",\"partida\":\"Estantería Out-rriger\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1005\",\"partida\":\"Estentería de Góndola\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1006\",\"partida\":\"Paneles perforados\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1007\",\"partida\":\"Entrepaños, planchas trupan\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1008\",\"partida\":\"Accesorios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1009\",\"partida\":\"Check Outs\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.101\",\"partida\":\"Portillones y guías\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento mascotas vivas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1013\",\"partida\":\"Instalaciones electricas exhibiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1015\",\"partida\":\"Caseta caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1016\",\"partida\":\"Mueble caja patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1017\",\"partida\":\"Muebles especiales patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1018\",\"partida\":\"Perfilera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario tienda (oficinas, arriendo herramientas)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Equipamiento de cosina\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1021\",\"partida\":\"Caja fuerte tesorería\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1022\",\"partida\":\"Armario de seguridad especial\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1023\",\"partida\":\"Elevador de plataforma\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1024\",\"partida\":\"Grúas electricas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1025\",\"partida\":\"Máquina dimensionadora de alfombra\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1026\",\"partida\":\"Máquina dimensionadora de linoleos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1027\",\"partida\":\"Máquina de cables eléctricos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1028\",\"partida\":\"Trasnspaleta electrica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1029\",\"partida\":\"Tronzadora de perfiles de acero\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1031\",\"partida\":\"Máquinas dimensionadoras de madera\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1032\",\"partida\":\"Máquina compactadora de cartones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1033\",\"partida\":\"Máquina de tintometría\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.1037\",\"partida\":\"Luminaria Segunda etapa (en Racks)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.104\",\"partida\":\"Inspección técnica etapa habilitación\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2\",\"partida\":\"Gerencia de administración y adquisiciones\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"0,0\",\"partida\":\"Mobiliario de casino\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2001\",\"partida\":\"Sillas, sillones y mesas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2002\",\"partida\":\"Música ambiental\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2003\",\"partida\":\"Lockers\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2004\",\"partida\":\"Canastos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2005\",\"partida\":\"Carro con porta bebe\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2006\",\"partida\":\"Carro electrico para minusvalidos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2007\",\"partida\":\"Carro niño tipo auto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2008\",\"partida\":\"Carro cliente auto servicio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2009\",\"partida\":\"Carro de arrastre nacional\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.201\",\"partida\":\"Carros Boston\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2011\",\"partida\":\"Carros placas + tableros\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2012\",\"partida\":\"Carros SP4\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2013\",\"partida\":\"Escaleras de fibra 8 peldaños\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2014\",\"partida\":\"Escaleras tipo avión\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2015\",\"partida\":\"Sillas de ruedas clientes con canasto\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2016\",\"partida\":\"Traspaletas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2017\",\"partida\":\"Napoleón de piso\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2018\",\"partida\":\"Máquina reensecadora de cemento\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2019\",\"partida\":\"Turnomatic\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.202\",\"partida\":\"Dispensador de tohallas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2021\",\"partida\":\"Máquina de café\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2022\",\"partida\":\"Máquina de Fax\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2023\",\"partida\":\"Televisores 21\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2024\",\"partida\":\"DVD\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2025\",\"partida\":\"Data Show con telón\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2026\",\"partida\":\"camara fotográfica\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2027\",\"partida\":\"Máquina cortadora de monedas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2028\",\"partida\":\"Máquina cortadora de billetes\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2029\",\"partida\":\"Máquina trituradora de papel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.203\",\"partida\":\"Calculadora para arqueo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2031\",\"partida\":\"Calculador de bolsillo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2032\",\"partida\":\"Dispensador de papel higienico\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2033\",\"partida\":\"Máquina enzunchadora\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.2034\",\"partida\":\"Fundas extensión uñas grúa horquilla\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3\",\"partida\":\"Gerencia de servicios\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.31\",\"partida\":\"Máquina Ingleteadora y mesón en patio constructor\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.32\",\"partida\":\"Dotación de equipos sala de dimensionado\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3201\",\"partida\":\"Mesa corte de vidrio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3202\",\"partida\":\"Atril Montado en riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3203\",\"partida\":\"Atril Fijo\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3204\",\"partida\":\"Carro para atriles\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3205\",\"partida\":\"Riel\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3206\",\"partida\":\"Carro de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3207\",\"partida\":\"Máq. / Pulidora de vidro. (Metral modelo SL)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3208\",\"partida\":\"Elementos para cortar vidrio \",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3209\",\"partida\":\"Máq. de corte circular\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.321\",\"partida\":\"mueble perforadora de bisagras\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.3211\",\"partida\":\"Atriles de acopio\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.33\",\"partida\":\"Dotación arriendo de herramientas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.4\",\"partida\":\"Gerencia de Prevención (CCTV y Sistema Anti Hurto)\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.5\",\"partida\":\"Gerencia de sistemas\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.6\",\"partida\":\"Gerencia de Diseño de tienda\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"},{\"id\":\"4.7\",\"partida\":\"Bonos y Viáticos\",\"presupuesto\":\"\",\"gasto_real\":\"\",\"saldo\":\"\"}]}]";

        // BYSECURITY logger.info("ok consulta");
        
        // BYSECURITY logger.info("Salida lectura BD: " + json);
        
        JsonParser parser = new JsonParser();
        JsonArray jo = parser.parse(json).getAsJsonArray();
        JsonObject jo_0 = jo.get(0).getAsJsonObject();
        return jo_0.get("DATA_EXTENDIDA_HIJO");
        // return jo_0.getAsJsonArray("DATA_EXTENDIDA_HIJO");
    }
    
    private String limpiaNum(String in) {
        if (in != null && !in.equals("")) {
            if ( in.startsWith("\\\"") && in.endsWith("\\\"") ) {
                return in.substring(2, in.length()-2);
            } else if ( in.startsWith("'") && in.endsWith("'") ) {
                return in.substring(1, in.length()-1);
            } else  if ( in.startsWith("\"") && in.endsWith("\"") ) {
                return in.substring(1, in.length()-1);
            }
        }
        return in;
    }

    private Map guardarEnBd(Connection conn, JsonObject jsonObj, JsonElement jsonObjDB) throws SQLException, GesvitaException {
        CallableStatement stmt;
        Clob clob;
        Map<String, Object> dataRetornar = new HashMap<>();
        
        stmt = conn.prepareCall("{call PKG_MONITOR_UPDATE.PROYECTO_TAREA_DATAEXT(?,?,?,?,?)}");
        clob = conn.createClob();
        stmt.setFloat(1, Float.parseFloat(readFieldString(jsonObj,"in_id_tarea")));
        stmt.setFloat(2, Float.parseFloat(readFieldString(jsonObj,"in_id_usuario")));
        clob.setString(1, jsonObjDB.toString());
        stmt.setClob(3, clob);
        stmt.registerOutParameter(4, Types.NUMERIC);
        stmt.registerOutParameter(5, Types.VARCHAR);
        stmt.execute();
        dataRetornar.put("out_codigo", stmt.getInt(4));
        dataRetornar.put("out_mensaje", stmt.getString(5));
        dataRetornar.put("resultadoEjecucion", "OK");
        return dataRetornar;
        // sendData(response, out, dataRetornar);
    }
    
    private String readFieldString(JsonObject jsonObj, String field) throws GesvitaException {
        String fieldlow = field.toLowerCase();
        if (!jsonObj.has(fieldlow))
            throw new GesvitaException("Expected field: '" + field + "' is not present in the input");
        JsonElement value = jsonObj.get(fieldlow);
        String salida = value.toString();
        if (!(value instanceof JsonArray) && !(value instanceof JsonObject)) {
            if (salida.startsWith("\"") && salida.endsWith("\""))
                salida = salida.substring(1, salida.length()-1);
            else if (salida.startsWith("'") && salida.endsWith("'"))
                salida = salida.substring(1, salida.length()-1);
        }
        return salida;
    }
    
    private long readFieldLong(JsonObject jsonObj, String field) throws GesvitaException {
        try {
            return Long.parseLong(readFieldString(jsonObj, field));
        } catch (NumberFormatException e) {
            throw new GesvitaException("Expected field: '" + field + "' is not a number");
        }
    }

    private String consultaDB(Logger logger, Connection conn, JsonObject json_consulta) throws  SQLException , GesvitaException{
        PreparedStatement stmt;
        String query = "SELECT  ID_PROYECTO, ID_TAREA_HIJO, DATA_EXTENDIDA_HIJO  FROM VW_PROYECTO_SUBTAREAS PPLVIEW WHERE ID_PROYECTO = ? AND ID_TAREA_HIJO = ?";
        stmt = conn.prepareStatement(query);
        stmt.setLong(1, readFieldLong(json_consulta, "id_proyecto"));
        stmt.setLong(2, readFieldLong(json_consulta, "id_tarea_hijo"));
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            long p = rs.getLong("ID_PROYECTO");
            long t = rs.getLong("ID_TAREA_HIJO");
            String dat = ClobUtil.clobToString(rs.getClob("DATA_EXTENDIDA_HIJO"));
            
            JsonParser parser = new JsonParser();
            JsonElement json_dat = parser.parse(dat);
            
            JsonArray json_out = new JsonArray();
            JsonObject item0 = new JsonObject();
            item0.addProperty("ID_PROYECTO", p);
            item0.addProperty("ID_TAREA_HIJO", t);
            item0.add("DATA_EXTENDIDA_HIJO", json_dat);
            json_out.add(item0);
            
            String salida = json_out.toString();
            
            // BYSECURITY logger.info("Salida DB " + salida);
            
            return salida;
        } else{
            return "";
        }
    }


}
