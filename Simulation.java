// graph vizualization
import com.mxgraph.layout.*;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.*;
// graphs
import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.KleinbergSmallWorldGraphGenerator;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.util.SupplierUtil;
// general graphics
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A simple network-based SIR application that also shows how to use JGraphX to visualize JGraphT graphs.
 */
public class Simulation {
    private Random rand;
    private static final long serialVersionUID = 2202072534703043194L;
    private static final int SIZE = 40; // size^2 is number of vertices
    private static final int CLOSE_FRIEND_THRESHOLD=3;//the threshold of close friend
    private int which_virus;// which virus are this programming simulating
    private static final int ALPHA=0;//id for alpha
    private static final int DELTA=1;//delta id
    private static final int OMICRON=2;//omicron id
    private static final double vaccination_efficiency_alpha=0.93;//data by early research of mRNA vaccination
    private static final double vaccination_efficiency_delta=0.58;//data by cdc.gov: overall, without prior infection
    private static final double vaccination_efficiency_omicron=0.292;//data by cdc.gov: two doses of Janssen, 14 days to 1 month since last dose
    private static final double vaccination_efficiency_omicron_booster=0.6;//data from https://www.cnn.com/2022/11/22/health/vaccine-effectiveness-bivalent-boosters-cdc/index.html#:~:text=The%20new%20study%20found%20that,be%20vulnerable%20to%20breakthrough%20infections.
    private double vac_validity;// effectiveness of vaccination from cdc.gov
    private int initial_size;//initial size of the simulation
    private boolean vaccinated;//whether people in the community is vaccinated or not
    private boolean boostered;//whether people in the community had booster
    private static final Dimension DEFAULT_SIZE = new Dimension(900, 900);
    private HashMap<String, mxICell> vertexToCellMap; // match vertices with drawing cells
    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;
    private Graph graph; // The network of people
    // Simulation variables
    private int time;  // current time (an int >= 0);
    private double transmission_rate = 0.07;  // probability when infected/susceptible meet with friend
    private double close_transmission_rate=0.121;//probability when infected/susceptible meet with close friend
    private double init_disease_probability = 0.02;
    private int infection_duration = 5;
    private int numInfected;
    private int numRecovered=0;

    private JFrame jframe;
    private JTextField infoTextField;

    /**
     * Make a new simulation, including gui
     * every kind of simulation constructor eventually call the last on
     * by defult, the value for vaccination is false, the value for booster is false,
     * and the virus type is alpha
     */
    public Simulation() {
        this(20,false,true,0);
    }

    public Simulation(int population) {
        this(population, false,true,0);
    }
    public Simulation(int population,boolean vaccinated){
        this(population, vaccinated,true,0);
    }
    public Simulation(int population, boolean vaccinated, int virus_type){
        this(population,vaccinated,false,virus_type);
    }
    public Simulation(int population, boolean vaccinated,boolean booster, int virus_type){
        this.which_virus=virus_type;
        //initialized the vaccination validity from the input of type of virus
        if (which_virus==Simulation.ALPHA){
            vac_validity=vaccination_efficiency_alpha;
        }else if (which_virus==Simulation.DELTA){
            vac_validity=vaccination_efficiency_delta;
        }else{
            vac_validity=vaccination_efficiency_omicron;
        }
        Person.reset_id();
        this.vaccinated=vaccinated;
        boostered=booster;
        initial_size = Math.abs(population);
        this.rand = new Random();
        time = 0;
        graph = createSmallWorld();
        init_gui();
    }

    public void init_gui() {
        ListenableGraph<String, DefaultEdge> g =
                new DefaultListenableGraph<>(graph);

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
        jframe = new JFrame("SIR Sim");
        infoTextField = new JTextField("Time: " + time);
        infoTextField.setEditable(false);
        jframe.setSize(DEFAULT_SIZE);
        Container jframe_comp = jframe.getContentPane();
        mxGraphComponent graphcomponent = new mxGraphComponent(jgxAdapter);
        graphcomponent.setConnectable(false);
        graphcomponent.getGraph().setAllowDanglingEdges(false);
        jframe_comp.setLayout(new BorderLayout());
        jframe_comp.add(BorderLayout.NORTH, infoTextField);
        jframe_comp.add(BorderLayout.CENTER, graphcomponent);

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);

