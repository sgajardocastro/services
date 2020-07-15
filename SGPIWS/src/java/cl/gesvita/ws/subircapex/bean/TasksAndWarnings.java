package cl.gesvita.ws.subircapex.bean;

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

    private EntradaCapex tasks[];
    private Warning avisos[];
}