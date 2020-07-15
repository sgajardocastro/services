package cl.gesvita.ws.obtener.lib;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cl.gesvita.ws.obtener.bean.EntradaCapex;
import cl.gesvita.ws.obtener.exception.GesvitaException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Felipe
 */
public class ExcelLeeCapex {
    static Pattern pattern_num = null;
    static Pattern pattern_entero;
    static Pattern pattern_percent;
    
    private static void init_patterns() {
        if (pattern_num == null) return;
        String regex_num = "^[\\d]+\\.[\\d]+$";
        pattern_num = Pattern.compile(regex_num);
        String regex_entero = "^[\\d\\,]+$";
        pattern_entero = Pattern.compile(regex_entero);
        String regex_percent = "^[\\d]+\\.[\\d]{2}\\%$";
        pattern_percent = Pattern.compile(regex_percent);
    }
    
    private static EntradaCapex[] parseExcel(String in) throws GesvitaException {
        init_patterns();
        final String CABECERAS[] = {"id","partida","presupuesto","gasto real","saldo","porcentaje"};
        List<EntradaCapex> salida = null;
        JsonParser parser = new JsonParser();
        JsonElement jo = parser.parse(in);
        boolean cabecera = false;
        boolean cabeceralista = false;
        int i=-1;
        String keycabeceras[] = new String[CABECERAS.length];
        if (jo instanceof JsonArray) {
            JsonArray arr;
            arr = (JsonArray) jo;
            // Para Cada Cada Fila
            for (JsonElement fila:arr) {
                if (fila instanceof JsonObject) {
                    JsonObject filaObj = (JsonObject) fila;
                    if (!cabecera){
                        Set<String> keys = filaObj.keySet();
                        // Para cada llave dentro de la fila
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
                               salida = new ArrayList<>();
                               keycabeceras[keycabeceras.length-1] = "-";
                            }
                            Matcher m2 = pattern_percent.matcher(val);
                            if (m2.find()) {
                               keycabeceras[keycabeceras.length-1] = "" + k;
                            }
                        }
                    }
                    if (cabeceralista){
                        salida.add(processFila(keycabeceras, filaObj));
                    }
                } else {
                    throw new GesvitaException("Error en el formato de excel");
                }
            }
        } else {
            throw new GesvitaException("Error en el formato de excel");
        }
        if (salida == null) {
            throw new GesvitaException("Error en el formato de excel");
        } else {
            EntradaCapex[] x = new  EntradaCapex[salida.size()];
            salida.toArray(x);
            return x;
        }
    }
    
    private static EntradaCapex processFila(String keycabeceras[], JsonObject fila){
        EntradaCapex salida = new EntradaCapex();
        salida.setId(leeNumeroFilaCapex(fila, keycabeceras[0]));
        salida.setPartida(leeCadenaFilaCapex(fila, keycabeceras[1]));
        salida.setPresupuesto(leeEnteroFilaCapex(fila, keycabeceras[2]));
        salida.setGastoreal(leeEnteroFilaCapex(fila, keycabeceras[3]));
        salida.setSaldo(leeEnteroFilaCapex(fila, keycabeceras[4]));
        salida.setPorcentaje(leePorcentajeFilaCapex(fila, keycabeceras[5]));
        return salida;
    }
    
    private static double leeNumeroFilaCapex( JsonObject fila, String field) {
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

    private static String leeCadenaFilaCapex(JsonObject fila, String field) {
        if (fila.has(field)) {
            String val = fila.get(field).getAsString().trim();
            return val;
        } else {
            return null;
        }
    }

    private static long leeEnteroFilaCapex(JsonObject fila, String field) {
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

    private static double leePorcentajeFilaCapex(JsonObject fila, String field) {
        if (fila.has(field)) {
            String val = fila.get(field).getAsString().trim();
            Matcher m = pattern_percent.matcher(val);
            if (m.find()) {
                return Double.parseDouble(val.substring(0,val.length()-1));
            } else {
                return 0D;
            }
        } else {
            return 0D;
        }
    }
}