        // positioning via jgraphx layouts
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 350;
        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());
        vertexToCellMap = jgxAdapter.getVertexToCellMap();
        colorVertices();
    }


    // Create the VertexFactory so the generator can create vertices
    Supplier<Person> vSupplier = new Supplier<Person>() {
        @Override
        public Person get() {
            return new Person(Simulation.this);
        }
    };

    /**
     * global time
     */
    public int time() {
        return this.time;
    }

    /**
     * duration for each individual of the infection
     */
    public int infectionDuration() {
        return infection_duration;
    }

    // Generates a completely connected graph.
    public Graph createCompleteGraph() {

        // Create the graph object
        Graph<Person, DefaultEdge> completeGraph =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);

        // Create the CompleteGraphGenerator object
        CompleteGraphGenerator<Person, DefaultEdge> completeGenerator =
                new CompleteGraphGenerator<>(SIZE);

        // Use the CompleteGraphGenerator object to make completeGraph a
        // complete graph with [size] number of vertices
        completeGenerator.generateGraph(completeGraph);
        return completeGraph;
    }

    /**
     * Create small world graph.
     */
    public Graph createSmallWorld() {

        // Create the graph object
        Graph<Person, DefaultEdge> smallworld =
                //Graph<Person, DefaultWeightedEdge> smallworld =
                new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
        //new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), false);
        //createDefaultWeightedEdgeSupplier

        /*
        n - generate set of lattice points in a n by n square
        p - lattice distance for which each node is connected to every other node in the lattice (local connections)
        q - how many long-range contacts to add for each node
        r - probability distribution parameter which is a basic structural parameter
            measuring how widely "networked" the underlying society of nodes is
         */
//        KleinbergSmallWorldGraphGenerator<Person, DefaultEdge> generator =
//                new KleinbergSmallWorldGraphGenerator(SIZE, 1, 2, 2);
        KleinbergSmallWorldGraphGenerator<Person, DefaultEdge> generator =
                new KleinbergSmallWorldGraphGenerator(initial_size, 1, 2, 2);
        //new KleinbergSmallWorldGraphGenerator(initial_size, 1, 2, 2);
        generator.generateGraph(smallworld);
        return smallworld;
    }


    // run the simulation with pausing
    public void run() {
        initDisease(init_disease_probability);
        while (numInfected > 0) {
            cycle();
            // next line is hack.  best commented out!
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
    }
    /////////////////////////////
    //give each of node in a graph a social network, vertecies nearby the vertex represent close friends
    //vertecies not close to the vertex represent friends
    //when one vertex does not connected to other vertex their relationship is stranger
    //as the difference of id number is smaller than 3, we consider them as close friend
    //if the difference of id number is bigger equal than 4, we consider them as close friend
    //since bard is a fully vaccinated campus, we assume stranger doesn't pass virus to the other students


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // One cycle of simulation.
    // All infected nodes pass disease to all friends with some probability.
    private void cycle() {
        time++; // global clock update
        //stem.out.println(numRecovered);//
        System.out.println(500-numRecovered);
        infoTextField.setText("Time: " + time);
        Iterator<Person> iter = new DepthFirstIterator<>(graph);
        while (iter.hasNext()) {
            Person vertex = iter.next();
            if (vertex.update(time) && vertex.getStatus() == Person.RECOVERED) {
                numInfected--;
            }
            // infected persons can transmit
            if (vertex.getStatus() == Person.INFECTIOUS) {
                Set<DefaultEdge> edges = graph.edgesOf(vertex);
                for (DefaultEdge e : edges) {

                    Person p = (Person) (graph.getEdgeTarget(e));
                    //loop through the graph and determine which connection is close_friends connections and which connections are friedns connections
                    int diff=Math.abs(vertex.get_id()-p.get_id());
                    if (diff<CLOSE_FRIEND_THRESHOLD) p.set_close();
                    if(diff>=CLOSE_FRIEND_THRESHOLD) p.set_friend();
                    if (p.equals(vertex)) p = (Person) (graph.getEdgeSource(e));
                    //System.out.println(p.get_network());
                    if(vaccinated==false) {//if not vaccinated, run normally

                        //System.out.println("vert " + vertex + " edge " + e + " targ " + p);
                        if (p.get_network() == Person.FRIEND) {
                            if (p.getStatus() == Person.SUSCEPTIBLE &&
                                    rand.nextDouble() < transmission_rate) { // transmit disease to p
                                p.setStatus(Person.INFECTIOUS);
                                numInfected++;
                                numRecovered++;
                            }
                        } else {
                            if (p.getStatus() == Person.SUSCEPTIBLE &&
                                    rand.nextDouble() < close_transmission_rate) { // transmit disease to p
                                p.setStatus(Person.INFECTIOUS);
                                numInfected++;
                                numRecovered++;
                            }
                        }
                    }else{//if vaccinated, considering about the effectiveness of vaccination
                        if (which_virus==Simulation.OMICRON&&boostered==true){
                            //System.out.println("got to this point" );
                            if (p.get_network() == Person.FRIEND) {
                                if (p.getStatus() == Person.SUSCEPTIBLE &&
                                        rand.nextDouble() < transmission_rate*(1-vaccination_efficiency_omicron_booster)) { // transmit disease to p
                                    p.setStatus(Person.INFECTIOUS);
                                    numInfected++;
                                    numRecovered++;
                                }
                            } else {
                                if (p.getStatus() == Person.SUSCEPTIBLE &&
                                        rand.nextDouble() < close_transmission_rate*(1-vaccination_efficiency_omicron_booster)) { // transmit disease to p
                                    p.setStatus(Person.INFECTIOUS);
                                    numInfected++;
                                    numRecovered++;
                                }
                            }

                        }else {
                            //System.out.println("vert " + vertex + " edge " + e + " targ " + p);
                            if (p.get_network() == Person.FRIEND) {
                                if (p.getStatus() == Person.SUSCEPTIBLE &&
                                        rand.nextDouble() < transmission_rate * (1 - vac_validity)) { // transmit disease to p
                                    p.setStatus(Person.INFECTIOUS);
                                    numInfected++;
                                    numRecovered++;
                                }
                            } else {
                                if (p.getStatus() == Person.SUSCEPTIBLE &&
                                        rand.nextDouble() < close_transmission_rate * (1 - vac_validity)) { // transmit disease to p
                                    p.setStatus(Person.INFECTIOUS);
                                    numInfected++;
                                    numRecovered++;
                                }
                            }
                        }
                    }
                }
            }
            setColor(vertex);
        }
    }

    // initialize disease in the world.
    private void initDisease(double initprob) {
        Iterator<Person> iter = new DepthFirstIterator<>(graph);
        while (iter.hasNext()) {
            Person vertex = iter.next();
            if (rand.nextDouble() < initprob) {
                vertex.setStatus(Person.INFECTIOUS);
                numInfected++;
                numRecovered++;
            } else {
                vertex.setStatus(Person.SUSCEPTIBLE);
            }
            setColor(vertex);
        }
    }

    //print out the result to the terminal window
    public void result() {
        double prop=numRecovered/(Math.pow(initial_size,2));
        double health= (Math.pow(initial_size,2))-numRecovered;
        double health_rate= health/(Math.pow(initial_size,2));
        System.out.println("number of people being infected: "+ numRecovered);
        System.out.println("proportion of people being infected: "+ prop);
        System.out.println("number of people that has not being infected: "+ health);
        System.out.println("number of people that stay healthy during this time"+ health_rate);
        System.out.println("number of days the virus spreading: "+ time);
        System.out.println( time);
    }



    // Set vertex color to indicate status.
    private void setColor(Person p) {
        String color = p.getColor();
        Object[] obj = new Object[1];
        obj[0] = (Object) (vertexToCellMap.get(p));
        //System.out.println(jgxAdapter.getCellStyle(p));
        jgxAdapter.setCellStyle("shape=ellipse;fillColor=" + color, obj); //#FFFFFF", obj);
    }

    // color all vertices
    public void colorVertices() {
        Iterator<Person> iter = new DepthFirstIterator<>(graph);
        while (iter.hasNext()) {
            Person p = iter.next();
            setColor(p);
        }
    }

    /**
     * An alternative starting point for this demo, to also allow running this applet as an
     * application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation();
        sim.run(); // run the simulation
    }

}