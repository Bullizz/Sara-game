package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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

public class SSC extends JPanel implements Runnable
{
	int width, height;
	Thread ssc_thread;
	boolean game_loop_running;
	BufferedImage background_img;
	int cycle_counter = 0;
	
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
	BufferedImage player_img, player_img_left, player_img_right;
	int max_speed;
	boolean striping = false;
	
	// Rocket parameters
	BufferedImage rocket_img_UP, rocket_img_LEFT, rocket_img_DOWN, rocket_img_RIGHT;
	int rocket_cycles;
	boolean active_cycle	= false;
	boolean active_rockets	= false;
	int[] rocket_pos_UP		= new int[20];	// Rockets from above
	int[] rocket_pos_LEFT	= new int[5];	// Rockets from the left
	int[] rocket_pos_DOWN	= new int[20];	// Rockets from below
	int[] rocket_pos_RIGHT	= new int[5];	// Rockets from the right
	int rocket_src;
	int dRocket = 0;
	
	int rocket_width;
	int rocket_height;
	
	public SSC(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, AudioHandler game_audio, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
		
		try
		{
			background_img		= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/background_img.png"));
			player_img_left		= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/player_LEFT.png"));
			player_img_right	= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/player_RIGHT.png"));
			player_img			= player_img_right;
			
			rocket_img_UP		= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/rocket_UP.png"));
			rocket_img_LEFT		= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/rocket_LEFT.png"));
			rocket_img_DOWN		= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/rocket_DOWN.png"));
			rocket_img_RIGHT	= ImageIO.read(getClass().getResourceAsStream("/image_files/ssc/rocket_RIGHT.png"));
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
		
		int x0 = ((10 * width) - height) / 20;
		int y0 = ((10 * height) - width) / 20;
		player = new Player(x0, y0, height / 10, width / 10);
		
		max_speed = player.max_speed * 3;
		
		// Gen. 4 - 10 rocket cycles
		rocket_cycles = (int) ((Math.random() * (10 - 4)) + 4);
		
		frame.add(this);
		frame.repaint();
		
		initSscThread();
	}

	private void initSscThread()
	{
		game_loop_running = true;
		
		ssc_thread = new Thread(this);
		ssc_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(ssc_thread != null)
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
					
					updateRockets();

					boolean collision = checkCollision();
					if(collision)
					{
						game_timer.setTime_coeff(10);
						initStripingEffect();
					}
					
					// Minigame completed or esc. pressed
					if(cycle_counter >= rocket_cycles || key_handler.GamePanel_esc_pressed)
					{
						frame.remove(this);
						
						game_loop_running = false;
						ssc_thread = null;
					}
					
					repaint();
					
					// If playing audio file is ended
					if(game_audio.isAudio_finished())
					{
						int current_audio_index = game_audio.getCurrent_audio_index();
						game_audio = new AudioHandler("", current_audio_index);
					}
					
					delta = 0;
				}
			} // End  of slave game-loop
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
		int[] direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of player
		int player_dx = direction_arr[0];
		int player_dy = direction_arr[1];
		
