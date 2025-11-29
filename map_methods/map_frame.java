import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class map_frame extends JPanel
{
	int[][] plotting_data;
	
	public map_frame(int[][] plotting_data)
	{
		super();
		
		this.plotting_data = plotting_data;
		new JPanel();
		setBackground(new Color(255, 255, 255, 1));
		repaint();
	}
	
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		for(int i = 0; i < plotting_data.length; i++)
		{
			int width 	= plotting_data[i][0];
			int x 		= plotting_data[i][1];
			int y 		= plotting_data[i][2];
			int color 	= plotting_data[i][3];
			
			g_2d.setColor(new Color(color, color, color, 1f));
			g_2d.fillRect(x, y, width, 1);
		}
		g_2d.dispose();
	}
}