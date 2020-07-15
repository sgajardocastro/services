/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.gesvita.ws.archivoshp.bean.db;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Felipe
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class InterchangeFile implements Serializable {
    private long id;

    private Timestamp timeCreation;

    Timestamp timeUpdate;
    String content;
    byte binaryContent[];
    String mimetype;
    String name;
    String finalname;
    int estado;

}