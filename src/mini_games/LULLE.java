package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.KeyHandler;

public class Lulle extends JPanel implements Runnable
{
	KeyHandler key_handler;
	
	Thread lulle_thread;
	int width, height;
	boolean game_paused = false, dirt_placed = false;
	final int RNG = 10;
	
	public Lulle(JFrame frame, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(true);
		setBackground(new Color(32, 89, 255));
		
		key_handler = new KeyHandler(frame);
		addKeyListener(key_handler);
		
		frame.add(this);
		frame.repaint();
	
		initLulleThread();
	}

	private void initLulleThread()
	{
		lulle_thread = new Thread(this);
		lulle_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(lulle_thread != null)
		{
			double draw_interval = Math.pow(10, 9);
			draw_interval /= FPS;
			double delta = 0;
			long last_time = System.nanoTime();
			long current_time;
			
			boolean game_paused = key_handler.isGame_paused();
			
			// Slave game-loop
			while(!game_paused)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{
					update();
					
					repaint();
					
					delta = 0;
				}
			} // End of slave game-loop
		} // End of master game-loop
	}

	private void update()
	{
		int dirt_rng = (int) (Math.random() * RNG);
		if(dirt_rng == 0 && !dirt_placed)
		{
			dirt_placed = true;
		}
		else
			dirt_placed = false;
		
		
	}
	
	@Override
	public void paintComponent(Graphics g_1d)
	{
		
	}
}