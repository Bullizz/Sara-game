package menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EndMenu extends JPanel
{
	int width, height;
	
	public EndMenu(JFrame frame, JLabel top, String final_time_str)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
		setBackground(new Color(255, 0, 0));
		FlowLayout left = new FlowLayout(FlowLayout.LEFT, 0, 0);
		setLayout(left);
		setOpaque(true);
			JPanel p = new JPanel();
			p.setBackground(new Color(0, 255, 0));
			p.setOpaque(true);
			add(p, LEFT_ALIGNMENT);
		
		frame.add(this);
		frame.repaint();
	}
}