package mini_games;

import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import main.KeyHandler;

public class Lulle extends JPanel implements Runnable
{
	KeyHandler key_handler;
	
	int width, height;
	
	public Lulle(JFrame frame, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(true);
		
		key_handler = new KeyHandler(frame);
		addKeyListener(key_handler);
		
		frame.add(this);
	}

	@Override
	public void run()
	{
		
	}
}