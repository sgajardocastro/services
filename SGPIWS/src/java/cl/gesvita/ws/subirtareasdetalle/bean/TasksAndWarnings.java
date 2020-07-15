package cl.gesvita.ws.subirtareasdetalle.bean;

import java.io.Serializable;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@ToString
public class TasksAndWarnings implements Serializable {

    private Task tasks[];
    private Warning avisos[];
}