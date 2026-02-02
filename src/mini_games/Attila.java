package mini_games;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Player;

import handlers.AudioHandler;
import handlers.KeyHandler;

import main.ErrorManagement;
import main.GamePanel;
import main.GameTimer;

import menu.StartMenu;

public class Attila extends JPanel implements Runnable
{
	/**/
	private static final long serialVersionUID = 7L;
	
	Thread lulle_thread;
	int width, height;
	boolean game_loop_running;
	Thread attila_thread;
	BufferedImage background_img;
	int background_x = 0;
	int angle = -30;
	
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
	int player_speed = 0;
	
	// Attila parameters
	BufferedImage attila_img;
	int attila_x, attila_y;
	
	// Keys parameters
	BufferedImage w_img, a_img, s_img, d_img;
	BufferedImage w_img_active, a_img_active, s_img_active, d_img_active;
	BufferedImage[] active_key_imgs;
	BufferedImage[] inactive_key_imgs;
	char[] keys_arr = {'w', 'a', 's', 'd'};
	int[][] falling_keys;
	int time_to_key_fall;
	char active_key;
	
	int key_dim		  = 128;
	int x_ws		  = 192;
	int x_a			  = 64;
	int x_d			  = 320;
	int falling_speed = 14;
	int[][] player_keys_pos;
	
	public Attila(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, AudioHandler game_audio, int player_x, int player_y)
	{
		super();
			this.width 	= frame.getWidth();
			this.height = (9 * frame.getHeight()) / 10;
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.game_audio			= game_audio;
		player_x_passing		= player_x;
		player_y_passing		= player_y;
		
		player = new Player(x_d + key_dim + 64,
							this.height - (15 + key_dim + key_dim),
							key_dim,
							2 * key_dim);
		
		attila_x = width  - (key_dim + 64);
		attila_y = height - (key_dim + 64);
		
		this.key_handler.setAttila_active(true);
	
		// Time until first key-fall, 60 - 120 frames
		time_to_key_fall = (int) ((Math.random() * (120 - 60)) + 60);
		
		player_keys_pos = new int[][]{{x_ws, this.height - (15 + key_dim + key_dim)},	// W
									  {x_a,  this.height - (15 + key_dim)},				// A
									  {x_ws, this.height - (15 + key_dim)},				// S
									  {x_d,  this.height - (15 + key_dim)}};			// D
		
		try
		{
			background_img		= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/background.png"));
			player_img			= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/player.png"));
			attila_img			= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/attila.png"));
			
			w_img_active		= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/W_active.png"));
			a_img_active 		= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/A_active.png"));
			s_img_active		= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/S_active.png"));
			d_img_active		= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/D_active.png"));
			active_key_imgs 	= new BufferedImage[]{w_img_active, a_img_active, s_img_active, d_img_active};
			
			w_img				= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/W_inactive.png"));
			a_img				= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/A_inactive.png"));
			s_img				= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/S_inactive.png"));
			d_img				= ImageIO.read(getClass().getResourceAsStream("/image_files/attila/D_inactive.png"));
			inactive_key_imgs	= new BufferedImage[]{w_img, a_img, s_img, d_img};
			
		} catch(Throwable ioe)
		{
			new ErrorManagement("<html><p>mini_games.Attila:</p><p>Reading File Error</p></html>", ioe.toString());
		}
		
		frame.add(this);
		frame.repaint();
		
		this.key_handler.setKey_available(true);
		initAttilaThread();
	}

