package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.GameTimer;
import main.KeyHandler;

public class Pauline extends JPanel implements Runnable
{
	int width, height;
	Thread pauline_thread;
	boolean game_loop_running, game_paused = false;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;

	// Player parameters
	int player_max_speed = 7;
	int player_x 		 = 0;
	int player_y 		 = 0;
	int player_width	 = 112;
	int player_height	 = 194;
	int player_speed_x 	 = 0;
	boolean carrying	 = false;
	ImageIcon player_img;
	
	// Item rain parameters
	String[] item_array   = {"song_book", "julmust", "pain_suprise", "christmas_card"};
	String[] status_array = {"default", "default", "default", "default"};
	int rng_lim = item_array.length;
	
	public Pauline(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
//		setBackground(new Color(0, 128, 0));
		
		this.player_y = this.height - player_height;
		this.player_x = 3 * (this.width / 5); 
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
		this.setLayout(new GridLayout(1, 9));
		
		for(int i = 0; i < 9; i++)
		{
			JPanel p = new JPanel();
			if(i < 3)
				p.setBackground(new Color(0, 128, 0));
			else
			{
				int r = (int) (Math.random() * 256);
				int g = (int) (Math.random() * 256);
				int b = (int) (Math.random() * 256);
				p.setBackground(new Color(r, g, b));
			}
			p.setOpaque(true);
			this.add(p);
		}
		
		this.setOpaque(true);
		frame.add(this);
		frame.repaint();
		
		initPaulineThread();
	}

	private void initPaulineThread()
	{
		game_loop_running = true;
		pauline_thread = new Thread(this);
		
		pauline_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(pauline_thread != null)
		{
			double draw_interval = Math.pow(10, 9);
			draw_interval /= FPS;
			double delta = 0;
			long last_time = System.nanoTime();
			long current_time;
			
			boolean game_paused = key_handler.isGame_paused();
			
			// Slave game-loop
			while(game_loop_running && !game_paused)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{					
					updatePlayer();
					
					updateItemRain();
					
					repaint();
					
					delta = 0;
				}
			}
		}
	}

	private void updatePlayer()
	{
		int[] direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of player
		int player_dx = direction_arr[0];
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < player_max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;
		
		// If within map constraints
		if(moveable(player_x, player_dx))
			player_x += player_speed_x * player_dx;
	}


	private void updateItemRain()
	{
		int falling_position = (int) (Math.random() * rng_lim);
		
		System.out.println();
	}
	
	
	private boolean moveable(int player_x, int dx)
	{
		if(dx > 0)
			player_x += player_width;
		
		// Left
		if(dx < 0 && player_x > this.width / 3)
			return true;
		// Right
		else if(dx > 0 && player_x < this.width)
			return true;
		
		return false;
	}

	/*
	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint table
		
		// Paint player
		g_2d.setColor(Color.PINK);
		g_2d.fillRect(player_x, player_y, player_width, player_height);

		// Paint items <falling|carrying>
		
		g_2d.dispose();
		
	}
	*/
}