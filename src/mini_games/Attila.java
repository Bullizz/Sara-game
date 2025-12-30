package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Player;
import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;

public class Attila extends JPanel implements Runnable
{
	Thread lulle_thread;
	int width, height;
	boolean game_loop_running, game_paused = false;
	Thread attila_thread;
	BufferedImage background_img;
	int background_x = 0;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	// Player parameters
	Player player;
	BufferedImage player_img;
	int player_speed = 0;
	
	// Attila parameters
	BufferedImage attila_img;
	
	// Keys parameters
	BufferedImage w_img, a_img, s_img, d_img;
	char[] keys_arr = {'w', 'a', 's', 'd'};
	int[][] falling_keys;
	int time_to_key_fall;
	
	int key_dim = 128;
	int x_ws = 256;
	int x_a = 128;
	int x_d = 384;
	int falling_speed = 10;

	public Attila(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width 	= frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		setBackground(new Color(32, 89, 255));
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
		key_handler.setAttila_active(true);
	
		// Time until first key-fall, 60 - 120 frames
		time_to_key_fall = (int) ((Math.random() * (120 - 60)) + 60);
		
		try
		{
			background_img= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/attila/background.png"));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		frame.add(this);
		frame.repaint();
		
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
			
			int frame_counter = 0;
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
					
					char active_key = key_handler.getCurrent_key();
//					System.out.println(active_key);

					updateFallingKeys(active_key);
					
					background_x -= falling_speed;
					if(background_x <= -(4 * width))
						background_x = 0;
					
					repaint();
					
					/*
					 * // Game completed
					 * if(player_x + player_width >= attila_x)
					 */
					
					frame_counter++;
					// Init. falling key
					if(frame_counter % time_to_key_fall == 0)
					{
						initFallingKey();
						frame_counter = 0;
						time_to_key_fall = (int) ((Math.random() * (60 - 30)) + 30);
					}
					
					delta = 0;
				}
			} // End of slave game-loop
		} // End of master game-loop
		new GamePanel(frame, top, game_timer, key_handler, player_x_passing, player_y_passing);

	}

	private void updatePlayer()
	{
		
	}
	
	private void updateFallingKeys(char active_key)
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		
		for(int i = 0; i < len; i++)
		{
			falling_keys[i][2] += falling_speed;
//			if(falling_keys[i][2] > height)
//				falling_keys[i] = null;
		}
	}
	
	private void initFallingKey()
	{
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
		len++;
		
		int[][] temp_arr = new int[len][3];
		int i;
		for(i = 0; i < len - 1; i++)
		{
			temp_arr[i][0] = falling_keys[i][0];
			temp_arr[i][1] = falling_keys[i][1];
			temp_arr[i][2] = falling_keys[i][2];
		}
		
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
			
			// Inital falling key
			if(i == 0)
				valid_key = true;
			// Ensure keys will not cover each other
			else if(temp_arr[i][1] != temp_arr[i - 1][1])
				valid_key = true;
		}
		
		falling_keys = temp_arr;
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint background
		g_2d.drawImage(background_img, background_x, 0, 5 * width, height, null);
		
		// Paint player
		
		// Paint attila
		
		// Paint player-keys
		
		// Paint falling keys
		int len = 0;
		if(falling_keys != null)
			len = falling_keys.length;
//			len = 1;
//		else
		
		for(int key_index = 0; key_index < len; key_index++)
		{
			g_2d.setColor(Color.WHITE);
			String s = "";
			switch(falling_keys[key_index][0])
			{
				// W
				case 0:
					s = "W";
					break;
				// A
				case 1:
					s = "A";
					break;
				// S
				case 2:
					s = "S";
					break;
				// D
				case 3:
					s = "D";
					break;
			}
			g_2d.fillRect(falling_keys[key_index][1], falling_keys[key_index][2], key_dim, key_dim);
			g_2d.setColor(Color.BLACK);
			g_2d.setFont(new Font("Arial", Font.BOLD, 30));
			g_2d.drawString(s, falling_keys[key_index][1] + (key_dim / 2), falling_keys[key_index][2] + (key_dim / 2));
		}
		g_2d.dispose();
	}
}