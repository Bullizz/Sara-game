package mini_games;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;

public class Lkab extends JPanel implements Runnable
{
	int width, height;
	Thread lkab_thread;
	boolean game_loop_running, game_paused = false;
	boolean background_painted = false;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	// Player parameters
	int player_max_speed  = 3;
	int player_x 		  = 1689;
	int player_y 		  = 584;
	double player_speed_x = 0;
	double player_speed_y = 0;
	int player_width	  = 112;
	int player_height	  = 145;
	ImageIcon player_img;

	// Wires parameters
	Color active_color;
	char active_color_ch;
	int wire_width;
	int wire_x;
	int wire_y;
	boolean wire_grabbed = false;
	
	// Wire sources parameters
	char[] wire_src_1 = new char[3]; // Left side src
	int[] wire_src_1_placed = {0, 0, 0};
	char[] wire_src_2 = new char[3]; // Right side src
	int[] wire_src_2_placed = {0, 0, 0};
	int len = 3; // Length all arrays
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
		wire_width		= wire_src_height / 5;
		
		initLkabThread();
	}

	// Randomize order of appearing wire-src, <Red | Green | Blue>
	private char[] initWireSrcArr()
	{
		char[] ret_arr = new char[len];
		char[] arr = {'r', 'g', 'b'};
		int index = 0;
		while(index < len)
		{
			int i = (int) (Math.random() * len);
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
					
					updateWires();
					
					repaint();
					
					delta = 0;
					
					// All wires placed
					if(IntStream.of(wire_src_1_placed).sum() == 3)
					{
						lkab_thread = null;
						frame.remove(this);
						game_loop_running = false;
					}
					
//					frame.setTitle(String.valueOf(IntStream.of(wire_src_1_placed).sum()));
				}
			} // End of slave game-loop
		} // End of master game_loop
		new GamePanel(frame, top, game_timer, key_handler, player_x_passing, player_y_passing);
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
		boolean wire_placed;
		if(!wire_grabbed)
		{
			// Check if any wire is grabbed
			wire_grabbed = checkPlayerWireSrcCrossing('\0');
			
			// Ensure wire cannot be grabbed again if placed
			if(wire_grabbed && IntStream.of(wire_src_1_placed).sum() > 0)
			{
				for(int i = 0; i < len; i++)
				{
					if(wire_src_1_placed[i] == 1)
					{
						// If current wire-color exists in wire_src_<1|2>_placed
						if(active_color_ch == wire_src_1[i])
						{
							nullifyColorData();
							break;
						}
					}
				}
			}
		}
		
		else if(wire_grabbed)
		{
			// Grabbed wire from left side
			if(wire_x < this.width / 2)
			{
				wire_placed = checkPlayerWireSrcCrossing('r');
				if(wire_placed)
				{
					savePlacedColor(active_color);
					nullifyColorData();
				}
			}
			
			// Grabbed wire from right side
			else if(wire_x > this.width / 2)
			{
				wire_placed = checkPlayerWireSrcCrossing('l');
				if(wire_placed)
				{
					savePlacedColor(active_color);
					nullifyColorData();
				}
			}
		}
	}

	/*
	 * Check if player crosses a wire_src
	 * Method arguments: <'l' | 'r' | '\0'>
	 	* 'l': Check if player crosses a wire_src on the left side
	 	* 'r': Check if player crosses a wire_src on the right side
	 	* '\0': Check if player crosses a wire_src on any side
	 */
	private boolean checkPlayerWireSrcCrossing(char side)
	{
		boolean crossing_x = false;
		boolean crossing_y = false;
		
		int wire_x = 0;
		int wire_y = 0;
		
		// Check if player covers wire_src in y-dir
		int i;
		for(i = 0; i < len; i++)
		{
			// y-pos. of wire_src at "index" i
			int y = ((i * 2) + 1) * wire_src_height;
			
			// If player covers top half of wire_src
			if(player_y < y && (player_y + player_height) > (y + (wire_src_height / 2)))
			{
				crossing_y = true;
				break;
			}
			
			// If player covers bottom half of wire_src
			else if((player_y + player_height) > (y + wire_src_height) && player_y < (y + (wire_src_height / 2)))
			{
				crossing_y = true;
				break;
			}
		}
		
		if(crossing_y)
		{
			wire_y = ((i * 2) + 1) * wire_src_height;
			wire_y += 2 * wire_width;
			
			// Left-hand-side wire_src
			if(player_x < wire_src_width / 2)
			{
				wire_x = wire_src_width;
				crossing_x = true;
			}

			// Right-hand-side wire_src
			else if(player_x + player_width > this.width - (wire_src_width / 2))
			{
				wire_x = this.width - wire_src_width;
				crossing_x = true;
			}
		}
		
		if(crossing_x && crossing_y)
		{
			// If function-call wants to check left side
			if(side == 'l')
			{
				// Check if player crossed left side
				if(wire_x < this.width / 2)
				{
					// Check if color of crossed src matches grabbed src
					if(wire_src_1[i] == active_color_ch)
						return true;
					else
						return false;
				}
				else
					return false;
			}
			
			// If function-call wantsa to check right side
			else if(side == 'r')
			{				
				// Check if player crossed right side
				if(wire_x > this.width / 2)
				{
					// Check if color of crossed src matches grabbed src
					if(wire_src_2[i] == active_color_ch)
						return true;
					else
						return false;
				}
				else
					return false;
			}
			
			// wire_src crossed, i.e. wire grabbed
			else if(side == '\0')
			{
				// Grabbed wire from left side
				if(wire_x < this.width / 2)
				{
					// Set color
					switch(wire_src_1[i])
					{
						case 'r':
							active_color = Color.RED;
							active_color_ch = 'r';
							break;
						case 'g':								
							active_color = Color.GREEN;
							active_color_ch = 'g';
							break;
						case 'b':
							active_color = Color.BLUE;
							active_color_ch = 'b';
							break;
					}

				}
					
				// Grabbed wire from right side
				if(wire_x > this.width / 2)
				{
					// Set color
					switch(wire_src_2[i])
					{
						case 'r':
							active_color = Color.RED;
							active_color_ch = 'r';
							break;
						case 'g':								
							active_color = Color.GREEN;
							active_color_ch = 'g';
							break;
						case 'b':
							active_color = Color.BLUE;
							active_color_ch = 'b';
							break;
					}
				}
				
				this.wire_x = wire_x;
				this.wire_y = wire_y;
				
				return true;
			}
		}
		
		return false;
	}

	// Saves which color got placed
	private void savePlacedColor(Color active_color)
	{
		// Assign 1 to placed array
		for(int i = 0; i < len; i++)
		{
			if(wire_src_1[i] == active_color_ch)
				wire_src_1_placed[i] = 1;
			if(wire_src_2[i] == active_color_ch)
				wire_src_2_placed[i] = 1;
		}
	}
	
	private void nullifyColorData()
	{
		wire_grabbed = false;
		active_color = null;
		active_color_ch = 0;
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint swamp
		BufferedImage background_img = null;
		try
		{
			background_img = ImageIO.read(getClass().getResourceAsStream("/image_files/gallivare_swamp.png"));
			g_2d.drawImage(background_img, 0, 0, this.width, this.height, null);
		} catch(IOException e)
		{
			e.printStackTrace();
			
		}
		
		// Paint player
		g_2d.setColor(Color.PINK);
		g_2d.fillRect(player_x, player_y, player_width, player_height);
		
		// Paint wire sources
		for(int i = 0; i < len; i++)
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
		
		// Paint placed wire
		if(IntStream.of(wire_src_1_placed).sum() > 0)
		{
			int wire_x_1 = wire_src_width;
			int wire_x_2 = this.width - wire_src_width;

			int wire_y_1 = 0;
			int wire_y_2 = 0;
			
			for(int j = 0; j < len; j++)
			{
				if(wire_src_1_placed[j] == 1)
				{
					char active_color_ch = wire_src_1[j];
					wire_y_1 = ((j * 2) + 1) * wire_src_height;
					wire_y_1 += 2 * wire_width;
					
					for(int k = 0; k < len; k++)
					{
						if(wire_src_2[k] == active_color_ch)
						{
							wire_y_2 = ((k * 2) + 1) * wire_src_height;
							wire_y_2 += 2 * wire_width;
						}
					}
					switch(active_color_ch)
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

					g_2d.setStroke(new BasicStroke((float) wire_width));
					g_2d.drawLine(wire_x_1, wire_y_1, wire_x_2, wire_y_2);
				}
			}
		}
		
		// Paint dragging wire
		if(active_color != null)
		{
			g_2d.setColor(active_color);
			g_2d.setStroke(new BasicStroke((float) wire_width));
			g_2d.drawLine(wire_x, wire_y, player_x, player_y + (player_height / 2));
		}
		
		g_2d.dispose();
	}
}