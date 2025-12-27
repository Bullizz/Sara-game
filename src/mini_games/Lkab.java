package mini_games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.GameTimer;
import main.KeyHandler;

public class Lkab extends JPanel implements Runnable
{
	int width, height;
	Thread lkab_thread;
	boolean game_loop_running, game_paused = false;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	// Player parameters
	int player_max_speed  = 4;
	int player_x 		  = 1689;
	int player_y 		  = 584;
	double player_speed_x = 0;
	double player_speed_y = 0;
	int player_width	  = 112;
	int player_height	  = 145;
	ImageIcon player_img;

	// Wires parameters
	Color active_color;
	int wire_width;
	int wire_x;
	int wire_y;
	boolean wire_grabbed = false;
	
	// Wire src. parameters
	char[] wire_src_1 = new char[3];
	char[] wire_src_2 = new char[3];
	int wire_src_width; 
	int wire_src_height;
	
	public Lkab(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width  = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		setBackground(new Color(0, 64, 0));
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
	
		frame.add(this);
		frame.repaint();
		
		wire_src_1 		= initWireSrcArr();
		wire_src_2 		= initWireSrcArr();
		wire_src_width  = width / 40;
		wire_src_height = height / 7;
		
		wire_width = wire_src_height / 5;
		
		initLkabThread();
	}

	private char[] initWireSrcArr()
	{
		char[] ret_arr = new char[3];
		char[] arr = {'r', 'g', 'b'};
		int index = 0;
		while(index < arr.length)
		{
			int i = (int) (Math.random() * arr.length);
			if(arr[i] != '0')
			{
				ret_arr[index] = arr[i];
				arr[i] = '0';
				index++;
			}
		}
		
		return ret_arr;
	}

	private void initLkabThread()
	{
		game_loop_running = true;
		
		lkab_thread = new Thread(this);
		lkab_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(lkab_thread != null)
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
					
					if(!wire_grabbed)
						wire_grabbed = checkPlayerWireSrcCrossing();
					
					if(wire_grabbed)
						updateWires();
					
					repaint();
					
					delta = 0;
				}
			} // End of slave game-loop
		} // End of master game_loop
	}
	
	private void updatePlayer()
	{
		int[] direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of player
		int player_dx = direction_arr[0];
		int player_dy = direction_arr[1];
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < player_max_speed)
			player_speed_x += 0.5;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x -= 0.5;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && player_speed_y < player_max_speed)
			player_speed_y += 0.5;
		else if((!key_handler.UP || !key_handler.DOWN) && player_speed_y > 0)
			player_speed_y -= 0.5;
		
		// If within map constraints
		if(moveableX(player_x, player_dx))
			player_x += player_speed_x * player_dx;
		if(moveableY(player_y, player_dy))
			player_y += player_speed_y * player_dy;
	}
	
	// Moveable within x-direction
	private boolean moveableX(int x, int dx)
	{
		if(dx > 0)
			x += player_width;
		
		// Left
		if(dx < 0 && 0 < (x - player_speed_x))
			return true;
		// Right
		else if(dx > 0 && (x + player_speed_x) < this.width)
			return true;
		
		return false;
	}

	// Moveable within y-direction
	private boolean moveableY(int y, int dy)
	{		
		if(dy > 0)
			y += player_height;
		
		// Up
		if(dy < 0 && 0 < (y - player_speed_y))
			return true;
		// Down
		else if(dy > 0 && (y + player_speed_y) < this.height)
			return true;
		
		return false;
	}

	private void updateWires()
	{
		// Left batch of wires grabbed
		if(wire_x < (this.width / 2))
		{
//			boolean wire_placed = checkPlayerWireSrcCrossing();
		}
		
		// Right batch of wires grabbed
		else if(wire_x > (this.width / 2))
		{
			
		}
	}

	/*
	 * change method name asap
	 */
	// Check if player is crossing a src, i.e. 'grabbing' wire
	private boolean checkPlayerWireSrcCrossing()
	{
		boolean crossed_x = false;
		boolean crossed_y = false;
		
		int i;
		for(i = 0; i < wire_src_1.length; i++)
		{
			int wire_src_y = ((i * 2) + 1) * wire_src_height;
			// Player covers top half of wire src
			if(player_y < wire_src_y && (player_y + player_height) > (wire_src_y + (wire_src_height) / 2))
			{
				crossed_y = true;
				break;
			}
			
			// Player covers bottom half of wire src
			else if((player_y + player_height) > (wire_src_y + wire_src_height) && player_y < (wire_src_y + (wire_src_height) / 2))
			{
				crossed_y = true;
				break;
			}
		}
		
		if(crossed_y)
		{
			wire_y =  ((i * 2) + 1) * wire_src_height;
			wire_y += 2 * (wire_src_height / 5);
			
			// Left sources
			if(player_x < wire_width / 2)
			{
				wire_x = wire_src_width;
				crossed_x = true;
				switch(wire_src_1[i])
				{
					case 'r':
						active_color = Color.RED;
						break;
					case 'g':
						active_color = Color.GREEN;
						break;
					case 'b':
						active_color = Color.BLUE;
						break;
				}
			}
			
			// Right sources
			else if(player_x + player_width > this.width - (wire_src_width / 2))
			{
				wire_x = this.width - wire_width;
				crossed_x = true;
				switch(wire_src_2[i])
				{
					case 'r':
						active_color = Color.RED;
						break;
					case 'g':
						active_color = Color.GREEN;
						break;
					case 'b':
						active_color = Color.BLUE;
						break;
				}
			}
		}
		
		if(crossed_x && crossed_y)
			return true;
		return false;
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint swamp
		
		// Paint wire sources
		for(int i = 0; i < wire_src_1.length; i++)
		{
			// Left set of wire-src
			char color_1 = wire_src_1[i];
			switch(color_1)
			{
				case 'r':
					g_2d.setColor(Color.RED);
					break;
				case 'g':
					g_2d.setColor(Color.GREEN);
					break;
				case 'b':
					g_2d.setColor(Color.BLUE);
					break;
			}
			g_2d.fillRect(0, ((i * 2) + 1) * wire_src_height, wire_src_width, wire_src_height);

			// Right set of wire-src
			char color_2 = wire_src_2[i];
			switch(color_2)
			{
				case 'r':
					g_2d.setColor(Color.RED);
					break;
				case 'g':
					g_2d.setColor(Color.GREEN);
					break;
				case 'b':
					g_2d.setColor(Color.BLUE);
					break;
			}
			g_2d.fillRect(this.width - wire_src_width, ((i * 2) + 1) * wire_src_height, wire_src_width, wire_src_height);
		}
		
		// Paint player
		g_2d.setColor(Color.PINK);
		g_2d.fillRect(player_x, player_y, player_width, player_height);
		
		// Paint wire
		if(active_color != null)
		{
			g_2d.setColor(active_color);
			g_2d.setStroke(new BasicStroke(4));
			g_2d.drawLine(wire_x, wire_y, player_x, player_y + (player_height / 2));
		}
		
		g_2d.dispose();
	}
}
