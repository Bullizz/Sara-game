package mini_games;

import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import main.KEY_HANDLER;

public class LULLE extends JPanel implements Runnable
{
	KEY_HANDLER key_handler;
	
	int width, height;
	
	public LULLE(JFrame frame, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(true);
		
		key_handler = new KEY_HANDLER(frame);
		addKeyListener(key_handler);
		
		frame.add(this);
	}

	@Override
	public void run()
	{
		
	}
}