	private void initAttilaThread()
	{
		game_loop_running = true;
		
		attila_thread = new Thread(this);
		attila_thread.start();
	}
	
	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(attila_thread != null)
		{
			double draw_interval = Math.pow(10, 9);
			draw_interval /= FPS;
			double delta = 0;
			long last_time = System.nanoTime();
			long current_time;
			
			int frame_counter		= 0;
			int stripe_counter		= 0;
			int active_key_counter	= 0;
			
			// Slave game-loop
			while(game_loop_running)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{
					updatePlayer();
					
					updateFallingKeys();
					
					// User pressed key
					if(key_handler.isKey_available())
						active_key = key_handler.getCurrent_key();
					if(active_key != '\0')
					{
						active_key_counter++;
						if(active_key_counter % 10 == 0)
						{
							key_handler.setKey_available(false);
							active_key = '\0';
							active_key_counter = 0;
						}
					}
					
					int[] collision = checkCollision(active_key);
					int index = collision[1];
					
					// Falling key "catched", i.e. key pressed when falling key in front of it 
					if(collision[0] == 1 && falling_keys[index][3] == 0)
					{
						player_speed++;
						falling_keys[index][3] = 1;
					}
					
					// Background movement
					background_x -= falling_speed;
					if(background_x <= -(4 * width))
						background_x = 0;
					
					repaint();
					
					 // Game completed
					 if(player.getPlayer_x() + player.player_width > attila_x + key_dim || key_handler.GamePanel_esc_pressed)
					 {
						 key_handler.setAttila_active(false);
						 
						 frame.remove(this);
						 game_loop_running = false;
						 attila_thread = null;
					 }
					 
					frame_counter++;
					
					// Init. a falling key, either after 20 to 60 frames
					if(frame_counter % time_to_key_fall == 0)
					{
						initFallingKey();
						frame_counter = 0;
						time_to_key_fall = (int) ((Math.random() * (60 - 20)) + 20);
					}
					
					stripe_counter++;
					
					// Player & Attila animation
					if(stripe_counter % 30 == 0)
					{
						toggleAngle();
						stripe_counter = 0;
					}
					
					// If playing audio file is ended
					if(game_audio.isAudio_finished())
					{
						int current_audio_index = game_audio.getCurrent_audio_index();
						game_audio = new AudioHandler("", current_audio_index);
					}
					
					delta = 0;
				}
			} // End of slave game-loop
		} // End of master game-loop
		if(key_handler.GamePanel_esc_pressed)
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
		int player_x = player.getPlayer_x();
		player_x += player_speed;
		player.setPlayer_x(player_x);
	}
	
	private void updateFallingKeys()
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		
		for(int i = 0; i < len; i++)
		{
			falling_keys[i][2] += falling_speed;
			
			// If falling key below GamePanel
			if(falling_keys[i][2] > height)
			{
				shortenArray(i);
				i--;
				len--;
			}
		}
	}
	
	// Check if falling key in front of pressed key
	private int[] checkCollision(char active_key)
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		
		// Get index representing pressed tile
		int active_key_int = -1;
		switch(active_key)
		{
			case 'w':
				active_key_int = 0;
				break;
			case 'a':
				active_key_int = 1;
				break;
			case 's':
				active_key_int = 2;
				break;
			case 'd':
				active_key_int = 3;
				break;
		}
		
		for(int key_index = 0; key_index < len; key_index++)
		{
			// If pressed key matches falling key
			if(falling_keys[key_index][0] == active_key_int)
			{				
				double locked_y = Double.valueOf(player_keys_pos[active_key_int][1]);
				double height = Double.valueOf(key_dim);
				double falling_y = Double.valueOf(falling_keys[key_index][2]);
				
				// Check if falling key covers atleast 75% of pressed key
				if(locked_y < falling_y && falling_y - (locked_y + height) <= locked_y + (height / 4))
					return new int[] {1, key_index};
				else if(locked_y > falling_y && (falling_y + height) >= locked_y + (0.75 * height))
					return new int[] {1, key_index};
			}
		}
		return new int[]{0, 0};
	}
	
	private void initFallingKey()
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		len++;
		
		// Fill new array with existing values
		int[][] temp_arr = new int[len][4];
		int i;
		for(i = 0; i < len - 1; i++)
		{
			temp_arr[i][0] = falling_keys[i][0];
			temp_arr[i][1] = falling_keys[i][1];
			temp_arr[i][2] = falling_keys[i][2];
			temp_arr[i][3] = falling_keys[i][3];
		}
		
		// Add new element (falling key) to array
		boolean valid_key = false;
		while(!valid_key)
		{
			int new_key_index = (int) (Math.random() * keys_arr.length);
			char new_key = keys_arr[new_key_index];
			
			temp_arr[i][0] = new_key_index;
			
			if(new_key == 'a')
				temp_arr[i][1] = x_a;
			else if(new_key == 'w' || new_key == 's')
				temp_arr[i][1] = x_ws;
			else if(new_key == 'd')
				temp_arr[i][1] =  x_d;
			
			temp_arr[i][2] = -key_dim;
			temp_arr[i][3] = 0;
			
			// Inital falling key
			if(i == 0)
				valid_key = true;
			// Ensure keys will not cover each other
			else if(temp_arr[i][1] != temp_arr[i - 1][1])
				valid_key = true;
		}
		
		falling_keys = temp_arr;
	}

	// Remove item from array when outside GamePanel
	private void shortenArray(int remove_index)
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		if(len < 1)
			return;
		
		int[][] temp_arr = new int[len - 1][4];
		int j = 0;
		for(int i = 0; i < len; i++)
		{
			if(i == remove_index)
				i++;
			temp_arr[j] = falling_keys[i];
			j++;
		}
		
		falling_keys = temp_arr;
	}
	
	private void toggleAngle()
	{
		if(angle == -30)
			angle = 30;
		else if(angle == 30)
			angle = -30;
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint background
		g_2d.drawImage(background_img, background_x, 0, 5 * width, height, null);
		
		// Paint player
		g_2d.rotate(Math.toRadians(angle), player.getPlayer_x() + (player.player_width / 2), player.getPlayer_y() + (player.player_height / 2));
		g_2d.drawImage(player_img, player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height, null);
		g_2d.rotate(Math.toRadians(-angle), player.getPlayer_x() + (player.player_width / 2), player.getPlayer_y() + (player.player_height / 2));
	
		// Paint attila
		g_2d.rotate(Math.toRadians(-angle), attila_x + (key_dim / 2), attila_y + (key_dim / 2));
		g_2d.drawImage(attila_img, attila_x, attila_y, key_dim, key_dim, null);
		g_2d.rotate(Math.toRadians(angle), attila_x + (key_dim / 2), attila_y + (key_dim / 2));
		
		// Paint player-keys
		for(int i = 0; i < player_keys_pos.length; i++)
		{
			if(keys_arr[i] == active_key)
				g_2d.drawImage(active_key_imgs[i], player_keys_pos[i][0], player_keys_pos[i][1], key_dim, key_dim, null);
			else
				g_2d.drawImage(inactive_key_imgs[i], player_keys_pos[i][0], player_keys_pos[i][1], key_dim, key_dim, null);
		}
		
		// Paint falling keys
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		
		for(int key_index = 0; key_index < len; key_index++)
		{
			BufferedImage key_img = null;
			switch(falling_keys[key_index][0])
			{
				// W
				case 0:
					key_img = w_img;
					break;
				// A
				case 1:
					key_img = a_img;
					break;
				// S
				case 2:
					key_img = s_img;
					break;
				// D
				case 3:
					key_img = d_img;
					break;
			}
			// Not "picked" by player
			if(falling_keys[key_index][3] == 0)
				g_2d.drawImage(key_img, falling_keys[key_index][1], falling_keys[key_index][2], key_dim, key_dim, null);
		}
		
		g_2d.dispose();
	}
}