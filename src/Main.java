import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args)
    {
        JFrame jFrame = new JFrame("Chess 2");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);

        Image icon = new ImageIcon(Main.class.getClassLoader().getResource("icon.png")).getImage();
        jFrame.setIconImage(icon);

        Window window = new Window();
        jFrame.add(window);
        jFrame.addMouseListener(window);
        jFrame.pack();

        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        while(true)
        {
            window.update();
            window.repaint();
        }
    }

}
