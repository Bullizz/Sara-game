package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Player;
import main.AudioHandler;
import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;
import menu.StartMenu;

public class Pauline extends JPanel implements Runnable
{
	int width, height;
	Thread pauline_thread;
	boolean game_loop_running;
	int falling_column;
	BufferedImage background_img;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	AudioHandler game_audio;
	int player_x_passing;
	int player_y_passing;

	// Player parameters
	Player player;
	BufferedImage player_img;
	int player_max_speed = 7;
	boolean carrying	 = false;
	
	// Item rain parameters
	String default_str = "default";
	String falling_str = "falling";
	String holding_str = "holding";
	
	String[][] item_array  = {{"song_book", "julmust", "pain_suprise", "christmas_card"}, // Item type
							 {default_str, default_str, default_str, default_str}, 		  // Item status: <default | falling | holding | placed>
							 {"-1", "-1", "-1", "-1"},									  // Item x-position
							 {"0", "0", "0", "0"},										  // Item y-position
							 {"0", "0", "0", "0"}};										  // Item placed status
	int rng_lim            = 4;
	int item_width		   = 100;
	int item_height		   = 100;
	int item_held_y		   = 23;
	int item_speed		   = 5;
	int[][] item_table_pos = {{251, 0},
							  {251, 200},
							  {251, 400},
							  {251, 600}};
	BufferedImage[] item_imgs = new BufferedImage[rng_lim];
	
	// Trash bin parameters
	int trash_x;
	int trash_y;
	int trash_width;
	int trash_height;
	BufferedImage trash_img;
	
	BufferedImage table_img;
	
