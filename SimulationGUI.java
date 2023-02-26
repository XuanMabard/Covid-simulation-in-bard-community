import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class SimulationGUI {
    private Simulation sim;
    private JFrame frame;
    private JTextField size;
    private Integer sim_size;
    private int which_virus;
    private JPanel upper;
    private JPanel middle;
    private JPanel lower;
    private JLabel label1;
    //asking whether they are vacinnated or not
    private JLabel label2;
    private JButton start_button;//click to start simulation
    private JButton vaccinated_true;//click to determine whether the community is vaccinated or not, by default no one get vaccinated.
    private JButton vaccinated_false;
    private JButton vaccinated_booster;
    private JButton virus_alpha;//to simulate the Alpha variant
    private JButton virus_delta;//to simulate the delta variant
    private JButton virus_omicron;//to simulate the omicron variant
    private boolean vaccinated_value=false;//by default, the all students haven't got the vaccination
    private boolean booster_value=false;

    public SimulationGUI() {
        which_virus=0;//if the species of variants not specefied by user, then simulate alpha by default
        Font font = new Font("TimesRoman", Font.BOLD, 18);
        frame = new JFrame("Covid simulation");//frame
        upper = new JPanel();
        middle=new JPanel();
        lower = new JPanel();
        size = new JTextField("enter desired size of simulation");
        size.setFont(font);
        size.setPreferredSize(new Dimension(300, 80));
        //add handler to the textfield
        TextFieldHandler handler = new TextFieldHandler();
        size.addActionListener(handler);
        label1=new JLabel();
        label1.setText("Size");
        label1.setHorizontalTextPosition(JLabel.CENTER);
        label2= new JLabel();
        label2.setText("vaccinated or not");
        label2.setHorizontalTextPosition(JLabel.CENTER);
        //button to initialize the simulation
        //button handler, if pressed, start the simulation
        ButtonHandler handler_button = new ButtonHandler();
        VaccinatedTrue vac_true=new VaccinatedTrue();
        VaccinatedFalse vac_false=new VaccinatedFalse();
        VaccinatedBooster vac_boo=new VaccinatedBooster();
        AlphaButton alp_but=new AlphaButton();
        DeltaButton del_but=new DeltaButton();
        OmicronButton omi_but=new OmicronButton();
        start_button= new JButton();
        start_button.setVisible(false);
        start_button.addActionListener(handler_button);
        start_button.setText("press to start simulation!");
        vaccinated_true=new JButton();
        vaccinated_true.setVisible(true);
        vaccinated_true.addActionListener(vac_true);
        vaccinated_true.setText("yes!");
        vaccinated_false=new JButton();
        vaccinated_false.setVisible(true);
        vaccinated_false.addActionListener(vac_false);
        vaccinated_false.setText("no!");
        vaccinated_booster=new JButton();
        vaccinated_booster.addActionListener(vac_boo);
        vaccinated_booster.setVisible(true);
        vaccinated_booster.setText("booster?");
        virus_alpha=new JButton();
        virus_alpha.addActionListener(alp_but);
        virus_alpha.setVisible(true);
        virus_alpha.setText("Alpha!");
        virus_delta=new JButton();
        virus_delta.addActionListener(del_but);
        virus_delta.setVisible(true);
        virus_delta.setText("Delta!");
        virus_omicron=new JButton();
        virus_omicron.addActionListener(omi_but);
        virus_omicron.setVisible(true);
        virus_omicron.setText("Omicron!");
        upper.add(label1);
        upper.add(size);
        upper.add(start_button);
        middle.add(label2,BorderLayout.NORTH);
        middle.add(vaccinated_true,BorderLayout.NORTH);
        middle.add(vaccinated_false);
        middle.add(vaccinated_booster);
        lower.add(virus_alpha);
        lower.add(virus_delta);
        lower.add(virus_omicron);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set the jframe size and location, and make it visible
        frame.setPreferredSize(new Dimension(600, 200));
        frame.pack();
        frame.setLocationRelativeTo(null);
        //frame.add(button);
        frame.add(upper,BorderLayout.NORTH);
        frame.add(middle,BorderLayout.CENTER);
        frame.add(lower,BorderLayout.SOUTH);
        //frame.add(panel);
        frame.setVisible(true);


    }
    //text handler of the size text
    private class TextFieldHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Integer size_of_sim;
            size_of_sim = Integer.parseInt(size.getText());
            sim_size = Math.toIntExact(Math.round(Math.sqrt(size_of_sim)));
            System.out.println(sim_size);
            start_button.setVisible(true);
        }
    }
    public static void main(String[] args) {
        SimulationGUI gui = new SimulationGUI();
//        gui.addWindowListener(
//                new WindowAdapter() {
//                    public void windowClosing( WindowEvent e )
//                    {
//                        gui.dispose();
//                    }
//                }
//        );
    }
    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //sim = new Simulation();
            //sim=new Simulation(sim_size);
            //for (int i =0;i<10;i++) {
                sim = new Simulation(sim_size, vaccinated_value, booster_value, which_virus);
                sim.run(); // run the simulation
                sim.result();
            //}
        }
    }
    private class VaccinatedTrue implements ActionListener{
        public void actionPerformed(ActionEvent e){
            vaccinated_value=true;
            booster_value=false;
            System.out.println(booster_value);
            vaccinated_true.setText("all students are vaccinated!");
            vaccinated_false.setText("no!");
            vaccinated_booster.setText("no booster!");
        }
    }
    private class VaccinatedFalse implements ActionListener{
        public void actionPerformed(ActionEvent e){
            vaccinated_value=false;
            booster_value=false;
            //System.out.println(vaccinated_value);
            vaccinated_false.setText("all students are not vaccinated!");
            vaccinated_true.setText("yes!");
            vaccinated_booster.setText("no booster!");
        }
    }
    private class VaccinatedBooster implements ActionListener{
        public void actionPerformed(ActionEvent e){
            booster_value=true;
            vaccinated_value=true;
            System.out.println(booster_value);
            vaccinated_true.setText("vaccinated!");
            vaccinated_false.setText("no!");
            vaccinated_booster.setText("got booster!");
        }
    }
    private class AlphaButton implements ActionListener{
        public void actionPerformed(ActionEvent e){
            which_virus=0;
            System.out.println("you chose to simulate alpha");
            virus_alpha.setText("you chose Alpha!");
            virus_delta.setText("Delta!");
            virus_omicron.setText("Omicron!");
        }
    }
    private class DeltaButton implements ActionListener{
        public void actionPerformed(ActionEvent e){
            which_virus=1;
            System.out.println("you chose to simulate delta");
            virus_delta.setText("you chose Delta!");
            virus_alpha.setText("Alpha!");
            virus_omicron.setText("Omicron!");
        }
    }
    private class OmicronButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            which_virus = 2;
            System.out.println("you chose to simulate omicron");
            virus_alpha.setText("Alpha!");
            virus_delta.setText("Delta!");
            virus_omicron.setText("you chose Omicron!");
        }
    }
    private void reset(){
        System.out.println("reset!");
        vaccinated_true.setText("yes!");
        vaccinated_false.setText("no!");
        vaccinated_booster.setText("booster?");
        virus_alpha.setText("Alpha!");
        virus_delta.setText("Delta!");
        virus_omicron.setText("Omicron!");
    }
}