		int player_speed_x = player.getPlayer_speed_x();
		int player_speed_y = player.getPlayer_speed_y();
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && player_speed_y < max_speed)
			player_speed_y++;
		else if((!key_handler.UP || !key_handler.DOWN) && player_speed_y > 0)
			player_speed_y--;
		
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		// If within map constraints
		if(moveableX(player_x, player_dx))
		{
			player_x += player_speed_x * player_dx;
			
			// Update player img
			if(player_dx > 0)
				player_img = player_img_right;
			else if(player_dx < 0)
				player_img = player_img_left;
		}
		if(moveableY(player_y, player_dy))
			player_y += player_speed_y * player_dy;
		
		player.setPlayer_x(player_x);
		player.setPlayer_y(player_y);
		player.setPlayer_speed_x(player_speed_x);
		player.setPlayer_speed_y(player_speed_y);
	}
	
	// Moveable within x-direction
	private boolean moveableX(int x, int dx)
	{
		if(dx > 0)
			x += player.player_width;
		
		int player_speed_x = player.getPlayer_speed_x();
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
			y += player.player_height;
		
		int player_speed_y = player.getPlayer_speed_y();
		// Up
		if(dy < 0 && 0 < (y - player_speed_y))
			return true;
		// Down
		else if(dy > 0 && (y + player_speed_y) < this.height)
			return true;
		
		return false;
	}
	
	private void updateRockets()
	{
		// Activate a rocket cycle
		if(!active_rockets)
		{
			// Get which side rockets will come from
			rocket_src = (int) (Math.random() * 4);
			switch(rocket_src)
			{
				// From above
				case 0:
					rocket_pos_UP = getRocketPosArray(rocket_pos_UP.length);
						rocket_width  = player.player_width;
						rocket_height = player.player_height;
					dRocket = -rocket_height;
					break;
				// From left
				case 1:
					rocket_pos_LEFT = getRocketPosArray(rocket_pos_LEFT.length);
						rocket_height = player.player_height;
						rocket_width = 2 * rocket_height;
					dRocket = -rocket_width;
					break;
				// From below
				case 2:
					rocket_pos_DOWN = getRocketPosArray(rocket_pos_DOWN.length);
						rocket_width  = player.player_width;
						rocket_height = player.player_height;
					dRocket = height;
					break;
				// From right
				case 3:
					rocket_pos_RIGHT = getRocketPosArray(rocket_pos_RIGHT.length);
						rocket_height = player.player_height;
						rocket_width = 2 * rocket_height;
					dRocket = width;
					break;
			}
			active_rockets = true;
		}
		
		// Rocket cycle active
		else if(active_rockets)
		{
			switch(rocket_src)
			{
				// From above
				case 0:
					if(dRocket > height)
					{
						cycle_counter++;
						active_rockets = false;
					}
					else
						dRocket += max_speed;
					break;
					
				// From left
				case 1:
					if(dRocket > width)
					{
						cycle_counter++;
						active_rockets = false;
					}
					else
						dRocket += 2 * max_speed;
					break;
				
				// From below
				case 2:
					if(dRocket + rocket_height < 0)
					{
						cycle_counter++;
						active_rockets = false;
					}
					else
						dRocket -= max_speed;
					break;
				
				// From right
				case 3:
					if(dRocket + rocket_width < 0)
					{
						cycle_counter++;
						active_rockets = false;
					}
					else
						dRocket -= 2 * max_speed;
					break;
			}
		}
	}

	// Check player-rocket collision
	private boolean checkCollision()
	{
		boolean crossing_x = false;
		boolean crossing_y = false;
		
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		switch(rocket_src)
		{
			// From above
			case 0:
				int y0  = dRocket;
				for(int rocket_index = 0; rocket_index < rocket_pos_UP.length; rocket_index++)
				{
					if(rocket_pos_UP[rocket_index] == 1)
					{
						int x0  = rocket_width * rocket_index;
						
						// Horizontal collision
						if(player_x < x0 && (player_x + player.player_width) >= x0 + (rocket_width / 2))
							crossing_x = true; 
						else if(x0 < player_x && player_x <= x0 + (rocket_width / 2))
							crossing_x = true; 
	
						// Vertical collision
						if(player_y < y0 && player_y + player.player_height >= y0 + (rocket_height / 2))
							crossing_y = true;
						else if(player_y > y0 && player_y <= y0 + (rocket_height / 2))
							crossing_y = true;
						
						if(crossing_x && crossing_y)
							return true;
					}
				}
				break;
				
			// From the left
			case 1:
				int x1 = dRocket;
				for(int rocket_index = 0; rocket_index < rocket_pos_LEFT.length; rocket_index++)
				{
					if(rocket_pos_LEFT[rocket_index] == 1)
					{
						int y1 = rocket_height * rocket_index;

						// Horizontal collision
						if(player_x < x1 && (player_x + player.player_width) >= x1 + (rocket_width / 2))
							crossing_x = true; 
						else if(x1 < player_x && player_x <= x1 + (rocket_width / 2))
							crossing_x = true; 
	
						// Vertical collision
						if(player_y < y1 && player_y + player.player_height >= y1 + (rocket_height / 2))
							crossing_y = true;
						else if(player_y > y1 && player_y <= y1 + (rocket_height / 2))
							crossing_y = true;
						
						if(crossing_x && crossing_y)
							return true;
					}
				}
				break;
				
			// From below
			case 2:
				int y2 = dRocket;
				for(int rocket_index = 0; rocket_index < rocket_pos_UP.length; rocket_index++)
				{
					if(rocket_pos_DOWN[rocket_index] == 1)
					{
						int x2 = rocket_width * rocket_index;

						// Horizontal collision
						if(player_x < x2 && (player_x + player.player_width) >= x2 + (rocket_width / 2))
							crossing_x = true; 
						else if(x2 < player_x && player_x <= x2 + (rocket_width / 2))
							crossing_x = true; 
		
						// Vertical collision
						if(player_y < y2 && player_y + player.player_height >= y2 + (rocket_height / 2))
							crossing_y = true;
						else if(player_y > y2 && player_y <= y2 + (rocket_height / 2))
							crossing_y = true;
						
						if(crossing_x && crossing_y)
							return true;
					}
				}
				break;
				
			// From the right
			case 3:
				int x3 = dRocket;
				for(int rocket_index = 0; rocket_index < rocket_pos_LEFT.length; rocket_index++)
				{
					if(rocket_pos_RIGHT[rocket_index] == 1)
					{
						int y3 = rocket_height * rocket_index;
						
						// Horizontal collision
						if(player_x < x3 && (player_x + player.player_width) >= x3 + (rocket_width / 2))
							crossing_x = true; 
						else if(x3 < player_x && player_x <= x3 + (rocket_width / 2))
							crossing_x = true; 
	
						// Vertical collision
						if(player_y < y3 && player_y + player.player_height >= y3 + (rocket_height / 2))
							crossing_y = true;
						else if(player_y > y3 && player_y <= y3 + (rocket_height / 2))
							crossing_y = true;
						
						if(crossing_x && crossing_y)
							return true;
					}
				}
				break;
		}
		
		return false;
	}
	
	// Get position-indices of rockets, e.g. [0, 1, 0, 1, 1]
	private int[] getRocketPosArray(int length)
	{
		int[] pos_arr = new int[length];
		int places_positioned = 0;
		int index = 0;
		
		// Array needs atleast 11 (src: <0 | 2>) or 3 (src: <1 | 3>) elements = 1
		while(places_positioned < (length / 2) + 1)
		{
			// Randomize where rockets will be launched from
			int place = (int) (Math.random() * ((length / 2) + 1));
			if(place == 0 && pos_arr[index] != 1)
			{
				pos_arr[index] = 1;
				places_positioned++;
			}
			
			index++;
			if(index == length)
				index = 0;
		}
		
		return pos_arr;
	}

	private void toggleStriping()
	{
		if(striping)
			striping = false;
		else if(!striping)
			striping = true;
	}
	
	// Striping effect when hit by rocket
	private void initStripingEffect()
	{
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			int t = 0;
			@Override
			public void run()
			{
				// Toggle every 30 ms
				if(t % 3 == 0)
					toggleStriping();
				
				// Effect lasting for 800 ms
				if(t > 80)
				{
					striping = false;
					game_timer.setTime_coeff(1);
					timer.cancel();
				}
				t++;
			}
		};
		timer.scheduleAtFixedRate(task, 0, 10);
	}
	
	@Override
	public void paintComponent(Graphics g_1d)
	{		
		super.paintComponent(g_1d);
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint background
		g_2d.drawImage(background_img, 0, 0, width, height, null);
		
		// Paint player
		if(!striping)
			g_2d.drawImage(player_img, player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height, null);
	
		// Paint rockets
		g_2d.setColor(Color.GRAY);
		if(active_rockets)
		{
			switch(rocket_src)
			{
				// Paint rockets from above
				case 0:
					for(int rocket_index = 0; rocket_index < rocket_pos_UP.length; rocket_index++)
					{
						if(rocket_pos_UP[rocket_index] == 1)
							g_2d.drawImage(rocket_img_UP, rocket_index * rocket_width, dRocket, rocket_width, rocket_height, null);
					}
					break;
				
				// Paint rockets from left
				case 1:
					for(int rocket_index = 0; rocket_index < rocket_pos_LEFT.length; rocket_index++)
					{
						if(rocket_pos_LEFT[rocket_index] == 1)
							g_2d.drawImage(rocket_img_LEFT, dRocket, rocket_index * rocket_height, rocket_width, rocket_height, null);
					}
					break;
				
				// Paint rockets from below
				case 2:
					for(int rocket_index = 0; rocket_index < rocket_pos_DOWN.length; rocket_index++)
					{
						if(rocket_pos_DOWN[rocket_index] == 1)
							g_2d.drawImage(rocket_img_DOWN, rocket_index * rocket_width, dRocket, rocket_width, rocket_height, null);
					}
					break;
					
				// Paint rockets from right
				case 3:
					for(int rocket_index = 0; rocket_index < rocket_pos_RIGHT.length; rocket_index++)
					{
						if(rocket_pos_RIGHT[rocket_index] == 1)
							g_2d.drawImage(rocket_img_RIGHT, dRocket, rocket_index * rocket_height, rocket_width, rocket_height, null);
					}
					break;
			}
		}
		g_2d.dispose();
	}
}