	public Pauline(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, AudioHandler game_audio, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
		
		player = new Player(3 * (this.width / 5), this.height - 194, 112, 194);

		item_held_y += player.getPlayer_y();
		
		trash_width    = player.player_width;
		trash_height   = player.player_height / 2;
		trash_x  	   = this.width - player.player_width;
		trash_y  	   = player.getPlayer_y() + trash_height;
		
		falling_column = this.width / 6;
		
		try
		{
			background_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/backg_img.png"));
			player_img 	 	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/player.png"));
			trash_img 	 	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/trashbin.png"));
			table_img 	 	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/table.png"));
			
			item_imgs[0]	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/xmax_songbook.png"));
			item_imgs[1] 	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/julmust.png")); 
			item_imgs[2]	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/pain_suprise.png")); 
			item_imgs[3]	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/pauline/xmas_card.png")); 
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.game_audio			= game_audio;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
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
			
			// Slave game-loop
			while(game_loop_running)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{
					updatePlayer();
					
					updateItemRain();
					
					repaint();
															
					// Check if all items are placed
					checkPlacedStatus();
					
					// User exit
					if(key_handler.GamePanel_space_pressed)
					{
						pauline_thread = null;
						frame.remove(this);
						game_loop_running = false;
					}
					
					delta = 0;
				}
			} // End of slave loop
		} // End of master-loop
		if(key_handler.GamePanel_space_pressed)
		{
			game_timer.timer.cancel();
			
			frame.removeKeyListener(key_handler);
			frame.remove(this);

			top.setText("Vada a Bordo, Cazzo!");
			
			new StartMenu(frame, top, game_audio);
		}
		else
			new GamePanel(frame, top, game_timer, key_handler, game_audio, player_x_passing, player_y_passing);
	}

	private void updatePlayer()
	{
		int[] direction_arr = key_handler.getDirection_arr();

		int player_speed_x = player.getPlayer_speed_x();
		
		// Get positive/negative direction of player
		int player_dx = direction_arr[0];
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < player_max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;
		
		int player_x = player.getPlayer_x();
		
		// If within table and trash bin
		if(moveable(player_x, player_dx))
			player_x += player_speed_x * player_dx;
		
		player.setPlayer_x(player_x);
		player.setPlayer_speed_x(player_speed_x);
	}
	
	private void updateItemRain()
	{
		int player_x = player.getPlayer_x();
		for(int i = 0; i < rng_lim; i++)
		{
			// Default
			if(item_array[1][i].equals(default_str))
			{
				int init_fall_num = (int) (Math.random() * rng_lim);
				if(init_fall_num == i)
				{
					int falling_index = getValidFallingIndex();
					
					// Assign falling info to item_array
					item_array[1][i] = falling_str;
					
					falling_index += 2;
					
					// X-position of item
					int item_x = (falling_column * falling_index) + ((falling_column - item_width) / 2);
					item_array[2][i] = String.valueOf(item_x);
				}				
			}
			
			// Falling
			else if(item_array[1][i].equals(falling_str))
			{
				// Check if player holds any item
				boolean item_held = Arrays.asList(item_array[1]).contains(holding_str);
				
				// Item catched
				if(!item_held && itemCatched(i))
				{
					item_array[1][i] = holding_str;
					item_array[2][i] = String.valueOf(player_x - item_width);
					item_array[3][i] = String.valueOf(item_held_y);
				}
				else
				{
					int item_y = Integer.valueOf(item_array[3][i]);
					item_y += item_speed;
					item_array[3][i] = String.valueOf(item_y);
				
					// If item below screen
					if(item_y > this.height)
					{
						item_array[1][i] = default_str;
						item_array[3][i] = String.valueOf(-item_height);
					}
				}
			}
			
			// Holding
			else if(item_array[1][i].equals(holding_str))
			{
				// Trashing held item
				if(trash_x - (player_x + player.player_width) < 2)
				{
					item_array[1][i] = default_str;
					item_array[2][i] = "-1";
					item_array[3][i] = String.valueOf(-item_height);
				}
				
				// Placing held item on table
				if(player_x - (2 * falling_column) < 5 && item_array[4][i].equals("0"))
				{
					item_array[4][i] = "1";
					item_array[1][i] = default_str;
					item_array[2][i] = "-1";
					item_array[3][i] = String.valueOf(-item_height); 
				}
				
				item_array[2][i] = String.valueOf(player_x - item_width);
			}
		}
	}

	// Go through items and look for a falling column that isn't occupied
	private int getValidFallingIndex()
	{
		int valid_index_counter = 0;
		int falling_index = 0;
		
		// Randomized index for current item
		falling_index = (int) (Math.random() * rng_lim);
		int i;
		for(i = 0; i < rng_lim; i++)
		{
			// Falling index of current item_array element
			int current_falling_position = Integer.valueOf(item_array[2][i]);
			int current_falling_index = ((2 * current_falling_position) - falling_column + item_width) / (2 * falling_column);
			current_falling_index -= 2;
			
			if(falling_index != current_falling_index)
				valid_index_counter++;
		}
		
		// If generated column has item in it, init. item fall outside of game panel
		if(valid_index_counter != i)
			falling_index = rng_lim;
		
		
		return falling_index;
	}
	
	// If player "covers" falling item
	private boolean itemCatched(int item_index)
	{
		boolean x_crossed = false;
		boolean y_crossed = false;
		
		int x0 = Integer.valueOf(item_array[2][item_index]);
		int x1 = x0 + item_width;
		
		int y0 = Integer.valueOf(item_array[3][item_index]);
		int y1 = y0 + item_height;
		
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		int player_height = player.player_height;
		
		if(Math.abs(x0 - player_x) < (item_width / 2) || Math.abs(x1 - (player_x + player_height)) < (item_width / 2))
			x_crossed = true;
			
		if(player_y < y0 && y1 < (player_y + player_height))
			y_crossed = true;

		if(x_crossed && y_crossed)
			return true;
		else
			return false;
	}

	private boolean moveable(int player_x, int dx)
	{
		if(dx > 0)
			player_x += player.player_width;
		
		// Left
		if(dx < 0 && player_x > this.width / 3)
			return true;
		// Right
		else if(dx > 0 && player_x < this.trash_x)
			return true;
		
		return false;
	}
	
	private void checkPlacedStatus()
	{
		int item_placed_counter = 0;
		for(int i = 0; i < rng_lim; i++)
		{
			if(item_array[4][i].equals("1"))
				item_placed_counter++;
		}
		
		// All items are placed on table
		if(item_placed_counter == rng_lim)
		{
			pauline_thread = null;
			frame.remove(this);
			game_loop_running = false;
		}
	}
	
	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint room
//		g_2d.setColor(Color.BLUE);
		g_2d.drawImage(background_img, 0, 0, width, height, null);
		
		// Paint table
		g_2d.drawImage(table_img, 0, 0, 2 * falling_column, height, null);
		
		// Paint "containers" on table
		for(int i = 0; i < 4; i++)
		{
			// If items placed
			if(item_array[4][i].equals("1"))
				g_2d.drawImage(item_imgs[i], item_table_pos[i][0], item_table_pos[i][1], 100, 100, null);
			else
			{				
				g_2d.setColor(Color.WHITE);
				g_2d.fillRect(item_table_pos[i][0], item_table_pos[i][1], 100, 100);
			}
		}
		
		// Paint trash bin
		g_2d.drawImage(trash_img, trash_x, trash_y, trash_width, trash_height, null);
		
		// Paint player
		g_2d.drawImage(player_img, player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height, null);

		// Paint falling items
		for(int i = 0; i < rng_lim; i++)
		{
			int x = Integer.valueOf(item_array[2][i]);
			int y = Integer.valueOf(item_array[3][i]);
			g_2d.drawImage(item_imgs[i], x, y, item_width, item_height, null);
		}
		
		g_2d.dispose();
	}
}