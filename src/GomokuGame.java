import javax.swing.*;

public class GomokuGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Five In a Row");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(910, 680);
        frame.setResizable(false);
        frame.add(new GamePanel()); 
        frame.setVisible(true);
    }
} 