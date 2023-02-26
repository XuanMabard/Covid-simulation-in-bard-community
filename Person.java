import java.io.Serializable;

/** Persons are used as vertices in the graph. */
public class Person implements Serializable {
    public static final int SUSCEPTIBLE = 0;
    public static final int INFECTIOUS = 1;
    public static final int RECOVERED = 2;
    public static final int NEVER = -1;
    private int network;
    public static final int CLOSE_FRIEND=0;
    public static final int FRIEND=1;
    public static final int STRANGER=2;
    private static int ID = 0;
    public int p_id;//the id that used to determine the close friend or not very close

    private int id; // unique identifier
    private int status;
    private int dateInfected;
    private int dateRecovered;
    private Simulation simulation;
    private static final String[] colors = {"#FFFFFF", "#FF0000", "#00FF00"};  // S/I/R


    public Person(Simulation sim) {
        simulation = sim;
        id = ID++;
        p_id=id;
        status = SUSCEPTIBLE;
        network=STRANGER;
        dateInfected = NEVER;
        dateRecovered = NEVER;
    }
    public void set_close(){
        network=CLOSE_FRIEND;
    }
    public void set_friend(){
        network=FRIEND;
    }
    public int get_id() {
        return p_id;
    }
    public int get_network(){
        return network;
        }

    // set status and record infectious/recovered time info.
    public void setStatus(int s) {
        status = s;
        if (status == INFECTIOUS) {
            dateInfected = simulation.time();
        } else if (status == RECOVERED) {
            dateRecovered = simulation.time();
        }
    }
    public static void reset_id(){
        ID=0;
    }
    public int getStatus() {
        return status;
    }

    /** Update status of person by one time click.
     * return true if status changes */
    public boolean update(int time) {
        boolean statuschanged = false;

        if (status == INFECTIOUS) {
            if (time - dateInfected > simulation.infectionDuration()) {
                status = RECOVERED;
                statuschanged = true;
            }
        }
        return statuschanged;
    }

    public boolean equals(Person p) {
        return p.id == this.id;
    }
    @Override
    public String toString() {
        return Integer.toString(id);
    }
    public String getColor() {
        return colors[status];
    }
}

