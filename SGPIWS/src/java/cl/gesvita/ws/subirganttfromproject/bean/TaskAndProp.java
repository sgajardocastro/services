package cl.gesvita.ws.subirganttfromproject.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.sf.mpxj.Task;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskAndProp {
    Task task;
    long idPadre;
    int level;
    double idtask;
}
